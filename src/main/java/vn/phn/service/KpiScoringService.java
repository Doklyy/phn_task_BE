package vn.phn.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import vn.phn.dto.EmployeeScore;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Tính điểm KPI từ 3 file CSV theo đúng sheet KPIs:
 * - Điểm nhiệm vụ (max 15): (Tổng_điểm_đạt / Tổng_điểm_giao) × 15
 * - Điểm báo cáo cuối ngày (max 5): lấy từ dòng "Điểm chuyên cần LK Tháng"
 * - Điểm thời gian làm việc – thưởng (max 5): (Điểm thực tế đạt / (Ngày LV × 8)) × 5
 */
@Service
public class KpiScoringService {

    private static final Map<String, Double> WEIGHT_MAP = Map.of(
            "Rất cao", 8.0,
            "Cao", 5.0,
            "Bình thường", 3.0,
            "Rất thấp", 1.0
    );

    private static final double TASK_SCORE_MAX = 15.0;
    private static final double REPORT_SCORE_MAX = 5.0;
    private static final double WORK_TIME_SCORE_MAX = 5.0;

    /**
     * Đọc 3 file CSV (InputStream) và tính điểm KPI cho từng nhân viên.
     *
     * @param nhiemVuCsv   Sheet "Theo doi Nhiem vu (2).csv" – cột: Chủ trì, Trọng số CV, Chất lượng CV, Trạng thái CV
     * @param baoCaoCsv    Sheet "Bao cao CV cuoi ngay.csv" – có dòng "Điểm chuyên cần LK Tháng", cột theo tên NV
     * @param thoiGianCsv  Sheet "Thoi gian lam viec.csv" – có dòng "Điểm thực tế đạt", "Ngày LV", cột theo tên NV
     */
    public List<EmployeeScore> tinhToanKPI(InputStream nhiemVuCsv, InputStream baoCaoCsv, InputStream thoiGianCsv) throws IOException {
        Map<String, Double> diemNhiemVu = tinhDiemNhiemVu(nhiemVuCsv);
        Map<String, Double> diemBaoCao = tinhDiemBaoCao(baoCaoCsv);
        Map<String, Double> diemThoiGian = tinhDiemThoiGian(thoiGianCsv);

        Set<String> allNhanVien = new HashSet<>();
        allNhanVien.addAll(diemNhiemVu.keySet());
        allNhanVien.addAll(diemBaoCao.keySet());
        allNhanVien.addAll(diemThoiGian.keySet());

        List<EmployeeScore> result = new ArrayList<>();
        for (String nv : allNhanVien) {
            double taskScore = diemNhiemVu.getOrDefault(nv, 0.0);
            double reportScore = diemBaoCao.getOrDefault(nv, 0.0);
            double workTimeScore = diemThoiGian.getOrDefault(nv, 0.0);
            double tong = taskScore + reportScore + workTimeScore;
            result.add(EmployeeScore.builder()
                    .employeeName(nv)
                    .taskScore(round2(taskScore))
                    .reportScore(round2(reportScore))
                    .workTimeScore(round2(workTimeScore))
                    .totalScore(round2(tong))
                    .build());
        }
        result.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));
        return result;
    }

    /**
     * Điểm nhiệm vụ = (Tổng_điểm_đạt / Tổng_điểm_giao) × 15
     * Tổng_điểm_đạt = ∑ (W × Q × T), Tổng_điểm_giao = ∑ W
     * Q = 1 nếu "Đạt chuẩn", else 0. T = 1 nếu "Hoàn thành đúng hạn", else 0.
     */
    private Map<String, Double> tinhDiemNhiemVu(InputStream input) throws IOException {
        Map<String, Double> sumDat = new HashMap<>();
        Map<String, Double> sumGiao = new HashMap<>();

        List<CSVRecord> records = readAllCsv(input);
        if (records.size() < 2) return new HashMap<>();
        CSVRecord headerRow = records.get(0);
        int idxChuTri = findColumnIndex(headerRow, 0, "Chủ trì", "Chu tri");
        int idxW = findColumnIndex(headerRow, 1, "Trọng số CV", "Trong so CV");
        int idxQ = findColumnIndex(headerRow, 2, "Chất lượng CV", "Chat luong CV");
        int idxT = findColumnIndex(headerRow, 3, "Trạng thái CV", "Trang thai CV");

        for (int row = 1; row < records.size(); row++) {
            CSVRecord r = records.get(row);
            String chuTri = getByIndex(r, idxChuTri).trim();
            if (chuTri.isEmpty()) continue;

            String wStr = getByIndex(r, idxW).trim();
            String q = getByIndex(r, idxQ).trim();
            String t = getByIndex(r, idxT).trim();

            double w = WEIGHT_MAP.getOrDefault(wStr, 0.0);
            double qVal = "Đạt chuẩn".equalsIgnoreCase(q) ? 1.0 : 0.0;
            double tVal = "Hoàn thành đúng hạn".equalsIgnoreCase(t) ? 1.0 : 0.0;

            sumDat.merge(chuTri, w * qVal * tVal, Double::sum);
            sumGiao.merge(chuTri, w, Double::sum);
        }

        Map<String, Double> result = new HashMap<>();
        sumDat.forEach((nv, dat) -> {
            double giao = sumGiao.getOrDefault(nv, 0.0);
            double diem = giao > 0 ? (dat / giao) * TASK_SCORE_MAX : 0.0;
            result.put(nv, Math.min(TASK_SCORE_MAX, diem));
        });
        return result;
    }

    /**
     * Điểm báo cáo: lấy từ dòng "Điểm chuyên cần LK Tháng" (đã tính sẵn trong sheet), max 5.
     * Cấu trúc: cột 0 = nhãn dòng, cột 1..n = tên nhân viên (header), các dòng sau có giá trị.
     */
    private Map<String, Double> tinhDiemBaoCao(InputStream input) throws IOException {
        Map<String, Double> result = new HashMap<>();
        List<CSVRecord> records = readAllCsv(input);
        if (records.isEmpty()) return result;

        for (CSVRecord r : records) {
            if (r.size() < 2) continue;
            String label = r.get(0).trim();
            if (!"Điểm chuyên cần LK Tháng".equals(label)) continue;

            CSVRecord headerRow = records.get(0);
            for (int i = 1; i < r.size() && i < headerRow.size(); i++) {
                String nv = headerRow.get(i).trim();
                if (nv.isEmpty()) continue;
                try {
                    String val = r.get(i).trim().replace(",", ".");
                    double diem = Double.parseDouble(val);
                    result.put(nv, Math.min(REPORT_SCORE_MAX, Math.max(0, diem)));
                } catch (NumberFormatException ignored) {
                }
            }
            break;
        }
        return result;
    }

    /**
     * Điểm thời gian = (Điểm thực tế đạt / (Ngày LV × 8)) × 5, max 5.
     * Cần 2 dòng: "Điểm thực tế đạt" và "Ngày LV".
     */
    private Map<String, Double> tinhDiemThoiGian(InputStream input) throws IOException {
        Map<String, Double> result = new HashMap<>();
        List<CSVRecord> records = readAllCsv(input);
        if (records.size() < 2) return result;

        CSVRecord headerRow = records.get(0);
        Map<Integer, Double> diemThucTeByCol = new HashMap<>();
        Map<Integer, Double> ngayLVByCol = new HashMap<>();

        for (CSVRecord r : records) {
            if (r.size() < 2) continue;
            String label = r.get(0).trim();
            if ("Điểm thực tế đạt".equals(label)) {
                for (int i = 1; i < r.size(); i++) {
                    try {
                        String val = r.get(i).trim().replace(",", ".");
                        diemThucTeByCol.put(i, Double.parseDouble(val));
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else if ("Ngày LV".equals(label)) {
                for (int i = 1; i < r.size(); i++) {
                    try {
                        String val = r.get(i).trim().replace(",", ".");
                        ngayLVByCol.put(i, Double.parseDouble(val));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        for (int i = 1; i < headerRow.size(); i++) {
            String nv = headerRow.get(i).trim();
            if (nv.isEmpty()) continue;
            Double diemThucTe = diemThucTeByCol.get(i);
            Double ngayLV = ngayLVByCol.get(i);
            if (diemThucTe == null) diemThucTe = 0.0;
            if (ngayLV == null || ngayLV <= 0) {
                result.put(nv, 0.0);
                continue;
            }
            double diem = (diemThucTe / (ngayLV * 8.0)) * WORK_TIME_SCORE_MAX;
            result.put(nv, Math.min(WORK_TIME_SCORE_MAX, Math.max(0, diem)));
        }
        return result;
    }

    private List<CSVRecord> readAllCsv(InputStream input) throws IOException {
        List<CSVRecord> list = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setTrim(true).build())) {
            for (CSVRecord r : parser) {
                list.add(r);
            }
        }
        return list;
    }

    private int findColumnIndex(CSVRecord headerRow, int defaultIndex, String... names) {
        for (String name : names) {
            for (int i = 0; i < headerRow.size(); i++) {
                if (name.equalsIgnoreCase(headerRow.get(i).trim())) return i;
            }
        }
        return defaultIndex;
    }

    private String getByIndex(CSVRecord r, int index) {
        return r.size() > index ? r.get(index) : "";
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}

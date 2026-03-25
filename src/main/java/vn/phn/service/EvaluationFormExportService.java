package vn.phn.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import vn.phn.dto.ScoringDto;
import vn.phn.entity.User;
import vn.phn.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.YearMonth;
import java.util.List;

/**
 * Sinh file Excel phiếu đánh giá cá nhân theo mẫu PHIEU_TEMPLATE.xlsx.
 *
 * YÊU CẦU: đặt file template tại backend/src/main/resources/templates/PHIEU_TEMPLATE.xlsx
 * với cấu trúc giống file "Copy of Check_Phiếu đánh giá NV Tháng 2".
 *
 * Service này chỉ điền các thông tin cơ bản:
 * - Họ tên + mã nhân viên (dòng "Bên nhận việc")
 * - Tổng điểm (totalScore)
 * - Điểm báo cáo công việc cuối ngày (dailyReportScore5)
 * - Điểm thưởng thời gian làm việc (timeWorkScore5)
 * Các phần I, II… giữ nguyên/để trống theo template.
 */
@Service
@RequiredArgsConstructor
public class EvaluationFormExportService {

    private final ScoringService scoringService;
    private final UserRepository userRepository;

    public Workbook buildWorkbookForMonth(YearMonth month) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/PHIEU_TEMPLATE.xlsx");
        try (InputStream is = resource.getInputStream()) {
            // Đọc trực tiếp file template thành một workbook
            Workbook wb = new XSSFWorkbook(is);
            Sheet templateSheet = wb.getSheetAt(0);

            List<ScoringDto> scores = scoringService.getRanking(month);

            boolean first = true;
            for (ScoringDto s : scores) {
                Long uid = s.getUserId();
                if (uid == null) continue;
                User user = userRepository.findById(uid).orElse(null);
                if (user == null) continue;

                String sheetName = user.getName() != null ? user.getName() : "User-" + uid;

                Sheet sheet;
                if (first) {
                    // Dùng luôn sheet template cho nhân viên đầu tiên
                    sheet = templateSheet;
                    int idx = wb.getSheetIndex(sheet);
                    wb.setSheetName(idx, sheetName);
                    first = false;
                } else {
                    // Nhân bản sheet template cho các nhân viên tiếp theo
                    int templateIndex = wb.getSheetIndex(templateSheet);
                    Sheet clonedIndex = wb.cloneSheet(templateIndex);  // <-- clone bằng index
                    sheet = wb.getSheetAt(templateIndex);
                    wb.setSheetName(templateIndex, sheetName);
                }

                // Bên nhận việc – ví dụ B11
                Row row11 = sheet.getRow(10);
                if (row11 != null) {
                    Cell cell = getOrCreateCell(row11, 1);
                    String username = user.getUsername() != null ? user.getUsername() : "";
                    String fullName = user.getName() != null ? user.getName() : "";
                    cell.setCellValue("Bên nhận việc: Đ/c " + fullName + " - " + username);
                }

                // Tổng điểm
                Row row15 = sheet.getRow(14);
                if (row15 != null && s.getTotalScore() != null) {
                    Cell cell = getOrCreateCell(row15, 7);
                    cell.setCellValue(s.getTotalScore());
                }

                // Điểm báo cáo công việc cuối ngày (V) – ví dụ H44
                Row row44 = sheet.getRow(43);
                if (row44 != null && s.getDailyReportScore5() != null) {
                    Cell cell = getOrCreateCell(row44, 7);
                    cell.setCellValue(s.getDailyReportScore5());
                }

                // Thưởng thời gian làm việc (VI) – ví dụ H47
                Row row47 = sheet.getRow(46);
                if (row47 != null && s.getTimeWorkScore5() != null) {
                    Cell cell = getOrCreateCell(row47, 7);
                    cell.setCellValue(s.getTimeWorkScore5());
                }
            }

            return wb;
        }
    }

    private static Cell getOrCreateCell(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }
        return cell;
    }
}


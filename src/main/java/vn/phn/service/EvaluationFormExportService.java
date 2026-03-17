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
            Workbook templateWb = new XSSFWorkbook(is);
            Sheet templateSheet = templateWb.getSheetAt(0);

            Workbook wb = new XSSFWorkbook();

            List<ScoringDto> scores = scoringService.getRanking(month);

            for (ScoringDto s : scores) {
                Long uid = s.getUserId();
                if (uid == null) continue;
                User user = userRepository.findById(uid).orElse(null);
                if (user == null) continue;

                String sheetName = user.getName() != null ? user.getName() : "User-" + uid;
                Sheet sheet = wb.createSheet(sheetName);
                copySheet(templateSheet, sheet);

                // DÒNG / CỘT CẦN ĐIỀN – điều chỉnh cho khớp template thực tế nếu cần
                // Bên nhận việc – ví dụ B11
                Row row11 = sheet.getRow(10);
                if (row11 != null) {
                    Cell cell = getOrCreateCell(row11, 1);
                    String username = user.getUsername() != null ? user.getUsername() : "";
                    String fullName = user.getName() != null ? user.getName() : "";
                    cell.setCellValue("Bên nhận việc: Đ/c " + fullName + " - " + username);
                }

                // Tổng điểm – ví dụ H15
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

            templateWb.close();
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

    /**
     * Sao chép đơn giản template sang sheet mới: giá trị + style + width.
     * Không xử lý công thức phức tạp nhưng đủ dùng cho mẫu phiếu này.
     */
    private static void copySheet(Sheet src, Sheet dest) {
        for (int r = 0; r <= src.getLastRowNum(); r++) {
            Row srcRow = src.getRow(r);
            Row destRow = dest.createRow(r);
            if (srcRow == null) continue;
            for (int c = 0; c < srcRow.getLastCellNum(); c++) {
                Cell srcCell = srcRow.getCell(c);
                Cell destCell = destRow.createCell(c);
                if (srcCell == null) continue;
                copyCell(srcCell, destCell);
            }
            destRow.setHeight(srcRow.getHeight());
        }
        Row firstRow = src.getRow(0);
        if (firstRow != null) {
            for (int c = 0; c < firstRow.getLastCellNum(); c++) {
                dest.setColumnWidth(c, src.getColumnWidth(c));
            }
        }
    }

    private static void copyCell(Cell src, Cell dest) {
        Workbook wb = dest.getSheet().getWorkbook();
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(src.getCellStyle());
        dest.setCellStyle(style);

        switch (src.getCellType()) {
            case STRING -> dest.setCellValue(src.getStringCellValue());
            case NUMERIC -> dest.setCellValue(src.getNumericCellValue());
            case BOOLEAN -> dest.setCellValue(src.getBooleanCellValue());
            case FORMULA -> {
                dest.setCellFormula(src.getCellFormula());
            }
            default -> {
            }
        }
    }
}


package vn.phn.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import vn.phn.dto.TaskDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    /**
     * Xuất danh sách task ra file Excel (định dạng .xlsx).
     */
    public byte[] exportTasksToExcel(List<TaskDto> tasks) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Nhiệm vụ");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = { "Mã", "Tiêu đề", "Trạng thái", "Hạn chót", "Trọng số W", "Chất lượng", "WQT", "Người thực hiện" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (TaskDto t : tasks) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(t.getId() != null ? String.valueOf(t.getId()) : "");
                row.createCell(1).setCellValue(t.getTitle() != null ? t.getTitle() : "");
                row.createCell(2).setCellValue(t.getStatus() != null ? t.getStatus().name() : "");
                row.createCell(3).setCellValue(t.getDeadline() != null ? t.getDeadline().toString() : "");
                row.createCell(4).setCellValue(t.getWeight() != null ? t.getWeight() : 0);
                row.createCell(5).setCellValue(t.getQuality() != null ? t.getQuality() : 0);
                row.createCell(6).setCellValue(t.getWqt() != null ? t.getWqt() : 0);
                row.createCell(7).setCellValue(t.getAssigneeName() != null ? t.getAssigneeName() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }
}

package vn.phn.controller;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.phn.dto.ScoringDto;
import vn.phn.service.EvaluationFormExportService;
import vn.phn.service.ScoringService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/scoring")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://phn-task.onrender.com"})
public class ScoringController {

    private final ScoringService scoringService;
    private final EvaluationFormExportService evaluationFormExportService;

    /**
     * Tính điểm chuyên cần và chất lượng (WQT) cho một user theo tháng.
     * Query optional: ?month=YYYY-MM
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ScoringDto> getUserScore(
            @PathVariable Long userId,
            @RequestParam(required = false) String month) {
        YearMonth ym = parseMonth(month);
        ScoringDto score = scoringService.calculateScore(userId, ym);
        return score != null ? ResponseEntity.ok(score) : ResponseEntity.notFound().build();
    }

    /**
     * Lấy bảng xếp hạng điểm cho tất cả user theo tháng (mỗi tháng reset).
     * Query optional: ?month=YYYY-MM
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<ScoringDto>> getRanking(
            @RequestParam(required = false) String month) {
        YearMonth ym = parseMonth(month);
        return ResponseEntity.ok(scoringService.getRanking(ym));
    }

    /**
     * Xuất phiếu đánh giá cá nhân theo tháng (mỗi nhân viên một sheet) dùng template PHIEU_TEMPLATE.xlsx.
     * Ví dụ: /api/scoring/export-forms?month=2026-02
     */
    @GetMapping("/export-forms")
    public void exportForms(
            @RequestParam String month,
            HttpServletResponse response) throws IOException {
        YearMonth ym = parseMonth(month);
        if (ym == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try (Workbook wb = evaluationFormExportService.buildWorkbookForMonth(ym)) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "PHIEU_DANH_GIA_" + month + ".xlsx";
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            wb.write(response.getOutputStream());
        }
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) return null;
        try {
            return YearMonth.parse(month);
        } catch (Exception e) {
            return null;
        }
    }
}

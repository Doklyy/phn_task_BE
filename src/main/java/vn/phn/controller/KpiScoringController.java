package vn.phn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.phn.dto.EmployeeScore;
import vn.phn.service.KpiScoringService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * API tính điểm KPI từ 3 file CSV (Nhiệm vụ, Báo cáo cuối ngày, Thời gian làm việc).
 * POST /api/kpi-scoring/calculate với 3 file: nhiemVu, baoCao, thoiGian.
 */
@RestController
@RequestMapping("/api/kpi-scoring")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://phn-task.onrender.com"})
public class KpiScoringController {

    private final KpiScoringService kpiScoringService;

    /**
     * Upload 3 file CSV và nhận bảng xếp hạng điểm KPI.
     * Form params: nhiemVu (file), baoCao (file), thoiGian (file).
     */
    @PostMapping(value = "/calculate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> calculateKpi(
            @RequestParam("nhiemVu") MultipartFile nhiemVu,
            @RequestParam("baoCao") MultipartFile baoCao,
            @RequestParam("thoiGian") MultipartFile thoiGian) {
        if (nhiemVu.isEmpty() || baoCao.isEmpty() || thoiGian.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Cần đủ 3 file: nhiemVu, baoCao, thoiGian"));
        }
        try {
            List<EmployeeScore> result = kpiScoringService.tinhToanKPI(
                    nhiemVu.getInputStream(),
                    baoCao.getInputStream(),
                    thoiGian.getInputStream());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Lỗi đọc file: " + e.getMessage()));
        }
    }
}

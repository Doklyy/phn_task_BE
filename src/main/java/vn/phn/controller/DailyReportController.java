package vn.phn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.phn.dto.DailyReportDto;
import vn.phn.dto.ReportComplianceDto;
import vn.phn.dto.SubmitReportRequest;
import vn.phn.entity.Role;
import vn.phn.entity.User;
import vn.phn.service.DailyReportService;
import vn.phn.service.UserService;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class DailyReportController {

    private final DailyReportService reportService;
    private final UserService userService;

    /**
     * Gửi báo cáo kết quả ngày. Ngày giờ hoàn thành = thời điểm gửi (server).
     */
    @PostMapping
    public ResponseEntity<DailyReportDto> submitReport(@Valid @RequestBody SubmitReportRequest req, @RequestParam Long userId) {
        DailyReportDto dto = reportService.submitReport(req, userId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<DailyReportDto>> getReportsByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(reportService.getReportsByUser(userId));
    }

    /**
     * Admin xem toàn bộ báo cáo để theo dõi.
     */
    @GetMapping("/admin")
    public ResponseEntity<List<DailyReportDto>> getAllReportsForAdmin(@RequestParam Long adminId) {
        User admin = userService.getEntity(adminId);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(reportService.getAllReports());
    }

    /**
     * Tổng hợp báo cáo theo tháng: điểm từng người (Admin = tất cả, Leader = nhóm).
     * Query: ?month=2026-02&currentUserId=1
     */
    @GetMapping("/monthly-compliance")
    public ResponseEntity<List<ReportComplianceDto>> getMonthlyCompliance(
            @RequestParam String month,
            @RequestParam Long currentUserId) {
        YearMonth ym;
        try {
            ym = YearMonth.parse(month);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportService.getMonthlyCompliance(ym, currentUserId));
    }
}

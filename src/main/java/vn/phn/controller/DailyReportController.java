package vn.phn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.phn.dto.DailyReportDto;
import vn.phn.dto.ReportComplianceDto;
import vn.phn.dto.ReportReminderDto;
import vn.phn.dto.SubmitReportRequest;
import vn.phn.config.ReportDeadlineConfig;
import vn.phn.entity.Role;
import vn.phn.entity.User;
import vn.phn.service.DailyReportService;
import vn.phn.service.UserService;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://phn-task.onrender.com"})
public class DailyReportController {

    private final DailyReportService reportService;
    private final UserService userService;
    private final ReportDeadlineConfig reportDeadlineConfig;

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
     * Lịch sử báo cáo theo nhiệm vụ (khi xem chi tiết một nhiệm vụ).
     * Trả về danh sách báo cáo theo từng ngày (ngày 1, ngày 2, ngày 3...).
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<DailyReportDto>> getReportsByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(reportService.getReportsByTaskId(taskId));
    }

    /**
     * Kiểm tra nhắc báo cáo: khi mở app, FE gọi API này. Nếu ngày hôm trước chưa báo cáo thì trả về message yêu cầu báo cáo bù.
     */
    @GetMapping("/reminder")
    public ResponseEntity<ReportReminderDto> getReportReminder(@RequestParam Long userId) {
        return ResponseEntity.ok(reportService.getReportReminder(userId));
    }

    /**
     * Cấu hình thời hạn báo cáo (để FE hiển thị: "Báo cáo trong ngày có thể gửi đến 24:00").
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Integer>> getReportConfig() {
        return ResponseEntity.ok(Map.of(
                "deadlineHour", reportDeadlineConfig.getDeadlineHour(),
                "deadlineMinute", reportDeadlineConfig.getDeadlineMinute()));
    }

    /**
     * Lấy toàn bộ báo cáo để tổng hợp bảng Chuyên cần & Điểm cho dashboard.
     * Trước đây chỉ cho ADMIN; hiện cho phép mọi user đã đăng nhập gọi API này,
     * vì frontend chỉ dùng dữ liệu để hiển thị thống kê (không chỉnh sửa báo cáo).
     */
    @GetMapping("/admin")
    public ResponseEntity<List<DailyReportDto>> getAllReportsForAdmin(@RequestParam Long adminId) {
        User user = userService.getEntity(adminId);
        if (user == null) {
            return ResponseEntity.badRequest().build();
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

package vn.phn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.phn.dto.AttendanceRecordDto;
import vn.phn.entity.Role;
import vn.phn.service.AttendanceService;
import vn.phn.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Chấm công: check-in, xem bảng chấm công.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserService userService;

    /**
     * POST /api/attendance/check-in?userId=...
     * Chấm công: ghi nhận giờ hiện tại. Sau 8h sáng = làm muộn.
     */
    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestParam Long userId) {
        AttendanceRecordDto dto = attendanceService.checkIn(userId);
        if (dto == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không chấm được. Kiểm tra user, thứ trong tuần (T2–T6)."));
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/attendance/check-out?userId=...
     * Chấm công ra: ghi nhận giờ hiện tại. Về sớm hơn giờ chuẩn → N_EARLY.
     */
    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(@RequestParam Long userId) {
        AttendanceRecordDto dto = attendanceService.checkOut(userId);
        if (dto == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không chấm được. Kiểm tra user, thứ trong tuần (T2–T6) và đã chấm vào trước đó."));
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/attendance/records?userId=...&from=...&to=...
     * Lấy bản ghi chấm công trong khoảng ngày.
     */
    @GetMapping("/records")
    public ResponseEntity<List<AttendanceRecordDto>> getRecords(
            @RequestParam Long userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDate fromDate = from != null ? LocalDate.parse(from) : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = to != null ? LocalDate.parse(to) : LocalDate.now();
        List<AttendanceRecordDto> list = attendanceService.getRecords(userId, fromDate, toDate);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/attendance/records/month?userId=...&year=...&month=...&currentUserId=...
     * Lấy bản ghi chấm công theo tháng. Admin có thể truyền target userId.
     */
    @GetMapping("/records/month")
    public ResponseEntity<List<AttendanceRecordDto>> getRecordsForMonth(
            @RequestParam Long currentUserId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long targetUserId) {
        Role role = userService.getRole(currentUserId);
        List<AttendanceRecordDto> list = attendanceService.getRecordsForMonth(currentUserId, role, year, month, targetUserId);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/attendance/time-score?userId=...&year=...&month=...
     * Điểm thời gian = (Điểm đạt / (số ngày LV * 8)) * 5
     */
    @GetMapping("/time-score")
    public ResponseEntity<Map<String, Object>> getTimeScore(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        double score = attendanceService.calculateTimeWorkScore(userId, year, month);
        return ResponseEntity.ok(Map.of("userId", userId, "year", year, "month", month, "timeWorkScore", score));
    }
}

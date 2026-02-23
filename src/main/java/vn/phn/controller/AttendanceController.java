package vn.phn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.phn.dto.AttendanceRecordDto;
import vn.phn.entity.Role;
import vn.phn.service.AttendanceService;
import vn.phn.service.UserService;

import vn.phn.entity.AttendanceCode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Chấm công: check-in, xem bảng chấm công.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://phn-task.onrender.com"})
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
     * GET /api/attendance/records?currentUserId=...&userId=...&from=...&to=...
     * Lấy bản ghi chấm công. Chỉ được xem bản ghi của người khác nếu currentUser là ADMIN hoặc có quyền chấm công.
     */
    @GetMapping("/records")
    public ResponseEntity<List<AttendanceRecordDto>> getRecords(
            @RequestParam Long currentUserId,
            @RequestParam Long userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        if (!userId.equals(currentUserId) && !userService.canManageAttendance(currentUserId)) {
            return ResponseEntity.status(403).build();
        }
        LocalDate fromDate = from != null ? LocalDate.parse(from) : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = to != null ? LocalDate.parse(to) : LocalDate.now();
        List<AttendanceRecordDto> list = attendanceService.getRecords(userId, fromDate, toDate);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/attendance/records/month?currentUserId=...&year=...&month=...&targetUserId=...
     * Lấy bản ghi chấm công theo tháng. Admin hoặc người có quyền chấm công có thể truyền targetUserId.
     */
    @GetMapping("/records/month")
    public ResponseEntity<List<AttendanceRecordDto>> getRecordsForMonth(
            @RequestParam Long currentUserId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long targetUserId) {
        Role role = userService.getRole(currentUserId);
        boolean canManage = userService.canManageAttendance(currentUserId);
        List<AttendanceRecordDto> list = attendanceService.getRecordsForMonth(currentUserId, role, canManage, year, month, targetUserId);
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

    /**
     * GET /api/attendance/codes → danh sách mã trạng thái chấm công (code, description) cho dropdown.
     */
    @GetMapping("/codes")
    public ResponseEntity<List<Map<String, String>>> getAttendanceCodes() {
        List<Map<String, String>> codes = Stream.of(AttendanceCode.values())
                .map(c -> {
                    String desc = c.getDescription();
                    return Map.<String, String>of("code", c.name(), "description", desc != null ? desc : c.name());
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(codes);
    }

    /**
     * PATCH /api/attendance/records/{id}?currentUserId=...&checkInAt=08:00&checkOutAt=17:30&attendanceCode=L
     * Cập nhật giờ vào/ra và trạng thái. Chỉ người có quyền chấm công.
     */
    @PatchMapping("/records/{id}")
    public ResponseEntity<?> updateRecord(
            @PathVariable Long id,
            @RequestParam Long currentUserId,
            @RequestParam(required = false) String checkInAt,
            @RequestParam(required = false) String checkOutAt,
            @RequestParam(required = false) String attendanceCode) {
        if (!userService.canManageAttendance(currentUserId)) {
            return ResponseEntity.status(403).build();
        }
        LocalTime in = parseTime(checkInAt);
        LocalTime out = parseTime(checkOutAt);
        AttendanceCode code = parseAttendanceCode(attendanceCode);
        AttendanceRecordDto dto = attendanceService.updateRecord(id, currentUserId, in, out, code);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/attendance/records?currentUserId=...&userId=...&recordDate=2026-02-11&checkInAt=08:00&checkOutAt=17:30&attendanceCode=L
     * Tạo bản ghi chấm công (chỉ người có quyền). Trùng user+ngày thì 400.
     */
    @PostMapping("/records")
    public ResponseEntity<?> createRecord(
            @RequestParam Long currentUserId,
            @RequestParam Long userId,
            @RequestParam String recordDate,
            @RequestParam(required = false) String checkInAt,
            @RequestParam(required = false) String checkOutAt,
            @RequestParam(required = false) String attendanceCode) {
        if (!userService.canManageAttendance(currentUserId)) {
            return ResponseEntity.status(403).build();
        }
        LocalDate date = LocalDate.parse(recordDate);
        LocalTime in = parseTime(checkInAt);
        LocalTime out = parseTime(checkOutAt);
        AttendanceCode code = parseAttendanceCode(attendanceCode);
        if (code == null) code = AttendanceCode.L;
        AttendanceRecordDto dto = attendanceService.createRecord(currentUserId, userId, date, in, out, code);
        if (dto == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Đã tồn tại bản ghi hoặc dữ liệu không hợp lệ."));
        }
        return ResponseEntity.ok(dto);
    }

    private static LocalTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalTime.parse(s.length() == 5 ? s : s.substring(0, 5));
        } catch (Exception e) {
            return null;
        }
    }

    private static AttendanceCode parseAttendanceCode(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return AttendanceCode.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}

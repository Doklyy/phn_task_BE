package vn.phn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.phn.dto.AttendanceRecordDto;
import vn.phn.entity.AttendanceCode;
import vn.phn.entity.AttendanceRecord;
import vn.phn.entity.LeaveRequestStatus;
import vn.phn.entity.LeaveRequestType;
import vn.phn.entity.Role;
import vn.phn.entity.User;
import vn.phn.repository.AttendanceRecordRepository;
import vn.phn.repository.LeaveRequestRepository;
import vn.phn.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Chấm công: check-in, tính điểm, bảng chấm công.
 * Quy chế: L=8, N_full=0, N_half=4, N_late=1, N_early=2, M=-8, V=-8, ...
 * Thứ 2 nghỉ/vắng/đến muộn: trừ thêm 2 điểm.
 * Điểm thời gian = (Điểm đạt / (số ngày LV * 8)) * 5
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private static final LocalTime LATE_THRESHOLD = LocalTime.of(8, 0); // Sau 8h = làm muộn
    private static final LocalTime EARLY_LEAVE_THRESHOLD = LocalTime.of(17, 0); // Trước 17h = về sớm

    private final AttendanceRecordRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    /**
     * Chấm công (check-in). Ghi nhận giờ hiện tại.
     * Nếu sau 8h sáng → isLate = true (trừ khi có đơn xin muộn được duyệt).
     */
    @Transactional
    public AttendanceRecordDto checkIn(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        // Thứ 7, CN: không chấm "làm cả ngày", tạo record đặc biệt nếu cần (TT7, TCN)
        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return null; // Chưa hỗ trợ trực T7/CN qua nút chấm
        }

        AttendanceRecord existing = attendanceRepository.findByUserIdAndRecordDate(userId, today).orElse(null);
        if (existing != null) {
            // Đã chấm rồi → chỉ cập nhật checkIn nếu chưa có
            if (existing.getCheckInAt() == null) {
                boolean hasLatePermission = hasApprovedLateRequest(userId, today);
                boolean isLate = now.isAfter(LATE_THRESHOLD) && !hasLatePermission;
                existing.setCheckInAt(now);
                existing.setIsLate(isLate);
                if (now.isAfter(LATE_THRESHOLD)) {
                    // Có đơn xin muộn đã được duyệt → N_LATE; ngược lại → M (đến muộn bị nhắc nhở)
                    AttendanceCode code = hasLatePermission ? AttendanceCode.N_LATE : AttendanceCode.M;
                    int points = code.getPoints();
                    if (dow == DayOfWeek.MONDAY) {
                        points -= 2; // Thứ 2 đến muộn trừ thêm 2 điểm
                    }
                    existing.setAttendanceCode(code);
                    existing.setPoints(points);
                }
                existing = attendanceRepository.save(existing);
            }
            return toDto(existing);
        }

        boolean hasLatePermission = hasApprovedLateRequest(userId, today);
        boolean isLate = now.isAfter(LATE_THRESHOLD) && !hasLatePermission;
        AttendanceCode code;
        int points;
        if (now.isAfter(LATE_THRESHOLD)) {
            code = hasLatePermission ? AttendanceCode.N_LATE : AttendanceCode.M;
            points = code.getPoints();
            if (dow == DayOfWeek.MONDAY) {
                points -= 2; // Thứ 2 đến muộn trừ thêm 2
            }
        } else {
            code = AttendanceCode.L;
            points = code.getPoints();
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .userId(userId)
                .recordDate(today)
                .attendanceCode(code)
                .points(points)
                .checkInAt(now)
                .isLate(isLate)
                .note(null)
                .build();
        record = attendanceRepository.save(record);
        return toDto(record);
    }

    private boolean hasApprovedLateRequest(Long userId, LocalDate date) {
        return leaveRequestRepository.findByUserIdAndStatus(userId, LeaveRequestStatus.APPROVED).stream()
                .anyMatch(lr -> lr.getType().name().contains("LATE") && !date.isBefore(lr.getFromDate()) && !date.isAfter(lr.getToDate()));
    }

    private boolean hasApprovedEarlyLeaveRequest(Long userId, LocalDate date) {
        return leaveRequestRepository.findByUserIdAndStatus(userId, LeaveRequestStatus.APPROVED).stream()
                .anyMatch(lr -> lr.getType() == LeaveRequestType.EARLY_LEAVE
                        && !date.isBefore(lr.getFromDate()) && !date.isAfter(lr.getToDate()));
    }

    /**
     * Chấm công ra (check-out). Ghi nhận giờ hiện tại.
     * Nếu về sớm hơn 17h và không có đơn xin về sớm được duyệt → mã N_EARLY.
     */
    @Transactional
    public AttendanceRecordDto checkOut(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return null;
        }

        AttendanceRecord record = attendanceRepository.findByUserIdAndRecordDate(userId, today).orElse(null);
        if (record == null) {
            // Chưa check-in mà check-out → không cho chấm
            return null;
        }

        // Đã có giờ ra rồi thì không cập nhật nữa
        if (record.getCheckOutAt() != null) {
            return toDto(record);
        }

        boolean hasEarlyPermission = hasApprovedEarlyLeaveRequest(userId, today);
        boolean isEarly = now.isBefore(EARLY_LEAVE_THRESHOLD) && !hasEarlyPermission;

        record.setCheckOutAt(now);
        record.setIsEarlyLeave(isEarly);

        if (isEarly) {
            AttendanceCode code = AttendanceCode.N_EARLY; // Xin về sớm (mặc định, admin có thể điều chỉnh sau)
            int points = code.getPoints();
            if (dow == DayOfWeek.MONDAY) {
                points -= 2; // Thứ 2 về sớm trừ thêm 2 điểm
            }
            record.setAttendanceCode(code);
            record.setPoints(points);
        }

        record = attendanceRepository.save(record);
        return toDto(record);
    }

    /**
     * Lấy bản ghi chấm công của user trong khoảng thời gian.
     */
    public List<AttendanceRecordDto> getRecords(Long userId, LocalDate from, LocalDate to) {
        List<AttendanceRecord> list = attendanceRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, from, to);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Lấy bản ghi chấm công tháng hiện tại (user hoặc admin xem tất cả).
     */
    public List<AttendanceRecordDto> getRecordsForMonth(Long currentUserId, Role role, int year, int month, Long targetUserId) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());

        Long uid = targetUserId != null ? targetUserId : currentUserId;
        if (role != Role.ADMIN && !uid.equals(currentUserId)) return List.of();

        return getRecords(uid, start, end);
    }

    /**
     * Tính điểm thời gian làm việc tháng: Điểm đạt / (số ngày LV * 8) * 5.
     *
     * - Số ngày LV = số ngày T2–T6 **đã diễn ra** trong tháng (không tính các ngày tương lai).
     *   Ví dụ: hôm nay là 11/02 thì chỉ tính từ 01/02 đến 11/02, không tính các ngày còn lại trong tháng.
     * - Với các tháng đã qua (không phải tháng hiện tại) vẫn tính full tháng.
     * - Nghỉ từ 5 ngày/tháng trở lên (bất kỳ lý do gì, **trừ việc hiếu hỷ**) thì điểm thời gian
     *   bị giới hạn tối đa ở mức "không quá Ki C". Ở đây tạm hiểu Ki C ~ 3.0/5.
     */
    public double calculateTimeWorkScore(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = start.with(TemporalAdjusters.lastDayOfMonth());

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate end;

        // Nếu là tháng hiện tại → chỉ tính đến ngày hôm nay; ngược lại → tính full tháng
        if (today.getYear() == year && today.getMonthValue() == month) {
            end = today.isBefore(lastDayOfMonth) ? today : lastDayOfMonth;
        } else {
            end = lastDayOfMonth;
        }

        List<AttendanceRecord> records =
                attendanceRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, start, end);

        int workingDays = countWorkingDaysInRange(start, end);
        if (workingDays <= 0) return 0;

        int totalPoints = records.stream().mapToInt(AttendanceRecord::getPoints).sum();
        double score = (double) totalPoints / (workingDays * 8) * 5.0;

        // Áp dụng quy tắc: nghỉ >= 5 ngày/tháng (không tính hiếu hỷ) → không quá Ki C (~3 điểm)
        long leaveDays = countLeaveDaysExcludingBereavement(userId, year, month);
        if (leaveDays >= 5) {
            score = Math.min(score, 3.0);
        }
        return score;
    }

    private int countWorkingDaysInRange(LocalDate start, LocalDate end) {
        LocalDate d = start;
        int count = 0;
        while (!d.isAfter(end)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                count++;
            }
            d = d.plusDays(1);
        }
        return count;
    }

    /**
     * Đếm số ngày nghỉ (FULL_DAY, HALF_DAY, LATE_ARRIVAL, EARLY_LEAVE, vắng...) trong tháng,
     * không tính các đơn hiếu hỷ (BEREAVEMENT).
     */
    private long countLeaveDaysExcludingBereavement(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());

        return leaveRequestRepository.findByUserIdAndStatus(userId, LeaveRequestStatus.APPROVED).stream()
                .filter(lr -> lr.getType() != LeaveRequestType.BEREAVEMENT)
                .flatMap(lr -> {
                    LocalDate d = lr.getFromDate();
                    LocalDate to = lr.getToDate();
                    if (to.isBefore(d)) {
                        to = d;
                    }
                    List<LocalDate> dates = new ArrayList<>();
                    while (!d.isAfter(to)) {
                        if (!d.isBefore(start) && !d.isAfter(end)
                                && d.getDayOfWeek() != DayOfWeek.SATURDAY
                                && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                            dates.add(d);
                        }
                        d = d.plusDays(1);
                    }
                    return dates.stream();
                })
                .distinct()
                .count();
    }

    public AttendanceRecordDto toDto(AttendanceRecord r) {
        if (r == null) return null;
        String userName = userRepository.findById(r.getUserId()).map(User::getName).orElse("");
        return AttendanceRecordDto.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .userName(userName)
                .recordDate(r.getRecordDate())
                .attendanceCode(r.getAttendanceCode().name())
                .attendanceCodeDescription(r.getAttendanceCode().getDescription())
                .points(r.getPoints())
                .checkInAt(r.getCheckInAt() != null ? r.getRecordDate().atTime(r.getCheckInAt()).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime() : null)
                .isLate(r.getIsLate())
                .checkOutAt(r.getCheckOutAt() != null ? r.getRecordDate().atTime(r.getCheckOutAt()).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime() : null)
                .isEarlyLeave(r.getIsEarlyLeave())
                .note(r.getNote())
                .build();
    }
}

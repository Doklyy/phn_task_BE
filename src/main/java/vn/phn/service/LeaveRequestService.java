package vn.phn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.phn.dto.LeaveRequestDto;
import vn.phn.entity.AttendanceCode;
import vn.phn.entity.AttendanceRecord;
import vn.phn.entity.LeaveRequest;
import vn.phn.entity.LeaveRequestStatus;
import vn.phn.entity.LeaveRequestType;
import vn.phn.entity.Role;
import vn.phn.entity.User;
import vn.phn.repository.AttendanceRecordRepository;
import vn.phn.repository.LeaveRequestRepository;
import vn.phn.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Đơn xin nghỉ / xin đến muộn / xin về sớm. Admin duyệt.
 */
@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final UserRepository userRepository;

    @Transactional
    public LeaveRequestDto create(Long userId, LeaveRequestType type, LocalDate fromDate, LocalDate toDate,
                                   java.time.LocalTime fromTime, java.time.LocalTime toTime, String reason) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        if (fromDate == null || toDate == null) return null;
        if (toDate.isBefore(fromDate)) toDate = fromDate;
        if (reason == null || reason.isBlank()) return null;
        long days = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (type == LeaveRequestType.BEREAVEMENT && days > 3) return null;

        LeaveRequest req = LeaveRequest.builder()
                .userId(userId)
                .type(type)
                .fromDate(fromDate)
                .toDate(toDate)
                .fromTime(fromTime)
                .toTime(toTime)
                .reason(reason.trim())
                .status(LeaveRequestStatus.PENDING)
                .build();
        req = leaveRequestRepository.save(req);
        return toDto(req);
    }

    /**
     * Admin duyệt đơn → tạo/update AttendanceRecord tương ứng.
     */
    @Transactional
    public LeaveRequestDto approve(Long requestId, Long adminId) {
        LeaveRequest req = leaveRequestRepository.findById(requestId).orElse(null);
        if (req == null) return null;
        if (req.getStatus() != LeaveRequestStatus.PENDING) return null;

        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() != Role.ADMIN) return null;

        req.setStatus(LeaveRequestStatus.APPROVED);
        req.setReviewedBy(adminId);
        req.setReviewedAt(Instant.now());
        req = leaveRequestRepository.save(req);

        // Tạo bản ghi chấm công cho từng ngày trong khoảng (trừ T7, CN)
        AttendanceCode code = mapTypeToCode(req.getType());
        int points = code.getPoints();

        LocalDate d = req.getFromDate();
        while (!d.isAfter(req.getToDate())) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                int dayPoints = points;
                // Nghỉ, Vắng, Đến muộn ngày đầu tuần trừ thêm 2 điểm
                if (d.getDayOfWeek() == DayOfWeek.MONDAY &&
                        (code == AttendanceCode.N_FULL
                                || code == AttendanceCode.N_HALF
                                || code == AttendanceCode.N_LATE
                                || code == AttendanceCode.N_EARLY
                                || code == AttendanceCode.V
                                || code == AttendanceCode.M)) {
                    dayPoints -= 2;
                }
                // Biến dùng trong lambda phải final / effectively final → copy ra biến cục bộ
                final LocalDate currentDate = d;
                final int currentPoints = dayPoints;
                final AttendanceCode currentCode = code;
                final Long userId = req.getUserId();
                final Long leaveRequestId = req.getId();
                final String reason = req.getReason();

                attendanceRepository.findByUserIdAndRecordDate(userId, currentDate).ifPresentOrElse(
                        existing -> {
                            existing.setAttendanceCode(currentCode);
                            existing.setPoints(currentPoints);
                            existing.setLeaveRequestId(leaveRequestId);
                            attendanceRepository.save(existing);
                        },
                        () -> {
                            AttendanceRecord rec = AttendanceRecord.builder()
                                    .userId(userId)
                                    .recordDate(currentDate)
                                    .attendanceCode(currentCode)
                                    .points(currentPoints)
                                    .leaveRequestId(leaveRequestId)
                                    .note(reason)
                                    .build();
                            attendanceRepository.save(rec);
                        }
                );
            }
            d = d.plusDays(1);
        }

        return toDto(req);
    }

    @Transactional
    public LeaveRequestDto reject(Long requestId, Long adminId, String rejectReason) {
        LeaveRequest req = leaveRequestRepository.findById(requestId).orElse(null);
        if (req == null) return null;
        if (req.getStatus() != LeaveRequestStatus.PENDING) return null;

        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() != Role.ADMIN) return null;

        req.setStatus(LeaveRequestStatus.REJECTED);
        req.setReviewedBy(adminId);
        req.setReviewedAt(Instant.now());
        req.setRejectReason(rejectReason != null ? rejectReason.trim() : null);
        req = leaveRequestRepository.save(req);
        return toDto(req);
    }

    private AttendanceCode mapTypeToCode(LeaveRequestType type) {
        return switch (type) {
            case FULL_DAY -> AttendanceCode.N_FULL;
            case HALF_DAY_MORNING, HALF_DAY_AFTERNOON -> AttendanceCode.N_HALF;
            case LATE_ARRIVAL -> AttendanceCode.N_LATE;
            case EARLY_LEAVE -> AttendanceCode.N_EARLY;
            case BEREAVEMENT -> AttendanceCode.L; // Việc hiếu hỷ = đi làm bình thường (tối đa 3 ngày)
        };
    }

    public List<LeaveRequestDto> getByUser(Long userId) {
        return leaveRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, org.springframework.data.domain.PageRequest.of(0, 50))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<LeaveRequestDto> getPendingForAdmin() {
        return leaveRequestRepository.findByStatusOrderByCreatedAtAsc(LeaveRequestStatus.PENDING)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public LeaveRequestDto toDto(LeaveRequest r) {
        if (r == null) return null;
        String userName = userRepository.findById(r.getUserId()).map(User::getName).orElse("");
        return LeaveRequestDto.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .userName(userName)
                .type(r.getType())
                .fromDate(r.getFromDate())
                .toDate(r.getToDate())
                .fromTime(r.getFromTime())
                .toTime(r.getToTime())
                .reason(r.getReason())
                .status(r.getStatus())
                .reviewedBy(r.getReviewedBy())
                .reviewedAt(r.getReviewedAt())
                .rejectReason(r.getRejectReason())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

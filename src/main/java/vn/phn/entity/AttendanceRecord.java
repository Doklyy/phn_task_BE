package vn.phn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Bản ghi chấm công hàng ngày (map 1-1 với bảng attendance_records).
 * Dùng enum AttendanceCode để đồng bộ với AttendanceService.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_code", nullable = false, length = 30)
    private AttendanceCode attendanceCode;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "check_in_at")
    private LocalTime checkInAt;

    @Column(name = "check_out_at")
    private LocalTime checkOutAt;

    @Column(name = "is_late")
    private Boolean isLate;

    @Column(name = "is_early_leave")
    private Boolean isEarlyLeave;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "leave_request_id")
    private Long leaveRequestId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

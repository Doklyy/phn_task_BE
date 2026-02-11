package vn.phn.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Bản ghi chấm công hàng ngày: 1 record / user / ngày.
 * Mã chuyên cần (AttendanceCode) và điểm theo quy chế.
 */
@Entity
@Table(name = "attendance_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "record_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(nullable = false)
    private Integer points;

    /** Giờ chấm công vào (nếu chấm công) */
    @Column(name = "check_in_at")
    private LocalTime checkInAt;

    /** Giờ chấm công ra (nếu chấm công) */
    @Column(name = "check_out_at")
    private LocalTime checkOutAt;

    /** true nếu chấm sau 8h sáng và không có đơn xin muộn được duyệt */
    @Column(name = "is_late")
    private Boolean isLate;

    /** true nếu chấm out sớm hơn giờ kết thúc chuẩn và không có đơn xin về sớm được duyệt */
    @Column(name = "is_early_leave")
    private Boolean isEarlyLeave;

    @Column(length = 1000)
    private String note;

    /** ID đơn xin nghỉ/muộn/về sớm nếu có (được duyệt) */
    @Column(name = "leave_request_id")
    private Long leaveRequestId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}

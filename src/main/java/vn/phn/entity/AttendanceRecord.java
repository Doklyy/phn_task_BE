package vn.phn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Bản ghi chấm công hàng ngày (map 1-1 với bảng attendance_records).
 * Chỉ dùng các trường cần cho tính điểm chuyên cần.
 */
@Getter
@Setter
@NoArgsConstructor
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

    @Column(name = "attendance_code", nullable = false, length = 30)
    private String attendanceCode;

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
}

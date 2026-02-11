package vn.phn.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false, length = 4000)
    private String result;

    /** Trọng số (W) người dùng nhập trong báo cáo (0..1) */
    @Column
    private Double weight;

    /** Mặc định = thời điểm nhấn Gửi */
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

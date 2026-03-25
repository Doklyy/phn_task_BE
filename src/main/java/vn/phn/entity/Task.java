package vn.phn.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 2000)
    private String content;

    @Column(length = 1000)
    private String objective;

    @Column(nullable = false)
    private LocalDateTime deadline;

    /**
     * Trọng số công việc (0..1), dùng tính WQT: WQT = weight * quality
     */
    @Column(nullable = false)
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    /**
     * Điểm chất lượng (0..1) do Leader đánh giá. WQT = weight * quality.
     */
    @Column
    private Double quality;

    @Column(name = "assigner_id", nullable = false)
    private Long assignerId;

    @Column(name = "leader_id", nullable = false)
    private Long leaderId;

    @Column(name = "assignee_id", nullable = false)
    private Long assigneeId;

    /** Đường dẫn file đính kèm (nếu có) */
    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    /** Báo cáo hoàn thành: ghi chú (assignee gửi khi bấm Hoàn thành) */
    @Column(name = "completion_note", length = 4000)
    private String completionNote;

    /** Báo cáo hoàn thành: link (nếu có) */
    @Column(name = "completion_link", length = 1000)
    private String completionLink;

    /** Báo cáo hoàn thành: đường dẫn file đính kèm (nếu có) */
    @Column(name = "completion_file_path", length = 500)
    private String completionFilePath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Thời điểm nhân viên tiếp nhận (NEW → ACCEPTED), hoặc cập nhật khi sếp trả về tồn đọng (→ ACCEPTED).
     * Dùng làm mốc bắt đầu bắt buộc báo cáo tiến độ theo ngày — không dùng {@code createdAt} khi task còn NEW.
     */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Lý do trả về tồn đọng lần gần nhất (Leader/Admin từ chối hoàn thành) */
    @Column(name = "last_reject_reason", length = 2000)
    private String lastRejectReason;

    @Column(name = "last_reject_at")
    private LocalDateTime lastRejectAt;

    /** Xóa mềm (admin): có giá trị thì task ẩn khỏi danh sách thường; hoàn tác = đặt lại null. */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

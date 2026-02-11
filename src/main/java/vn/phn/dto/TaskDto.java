package vn.phn.dto;

import lombok.*;
import vn.phn.entity.TaskStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {
    private Long id;
    private String title;
    private String content;
    private String objective;
    private LocalDateTime deadline;
    private Double weight;
    private TaskStatus status;
    private Double quality;
    private Double wqt; // weight * quality (tính khi có quality)
    private Long assignerId;
    private Long leaderId;
    private Long assigneeId;
    private String leaderName;
    private String assigneeName;
    private String attachmentPath;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    /** Báo cáo hoàn thành (khi status = PENDING_APPROVAL) */
    private String completionNote;
    private String completionLink;
    private String completionFilePath;
}

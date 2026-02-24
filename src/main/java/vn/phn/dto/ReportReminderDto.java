package vn.phn.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Dùng cho tính năng nhắc báo cáo: khi mở app, nếu ngày hôm trước chưa báo cáo thì chặn toàn bộ hệ thống,
 * hiện danh sách nhiệm vụ chưa báo cáo; chỉ khi báo cáo bù xong mới dùng hệ thống bình thường.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportReminderDto {
    /** true nếu ngày hôm trước có công việc nhưng chưa có báo cáo → chặn dùng hệ thống */
    private boolean missingYesterday;
    /** Ngày cần báo cáo bù (ngày hôm trước) */
    private LocalDate yesterday;
    /** Thông báo hiển thị cho người dùng */
    private String message;
    /** Danh sách nhiệm vụ (của user) chưa báo cáo cho ngày yesterday — để hiển thị trên màn hình chặn */
    private List<MissingTaskItem> missingTasks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MissingTaskItem {
        private Long taskId;
        private String taskTitle;
    }
}

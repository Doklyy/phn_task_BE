package vn.phn.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * Dùng cho tính năng nhắc báo cáo: khi mở app, nếu ngày hôm trước chưa báo cáo thì hiện thông báo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportReminderDto {
    /** true nếu ngày hôm trước có công việc nhưng chưa có báo cáo */
    private boolean missingYesterday;
    /** Ngày cần báo cáo bù (ngày hôm trước) */
    private LocalDate yesterday;
    /** Thông báo hiển thị cho người dùng */
    private String message;
}

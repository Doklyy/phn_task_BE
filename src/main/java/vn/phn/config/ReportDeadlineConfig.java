package vn.phn.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Thời hạn báo cáo cuối ngày: có thể gửi báo cáo trong ngày đến giờ này (ví dụ 24 = 24:00, 19 = 19:00).
 */
@Configuration
@Getter
public class ReportDeadlineConfig {

    @Value("${app.report.deadline-hour:24}")
    private int deadlineHour;

    @Value("${app.report.deadline-minute:0}")
    private int deadlineMinute;
}

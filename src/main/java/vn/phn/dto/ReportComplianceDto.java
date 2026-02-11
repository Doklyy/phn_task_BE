package vn.phn.dto;

import lombok.*;

/**
 * Điểm báo cáo tháng: số ngày phải báo cáo (có công việc tồn), đã báo cáo, trễ/thiếu, điểm (+ cộng đủ, - trừ thiếu).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportComplianceDto {
    private Long userId;
    private String userName;
    private String team;
    /** Số ngày trong tháng có công việc tồn (phải báo cáo trước 24h hôm sau) */
    private int requiredDays;
    /** Số ngày đã báo cáo trong tháng */
    private int reportedDays;
    /** Số ngày thiếu báo cáo (required - reported, chỉ tính khi required > 0) */
    private int missedDays;
    /** Điểm: +1 mỗi ngày báo cáo đủ, -2 mỗi ngày thiếu (có thể tùy chỉnh) */
    private int point;
}

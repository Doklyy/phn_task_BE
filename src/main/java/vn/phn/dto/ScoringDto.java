package vn.phn.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringDto {
    private Long userId;
    private String userName;
    private String name;
    /** Điểm chuyên cần: tỷ lệ số ngày báo cáo / tổng số ngày làm việc (0..1) */
    private Double attendanceScore;
    /** Điểm chất lượng WQT trung bình từ các task đã hoàn thành */
    private Double qualityScore;
    /** Tổng điểm = (attendanceScore * 0.4) + (qualityScore * 0.6) */
    private Double totalScore;
    /** Số ngày đã báo cáo */
    private Integer reportedDays;
    /** Tổng số task đã hoàn thành */
    private Integer completedTasks;
}

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
    /** Điểm chất lượng W_Q_T (0..1): (Tổng điểm đạt / Tổng điểm giao) với điểm đạt = W * Q * T */
    private Double qualityScore;
    /** Điểm W_Q_T quy đổi theo điểm chuẩn (ví dụ 15 điểm tối đa cho mục Nhiệm vụ) */
    private Double qualityScoreMax;
    /** Tổng điểm = (attendanceScore * 0.4) + (qualityScore * 0.6) */
    private Double totalScore;
    /** Số ngày đã báo cáo */
    private Integer reportedDays;
    /** Tổng số task đã hoàn thành */
    private Integer completedTasks;
}

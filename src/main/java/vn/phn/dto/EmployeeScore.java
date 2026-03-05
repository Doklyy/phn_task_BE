package vn.phn.dto;

import lombok.*;

/**
 * Kết quả tính điểm KPI từ 3 file CSV (Nhiệm vụ, Báo cáo cuối ngày, Thời gian làm việc).
 * Tổng điểm = Điểm nhiệm vụ (max 15) + Điểm báo cáo (max 5) + Điểm thời gian (max 5 thưởng).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeScore {
    /** Tên nhân viên (Chủ trì / từ cột CSV) */
    private String employeeName;
    /** Tổng điểm KPI (nhiệm vụ + báo cáo + thời gian) */
    private Double totalScore;
    /** Điểm nhiệm vụ (max 15) */
    private Double taskScore;
    /** Điểm báo cáo cuối ngày (max 5) */
    private Double reportScore;
    /** Điểm thời gian làm việc – thưởng (max 5) */
    private Double workTimeScore;
}

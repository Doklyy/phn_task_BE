package vn.phn.entity;

import lombok.Getter;

/**
 * Mã chuyên cần và điểm tương ứng (theo bảng thời gian làm việc).
 * L (Làm cả ngày): 8
 * N_FULL (Nghỉ cả ngày được duyệt): 0
 * N_HALF (Nghỉ nửa ngày): 4
 * N_LATE (Xin đến muộn): 5
 * N_EARLY (Xin về sớm): 5
 * M (Đến muộn bị nhắc nhở): -8
 * V (Vắng chưa được đồng ý): -8
 * L_HOLIDAY (Nghỉ lễ): 0
 * T_HOLIDAY (Trực lễ): 8
 * CN (Nghỉ chủ nhật): 0
 * T7 (Nghỉ thứ 7): 0
 * TT7 (Trực thứ 7 theo vòng): 0
 * TCN (Trực CN theo yêu cầu C/v): 8
 *
 * Quy tắc: Nghỉ, Vắng, Đến muộn ngày đầu tuần (Thứ 2) trừ thêm 2 điểm.
 */
@Getter
public enum AttendanceCode {
    L(8, "Làm cả ngày"),
    N_FULL(0, "Nghỉ cả ngày (được duyệt)"),
    N_HALF(4, "Nghỉ nửa ngày"),
    N_LATE(5, "Xin đến muộn"),
    N_EARLY(5, "Xin về sớm"),
    M(-8, "Đến muộn bị nhắc nhở"),
    V(-8, "Vắng chưa được đồng ý"),
    L_HOLIDAY(0, "Nghỉ lễ"),
    T_HOLIDAY(8, "Trực lễ"),
    CN(0, "Nghỉ chủ nhật"),
    T7(0, "Nghỉ thứ 7"),
    TT7(0, "Trực thứ 7 (theo vòng)"),
    TCN(8, "Trực CN theo yêu cầu công việc");

    private final int points;
    private final String description;

    AttendanceCode(int points, String description) {
        this.points = points;
        this.description = description;
    }
}

package vn.phn.entity;

import lombok.Getter;

/**
 * Mã chuyên cần và điểm (theo bảng quy định).
 * L=8, N_FULL(N)=0, N_HALF(N4)=4, N_LATE(N1)=5, N_EARLY(N2)=5,
 * M=-8, V=-8, L_HOLIDAY(L)=0, T_HOLIDAY(T)=8, CN=0, T7=0, TT7=0, TCN=8.
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

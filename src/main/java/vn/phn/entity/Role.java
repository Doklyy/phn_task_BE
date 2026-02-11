package vn.phn.entity;

/**
 * 3 cấp độ quyền: Admin (toàn quyền), Leader (giao việc, chỉnh sửa, đánh giá), Staff (xem việc, báo cáo).
 */
public enum Role {
    ADMIN,
    LEADER,
    STAFF
}

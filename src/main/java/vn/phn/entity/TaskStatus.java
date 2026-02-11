package vn.phn.entity;

public enum TaskStatus {
    NEW,               // Mới, chưa tiếp nhận
    ACCEPTED,           // Đã tiếp nhận, đang làm
    PENDING_APPROVAL,   // Đã nộp báo cáo hoàn thành, đợi leader (người phân công) duyệt
    COMPLETED           // Đã hoàn thành (admin đã duyệt)
}

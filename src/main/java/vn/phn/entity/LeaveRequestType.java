package vn.phn.entity;

/**
 * Loại đơn xin nghỉ / xin muộn / xin về sớm.
 */
public enum LeaveRequestType {
    /** Nghỉ cả ngày */
    FULL_DAY,
    /** Nghỉ nửa ngày (sáng) */
    HALF_DAY_MORNING,
    /** Nghỉ nửa ngày (chiều) */
    HALF_DAY_AFTERNOON,
    /** Xin đến muộn */
    LATE_ARRIVAL,
    /** Xin về sớm */
    EARLY_LEAVE,
    /** Nghỉ việc hiếu hỷ (bố mẹ đẻ, ông bà, con, vợ chồng, bản thân...) - tối đa 3 ngày = đi làm bình thường */
    BEREAVEMENT
}

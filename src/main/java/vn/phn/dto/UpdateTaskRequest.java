package vn.phn.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Admin/Leader chỉnh sửa thông tin công việc: nội dung, thời hạn, trọng số, trạng thái, chất lượng.
 * Mọi trường đều optional – chỉ cập nhật những field được gửi lên (khác null).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {

    @Size(max = 500)
    private String title;

    @Size(max = 2000)
    private String content;

    @Size(max = 1000)
    private String objective;

    private LocalDateTime deadline;

    @DecimalMin("0")
    @DecimalMax("1")
    private Double weight;

    /** Trạng thái: NEW, ACCEPTED, PENDING_APPROVAL, COMPLETED, PAUSED */
    private String status;

    @DecimalMin("0")
    @DecimalMax("1")
    private Double quality;
}

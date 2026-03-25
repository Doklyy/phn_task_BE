package vn.phn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Trả về sau khi admin xóa mềm nhiệm vụ — FE dùng {@code taskId} để gọi hoàn tác.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSoftDeleteResponseDto {
    private Long taskId;
    private String message;
}

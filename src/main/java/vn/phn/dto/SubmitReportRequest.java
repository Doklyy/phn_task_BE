package vn.phn.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitReportRequest {

    @NotNull
    private Long taskId;

    @NotNull
    private LocalDate reportDate;

    @NotBlank(message = "Kết quả không được để trống")
    @Size(min = 10, max = 4000)
    private String result;

    @DecimalMin("0")
    @DecimalMax("1")
    private Double weight;

    /** Đường dẫn file đính kèm (sau khi upload qua POST /api/upload) */
    private String attachmentPath;
}

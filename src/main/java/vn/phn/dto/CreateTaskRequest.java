package vn.phn.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 500)
    private String title;

    @Size(max = 2000)
    private String content;

    @Size(max = 1000)
    private String objective;

    @NotNull
    private LocalDateTime deadline;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("1")
    private Double weight;

    @NotNull
    private Long leaderId;

    @NotNull
    private Long assigneeId;
}

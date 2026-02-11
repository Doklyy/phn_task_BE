package vn.phn.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReportDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long taskId;
    private String taskTitle;
    private LocalDate reportDate;
    private String result;
    private Double weight;
    private LocalDateTime submittedAt;
    private String attachmentPath;
}

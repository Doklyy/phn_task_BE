package vn.phn.dto;

import lombok.*;
import vn.phn.entity.AttendanceCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecordDto {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDate recordDate;
    private String attendanceCode; // enum name
    private String attendanceCodeDescription;
    private Integer points;
    private LocalDateTime checkInAt;
    private Boolean isLate;       // true nếu chấm sau 8h sáng và không có đơn xin muộn
    private LocalDateTime checkOutAt;
    private Boolean isEarlyLeave; // true nếu chấm out sớm và không có đơn xin về sớm
    private String note;
}

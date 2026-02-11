package vn.phn.dto;

import lombok.*;
import vn.phn.entity.LeaveRequestType;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeaveRequest {
    private LeaveRequestType type;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalTime fromTime;
    private LocalTime toTime;
    private String reason;
}

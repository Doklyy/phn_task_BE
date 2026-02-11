package vn.phn.dto;

import lombok.*;
import vn.phn.entity.LeaveRequestStatus;
import vn.phn.entity.LeaveRequestType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDto {
    private Long id;
    private Long userId;
    private String userName;
    private LeaveRequestType type;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalTime fromTime;
    private LocalTime toTime;
    private String reason;
    private LeaveRequestStatus status;
    private Long reviewedBy;
    private Instant reviewedAt;
    private String rejectReason;
    private Instant createdAt;
}

package vn.phn.dto;

import lombok.*;
import vn.phn.entity.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String name;
    private Role role;
    /** Nhóm: old_product | new_product | null (admin) */
    private String team;
    /** Có quyền chấm công (xem bảng chấm công, chấm công cho người khác). Admin luôn có. */
    private Boolean canManageAttendance;

    /** Getter/setter tường minh để build Docker (Lombok có thể không chạy đủ trong môi trường build). */
    public Boolean getCanManageAttendance() {
        return canManageAttendance;
    }

    public void setCanManageAttendance(Boolean canManageAttendance) {
        this.canManageAttendance = canManageAttendance;
    }
}

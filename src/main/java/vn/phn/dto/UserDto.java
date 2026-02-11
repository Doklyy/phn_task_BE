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
}

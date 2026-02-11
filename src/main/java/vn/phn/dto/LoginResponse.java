package vn.phn.dto;

import lombok.*;
import vn.phn.entity.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long userId;
    private String username;
    private String name;
    private Role role;
    private String token; // Có thể dùng JWT sau này, hiện tại trả userId để FE dùng
    /** Nhóm: old_product | new_product | null */
    private String team;
}

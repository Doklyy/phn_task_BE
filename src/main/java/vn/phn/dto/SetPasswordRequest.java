package vn.phn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetPasswordRequest {

    @NotBlank(message = "Mật khẩu mới không được để trống")
    private String newPassword;
}

package db.shield.auth.service.dto.user;

import db.shield.auth.service.model.constant.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Size(max = 100)
        String username,

        @Email
        @Size(max = 255)
        String email,

        UserRole role,

        Boolean isLocked

) {
}

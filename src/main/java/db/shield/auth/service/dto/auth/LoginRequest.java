package db.shield.auth.service.dto.auth;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record LoginRequest(

        @NotBlank
        @Size(min = 2, max = 25)
        String username,
        @NotBlank
        @Size(min = 8, max = 255)
        String password
) {
}

package db.shield.auth.service.exception;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ErrorResponse {
    private String message;
    private OffsetDateTime time;

    public ErrorResponse(String message) {
        this.message = message;
        this.time = OffsetDateTime.now();
    }
}

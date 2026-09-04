package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRegistrationResponseDTO(
        UUID id,
        String name,
        String surname,
        String email,
        String message,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime time) {
}

package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record UpdateCredentialsResponseDTO(
        String message,

        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime time
) {
}

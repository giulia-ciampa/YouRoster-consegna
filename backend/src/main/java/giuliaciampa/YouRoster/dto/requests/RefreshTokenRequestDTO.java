package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "Il Refresh Token è obbligatorio")
        String refreshToken) {
}

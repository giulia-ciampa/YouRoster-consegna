package giuliaciampa.YouRoster.dto.responses;

public record LoginResponseDTO(

        String accessToken,
        String refreshToken,
        String photoUrl,
        String roleName
) {
}

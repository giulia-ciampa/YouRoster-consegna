package giuliaciampa.YouRoster.dto.responses;

import java.util.UUID;

public record RestShiftResponseDTO(
        UUID id,
        Boolean isRest
) {
}

package giuliaciampa.YouRoster.dto.responses;

import java.util.Set;
import java.util.UUID;

public record UserBasicInformationResponseDTO(UUID userId,
                                              String name,
                                              String surname,
                                              String phoneNumber,
                                              String email,
                                              String officeName,
                                              Set<String> roleNames) {
}

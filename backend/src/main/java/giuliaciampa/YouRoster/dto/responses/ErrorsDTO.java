package giuliaciampa.YouRoster.dto.responses;

import java.time.LocalDateTime;

public record ErrorsDTO(String message, LocalDateTime time) {
    
}

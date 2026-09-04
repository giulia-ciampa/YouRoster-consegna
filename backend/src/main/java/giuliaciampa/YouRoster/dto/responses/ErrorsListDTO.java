package giuliaciampa.YouRoster.dto.responses;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorsListDTO(String message, LocalDateTime timestamp, List<String> errorsList) {
}

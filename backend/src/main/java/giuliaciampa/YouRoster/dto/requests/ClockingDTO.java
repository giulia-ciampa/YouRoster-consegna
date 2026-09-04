package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ClockingDTO(

        @NotNull(message = "È necessario attivare il GPS per registrare la posizione della timbratura")
        BigDecimal latitude,
        BigDecimal longitude,
        String note
) {
}

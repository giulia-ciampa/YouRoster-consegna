package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalTime;

public record OfficeDTO(
        @NotBlank(message = "Il nome dell'ufficio è obbligatorio")
        String name,

        @NotBlank(message = "l'indirizzo dell'ufficio è obbligatorio")
        String street,

        @NotBlank(message = "Il numero civico è obbligatorio")
        String houseNumber,

        @NotBlank(message = "Il CAP è obbligatorio")
        @Pattern(regexp = "^[0-9]{5}$", message = "Il CAP deve essere di 5 cifre")
        String zipCode,

        @NotBlank(message = "La città è obbligatoria")
        String city,

        @NotBlank(message = "La provincia è obbligatoria")
        String province,

        @NotNull(message = "L'orario di apertura è obbligatorio")
        LocalTime openingTime,

        @NotNull(message = "L'orario di chiusura è obbligatorio")
        LocalTime closingTime,

        @NotNull(message = "La latitudine è obbligatoria")
        BigDecimal latitude,

        @NotNull(message = "La latitudine è obbligatoria")
        BigDecimal longitude

) {
}

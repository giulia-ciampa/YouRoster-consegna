package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.DocumentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record UpdateUserProfileDTO(
        String name,
        String surname,
        @Past(message = "La data di nascita non può essere nel futuro")
        LocalDate dateOfBirth,
        String placeOfBirth,
        String taxCode,

        String phoneNumber,

        String streetAddress,
        String houseNumber,

        @Pattern(
                regexp = "^[0-9]{5}$",
                message = "Il CAP deve essere di 5 cifre"
        )
        String zipCode,

        String city,
        String province,

        String iban,

        String documentNumber,
        DocumentType documentType,

        @PastOrPresent(
                message = "La data di emissione del documento non può essere al futuro"
        )
        LocalDate issueDate,

        @Future(message = "Il documento è scaduto!")
        LocalDate expirationDate,

        MultipartFile photoUrl,
        MultipartFile documentFront,
        MultipartFile documentBack,
        MultipartFile taxCodeCardFront,
        MultipartFile taxCodeCardBack
) {
}

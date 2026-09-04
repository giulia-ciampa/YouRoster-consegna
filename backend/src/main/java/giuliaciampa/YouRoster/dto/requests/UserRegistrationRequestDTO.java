package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.DocumentType;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

public record UserRegistrationRequestDTO(
        @NotBlank(message = "Il nome è obbligatorio")
        String name,

        @NotBlank(message = "Il cognome è obbligatorio")
        String surname,


        @NotBlank(message = "Il codice fiscale è obbligatorio")
        @Pattern(
                regexp = "^[A-Z]{6}[0-9]{2}[A-Z]{1}[0-9]{2}[A-Z]{1}[0-9]{3}[A-Z]{1}$",
                message = "Il Codice Fiscale deve essere in formato valido e in lettere MAIUSCOLE (es. ABCDEF80A01H501Z)"
        )
        String taxCode,

        @PastOrPresent(message = "la data di nascita non può essere al futuro")
        @NotNull(message = "La data di nascita è obbligatoria")
        LocalDate dateOfBirth,

        @NotBlank(message = "Il luogo di nascita è obbligatorio")
        String placeOfBirth,

        @NotBlank(message = "La nazionalità è obbligatoria")
        String nationality,

        @NotBlank(message = "Il numero di cellulare è obbligatorio")
        @Pattern(
                regexp = "^\\+\\d{1,3}\\d{4,14}$",
                message = "Inserisci un numero di telefono valido nel formato internazionale (es. +39..., +1..., ecc.)"
        )
        String phoneNumber,

        @NotBlank(message = "L'indirizzo (via/piazza) è obbligatorio")
        String streetAddress,

        @NotBlank(message = "Il numero civico è obbligatorio")
        String houseNumber,

        @NotBlank(message = "Il CAP è obbligatorio")
        @Pattern(regexp = "^[0-9]{5}$", message = "Il CAP deve essere di 5 cifre")
        String zipCode,

        @NotBlank(message = "La città è obbligatoria")
        String city,

        @NotBlank(message = "La provincia è obbligatoria")
        @Size(min = 2, max = 2, message = "La provincia deve essere di 2 lettere")
        String province,

        UUID referenceOfficeId,

        @Pattern(
                regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$",
                message = "Inserire un IBAN valido (formato SEPA)"
        )
        @NotBlank(message = "L'iban è obbligatorio")
        String iban,

        @NotBlank(message = "Il numero di documento è obbligatorio")
        String documentNumber,

        @NotNull(message = "Il tipo di documento è obbligatorio")
        DocumentType documentType,

        @PastOrPresent(message = "la data di emissione del documento non può essere al futuro")
        @NotNull(message = "La data di emissione del documento è obbligatoria")
        LocalDate issueDate,

        @Future(message = "Il documento è scaduto!")
        @NotNull(message = "La data di scadenza del documento è obbligatoria")
        LocalDate expirationDate,

        @NotNull(message = "Il fronte del documento è obbligatorio")
        MultipartFile documentFront,

        @NotNull(message = "Il retro del documento è obbligatorio")
        MultipartFile documentBack,

        @NotNull(message = "Il fronte del codice fiscale è obbligatorio")
        MultipartFile taxCodeFront,

        @NotNull(message = "Il retro del codice fiscale è obbligatorio")
        MultipartFile taxCodeBack,

        @Email(message = "Email non valida")
        @NotBlank(message = "L'indirizzo email è obbligatorio")
        String email,

        @NotBlank(message = "La password è obbligatoria")
        @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{6,}$", message = "La password deve contenere almeno sei caratteri, un numero e una lettera maiuscola")
        String password,

        @NotBlank(message = "La conferma della password è obbligatoria")
        String confirmPassword,

        MultipartFile avatar

) {
}

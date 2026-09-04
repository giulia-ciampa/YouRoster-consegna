package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.AccountStatus;
import giuliaciampa.YouRoster.entities.DocumentType;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponseDTO(
        UUID userId,
        String name,
        String surname,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dateOfBirth,
        String placeOfBirth,
        String phoneNumber,
        String taxCode,
        String photoUrl,
        String streetAddress,
        String houseNumber,
        String zipCode,
        String city,
        String province,
        String iban,
        String documentNumber,
        DocumentType documentType,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate issueDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate expirationDate,
        String documentFrontUrl,
        String documentBackUrl,
        String taxCodeCardFrontUrl,
        String taxCodeCardBackUrl,
        String officeName,
        Set<String> roleNames,
        String email,
        AccountStatus accountStatus,
        UUID officeId,
        UUID accountId
) {
}

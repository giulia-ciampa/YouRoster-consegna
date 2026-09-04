package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.CertificateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record AbsenceCertificationRequestDTO(
        @NotBlank(message = "Il numero di protocollo è obbligatorio")
        String protocolCode,

        @NotNull(message = "inserisci il primo giorno di assenza")
        LocalDate startDate,

        @NotNull(message = "inserisci l'ultimo giorno di assenza")
        LocalDate endDate,

        @NotNull(message = "inserisci la data del rilascio del certificato")
        LocalDate issueDate,

        @NotNull(message = "carica il certificato")
        MultipartFile certificateFile,

        @NotNull(message = "indica il tipo di certificato")
        CertificateType certificateType,

        String employeeNotes
) {
}

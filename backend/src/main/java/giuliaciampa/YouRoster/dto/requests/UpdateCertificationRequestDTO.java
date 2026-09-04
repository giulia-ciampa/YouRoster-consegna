package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.CertificateType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record UpdateCertificationRequestDTO(

        String protocolCode,


        LocalDate startDate,


        LocalDate endDate,


        LocalDate issueDate,


        MultipartFile certificateFile,


        CertificateType certificateType,

        String employeeNotes
) {
}

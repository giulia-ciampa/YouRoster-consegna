package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.AssignmentType;
import giuliaciampa.YouRoster.entities.CertificateType;
import giuliaciampa.YouRoster.entities.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AbsenceCertificationReviewResponseDTO(
        UUID id,
        String protocolCode,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate startDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate endDate,
        Integer totalDays,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate issueDate,
        String certificateUrl,
        CertificateType certificateType,
        RequestStatus requestStatus,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime createdAt,
        String employeeNotes,
        String reviewerNotes,
        List<String> reviewerRole,
        String reviewer,
        AssignmentType assignmentType,
        String employeeName

) {

}

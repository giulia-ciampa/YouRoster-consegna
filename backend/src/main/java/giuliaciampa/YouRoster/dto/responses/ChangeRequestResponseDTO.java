package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.CertificateType;
import giuliaciampa.YouRoster.entities.RequestStatus;
import giuliaciampa.YouRoster.entities.RequestType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ChangeRequestResponseDTO(
        UUID id,
        String employeeName,
        RequestStatus requestStatus,
        RequestType requestType,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime createdAt,
        String employeeNotes,
        String reviewerNotes,
        String reviewerName,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate startDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate endDate,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        BigDecimal totalHours,
        Integer totalDays,
        String protocolCode,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime originalRequestCreatedAt,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate endDateOriginalRequest,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate startDateOriginalRequest,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate originalResponseDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate responseDate,
        String originalRequestEmployeeNotes,
        String originalRequestReviewerNotes,
        Integer originalRequestTotalDays,
        BigDecimal originalRequestTotalHours,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate originalDate,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTimeOriginalRequest,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTimeOriginalRequest,
        CertificateType certificationType

) {
}

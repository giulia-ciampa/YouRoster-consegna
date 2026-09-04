package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.ContractStatus;
import giuliaciampa.YouRoster.entities.ContractType;

import java.time.LocalDate;
import java.util.UUID;

public record ContractResponseDTO(
        UUID id,
        UUID userId,
        String userName,
        String userSurname,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate startDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate endDate,
        ContractType contractType,
        int hoursOfLeaveDuePerYear,
        int daysOfLeaveDuePerYear,
        int workingHoursPerWeek,
        ContractStatus contractStatus
) {
}

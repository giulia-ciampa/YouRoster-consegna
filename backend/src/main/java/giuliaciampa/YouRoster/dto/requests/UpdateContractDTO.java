package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.ContractStatus;
import giuliaciampa.YouRoster.entities.ContractType;

import java.time.LocalDate;

public record UpdateContractDTO(


        LocalDate startDate,

        LocalDate endDate,

        ContractType contractType,


        Integer hoursOfLeaveDuePerYear,


        Integer daysOfLeaveDuePerYear,

        Integer workingHoursPerWeek,


        ContractStatus contractStatus
) {
}

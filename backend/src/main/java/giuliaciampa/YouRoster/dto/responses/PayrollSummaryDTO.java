package giuliaciampa.YouRoster.dto.responses;

import giuliaciampa.YouRoster.entities.ContractStatus;
import giuliaciampa.YouRoster.entities.ContractType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollSummaryDTO(
        UUID userId,
        String name,
        String surname,
        ContractType contractType,
        LocalDate contractStartDate,
        String contractEndDate,
        ContractStatus contractStatus,
        BigDecimal accruedDaysTotal,
        BigDecimal accruedDaysThisMonth,
        BigDecimal usedDays,
        BigDecimal remainingDays,

        BigDecimal accruedHoursTotal,
        BigDecimal accruedHoursThisMonth,
        BigDecimal usedHours,
        BigDecimal remainingHours
) {
}

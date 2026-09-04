package giuliaciampa.YouRoster.dto.responses;

import java.math.BigDecimal;

public record UserSummaryResponseDTO(
        // --- Campi di calcolo ferie ---
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

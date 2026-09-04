package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.responses.UserSummaryResponseDTO;
import giuliaciampa.YouRoster.entities.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class UserSummaryService {
    private final HolidayRequestService holidayRequestService;
    private final LeaveHoursRequestService leaveHoursRequestService;
    private final ContractService contractService;

    public UserSummaryService(HolidayRequestService holidayRequestService, LeaveHoursRequestService leaveHoursRequestService, ContractService contractService) {
        this.holidayRequestService = holidayRequestService;
        this.leaveHoursRequestService = leaveHoursRequestService;
        this.contractService = contractService;
    }

    //---------------------------------------GIORNI----------------------------------------------

    // 1. CALCOLO DEI GIORNI MATURATI DALL'INIZIO DELL'ANNO O DALL'INIZIO DEL CONTRATTO
    public BigDecimal calculateAccruedDays(Contract contract) {
        if (contract == null) return null;

        int totalDaysPerYear = contract.getDaysOfLeaveDuePerYear();
        BigDecimal monthlyRate = BigDecimal.valueOf(totalDaysPerYear)
                .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);

        LocalDate now = LocalDate.now();
        LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);

        LocalDate effectiveStart = contract.getStartDate().isAfter(yearStart)
                ? contract.getStartDate()
                : yearStart;

        if (effectiveStart.isAfter(now)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(
                effectiveStart.withDayOfMonth(1), now.withDayOfMonth(1)) + 1;

        BigDecimal accrued = monthlyRate.multiply(BigDecimal.valueOf(monthsBetween));
        BigDecimal maxDays = BigDecimal.valueOf(totalDaysPerYear);

        // Restituisce il minimo tra quelli maturati e il cap annuale, arrotondato a 2 decimali
        return accrued.min(maxDays).setScale(2, RoundingMode.HALF_UP);
    }

    // 2. CALCOLA I GIORNI MATURATI NEL MESE CORRENTE
    public BigDecimal calculateAccruedDaysThisMonth(Contract contract) {
        if (contract == null) return null;
        return BigDecimal.valueOf(contract.getDaysOfLeaveDuePerYear())
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    // 3. CALCOLA I GIORNI USATI (sommando le richieste con stato APPROVATA)
    public BigDecimal calculateUsedDays(User user) {
        int currentYear = LocalDate.now().getYear();

        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
        LocalDate endOfYear = LocalDate.of(currentYear, 12, 31);

        List<HolidayRequest> approvedHoliday = holidayRequestService.findByEmployeeAndRequestStatusAndStartDateBetween(
                user, RequestStatus.APPROVED, startOfYear, endOfYear);

        double totalUsed = approvedHoliday.stream()
                .mapToDouble(HolidayRequest::getTotalDaysRequested)
                .sum();

        return BigDecimal.valueOf(totalUsed).setScale(2, RoundingMode.HALF_UP);
    }

    // 4. CALCOLA I GIORNI RIMANENTI
    public BigDecimal calculateRemainingDays(Contract contract, User user) {
        BigDecimal accruedTotal = calculateAccruedDays(contract);
        BigDecimal usedDays = calculateUsedDays(user);

        if (accruedTotal == null) return null;

        // Sottrazione sicura con BigDecimal
        return accruedTotal.subtract(usedDays).setScale(2, RoundingMode.HALF_UP);
    }

    //---------------------------------------ORE----------------------------------------------

    //METODO 1: CALCOLO DELLE ORE MATURATE TOTALI
    public BigDecimal calculateAccruedHours(Contract contract) {
        if (contract == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // Supponendo che nel tuo contratto o in una configurazione tu abbia le ore totali spettanti all'anno (es. contract.getHoursOfLeaveDuePerYear() o un valore fisso)
        int totalHoursPerYear = contract.getHoursOfLeaveDuePerYear(); // Assicurati di avere questo campo o gestiscilo in base al CCNL
        BigDecimal monthlyRate = BigDecimal.valueOf(totalHoursPerYear)
                .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);

        LocalDate now = LocalDate.now();
        LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);

        LocalDate effectiveStart = contract.getStartDate().isAfter(yearStart)
                ? contract.getStartDate()
                : yearStart;

        if (effectiveStart.isAfter(now)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(
                effectiveStart.withDayOfMonth(1), now.withDayOfMonth(1)) + 1;

        BigDecimal accrued = monthlyRate.multiply(BigDecimal.valueOf(monthsBetween));
        BigDecimal maxHours = BigDecimal.valueOf(totalHoursPerYear);

        return accrued.min(maxHours).setScale(2, RoundingMode.HALF_UP);
    }

    // 2. ORE MATURATE NEL MESE CORRENTE
    public BigDecimal calculateAccruedHoursThisMonth(Contract contract) {
        if (contract == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        int totalHoursPerYear = contract.getHoursOfLeaveDuePerYear();
        return BigDecimal.valueOf(totalHoursPerYear)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    //3. CALCOLO ORE USATE
    public BigDecimal calculateUsedHours(User user) {
        int currentYear = LocalDate.now().getYear();

        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
        LocalDate endOfYear = LocalDate.of(currentYear, 12, 31);

        List<LeaveHoursRequest> approvedLeaveHours = leaveHoursRequestService.findByEmployeeAndRequestStatusAndDateBetween(
                user, RequestStatus.APPROVED, startOfYear, endOfYear);

        double totalUsed = approvedLeaveHours.stream()
                .mapToDouble(req -> req.getTotalHours().doubleValue())
                .sum();

        return BigDecimal.valueOf(totalUsed).setScale(2, RoundingMode.HALF_UP);
    }

    //4. CALCOLO ORE RIMANENTI
    public BigDecimal calculateRemainingHours(Contract contract, User user) {
        BigDecimal accruedHoursTotal = calculateAccruedHours(contract);
        BigDecimal usedHours = calculateUsedHours(user);

        return accruedHoursTotal.subtract(usedHours).setScale(2, RoundingMode.HALF_UP);
    }

    // METODO FINALE CHE UNISCE TUTTO NEL DTO DI RIEPILOGO
    public UserSummaryResponseDTO getUserLeaveSummary(Account currentAccount) {

        Contract activeContract = contractService.findByUserAndContractStatus(currentAccount.getUser(), ContractStatus.ACTIVE);

        BigDecimal accruedDaysTotal = calculateAccruedDays(activeContract);
        BigDecimal accruedDaysThisMonth = calculateAccruedDaysThisMonth(activeContract);
        BigDecimal usedDays = calculateUsedDays(currentAccount.getUser());
        BigDecimal remainingDays = calculateRemainingDays(activeContract, currentAccount.getUser());

        BigDecimal accruedHoursTotal = calculateAccruedHours(activeContract);
        BigDecimal accruedHoursThisMonth = calculateAccruedHoursThisMonth(activeContract);
        BigDecimal usedHours = calculateUsedHours(currentAccount.getUser());
        BigDecimal remainingHours = calculateRemainingHours(activeContract, currentAccount.getUser());


        return new UserSummaryResponseDTO(
                accruedDaysTotal,
                accruedDaysThisMonth,
                usedDays,
                remainingDays,
                accruedHoursTotal,
                accruedHoursThisMonth,
                usedHours,
                remainingHours
        );
    }
}

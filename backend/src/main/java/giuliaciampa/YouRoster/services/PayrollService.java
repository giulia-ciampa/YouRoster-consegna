package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.responses.PayrollSummaryDTO;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PayrollService {
    private final UserService userService;
    private final ContractService contractService;
    private final HolidayRequestService holidayRequestService;
    private final LeaveHoursRequestService leaveHoursRequestService;

    public PayrollService(UserService userService,
                          ContractService contractService,
                          HolidayRequestService holidayRequestService,
                          LeaveHoursRequestService leaveHoursRequestService) {
        this.userService = userService;
        this.contractService = contractService;
        this.holidayRequestService = holidayRequestService;
        this.leaveHoursRequestService = leaveHoursRequestService;
    }

    // --- GIORNI ---

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

        return accrued.min(maxDays).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateAccruedDaysThisMonth(Contract contract) {
        if (contract == null) return null;
        return BigDecimal.valueOf(contract.getDaysOfLeaveDuePerYear())
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

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

    // --- ORE ---

    public BigDecimal calculateAccruedHours(Contract contract) {
        if (contract == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        int totalHoursPerYear = contract.getHoursOfLeaveDuePerYear();
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

    public BigDecimal calculateAccruedHoursThisMonth(Contract contract) {
        if (contract == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        int totalHoursPerYear = contract.getHoursOfLeaveDuePerYear();
        return BigDecimal.valueOf(totalHoursPerYear)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

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

    // --- RIEPILOGO COMPLETO PER IL PAYROLL SPECIALIST ---

    public PayrollSummaryDTO getUserLeaveSummary(UUID userId) {
        User user = userService.findById(userId);
        Contract activeContract = contractService.findByUserAndContractStatus(user, ContractStatus.ACTIVE);

        if (activeContract == null) {
            throw new BadRequestException("Nessun contratto attivo trovato per l'utente selezionato");
        }

        // Calcoli Giorni
        BigDecimal accruedDaysTotal = calculateAccruedDays(activeContract);
        BigDecimal accruedDaysThisMonth = calculateAccruedDaysThisMonth(activeContract);
        BigDecimal usedDays = calculateUsedDays(user);
        BigDecimal remainingDays = accruedDaysTotal != null ? accruedDaysTotal.subtract(usedDays).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // Calcoli Ore
        BigDecimal accruedHoursTotal = calculateAccruedHours(activeContract);
        BigDecimal accruedHoursThisMonth = calculateAccruedHoursThisMonth(activeContract);
        BigDecimal usedHours = calculateUsedHours(user);
        BigDecimal remainingHours = accruedHoursTotal.subtract(usedHours).setScale(2, RoundingMode.HALF_UP);


        return new PayrollSummaryDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                activeContract.getContractType(),
                activeContract.getStartDate(),
                activeContract.getEndDate() != null ? activeContract.getEndDate().toString() : "Indeterminato",
                activeContract.getContractStatus(),
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

    public Page<PayrollSummaryDTO> getAllUsersLeaveSummary(Pageable pageable) {

        Page<Contract> activeContracts = contractService.findByContractStatus(ContractStatus.ACTIVE, pageable);

        if (activeContracts == null) {
            throw new BadRequestException("Nessun contratto attivo trovato per l'utente selezionato");
        }

        List<User> users = userService.findAll();

        return activeContracts.map(contract -> {
            User user = contract.getUser();
            // Calcoli Giorni
            BigDecimal accruedDaysTotal = calculateAccruedDays(contract);
            BigDecimal accruedDaysThisMonth = calculateAccruedDaysThisMonth(contract);
            BigDecimal usedDays = calculateUsedDays(user);
            BigDecimal remainingDays = accruedDaysTotal != null ? accruedDaysTotal.subtract(usedDays).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            // Calcoli Ore
            BigDecimal accruedHoursTotal = calculateAccruedHours(contract);
            BigDecimal accruedHoursThisMonth = calculateAccruedHoursThisMonth(contract);
            BigDecimal usedHours = calculateUsedHours(user);
            BigDecimal remainingHours = accruedHoursTotal.subtract(usedHours).setScale(2, RoundingMode.HALF_UP);

            return new PayrollSummaryDTO(
                    user.getId(),
                    user.getName(),
                    user.getSurname(),
                    contract.getContractType(),
                    contract.getStartDate(),
                    contract.getEndDate() != null ? contract.getEndDate().toString() : "Indeterminato",
                    contract.getContractStatus(),
                    accruedDaysTotal,
                    accruedDaysThisMonth,
                    usedDays,
                    remainingDays,
                    accruedHoursTotal,
                    accruedHoursThisMonth,
                    usedHours,
                    remainingHours
            );
        });
    }
}

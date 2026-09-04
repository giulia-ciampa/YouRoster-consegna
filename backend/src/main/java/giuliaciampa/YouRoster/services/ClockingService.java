package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.ClockingDTO;
import giuliaciampa.YouRoster.dto.requests.ManualClockingAdminDTO;
import giuliaciampa.YouRoster.dto.responses.ClockingResponseDTO;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.repositories.ClockingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
public class ClockingService {

    private final ClockingRepository clockingRepository;
    private final ShiftAssignmentService shiftAssignmentService;

    public ClockingService(ClockingRepository clockingRepository, ShiftAssignmentService shiftAssignmentService) {
        this.clockingRepository = clockingRepository;
        this.shiftAssignmentService = shiftAssignmentService;
    }

    //METODO HELPER PER CALCOLARE LA DISTANZA
    private boolean checkGeoDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Raggio della terra in metri
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return distance <= 50.0;
    }

    //HELPER PER LA RISPOSTA
    private ClockingResponseDTO mapToResponseDTO(Clocking clocking) {

        int workedMinutes = clocking.getWorkedMinutes();

        int hours = workedMinutes / 60;
        int minutes = workedMinutes % 60;

        String workedHours = hours + "h " + minutes + "m";

        return new ClockingResponseDTO(clocking.getId(), clocking.getShiftAssignment().getId(), clocking.getOffice().getName(), clocking.getActualStartTime(), clocking.getActualEndTime(), clocking.getShiftAssignment().getShift().getStartTime(), clocking.getShiftAssignment().getShift().getEndTime(), clocking.getAttendanceStatus(), clocking.getLatitude(), clocking.getLongitude(), clocking.isPositionValid(), clocking.getNote(), clocking.getLateMinutes(), workedMinutes, workedHours, clocking.getEarlyDepartureMinutes(), clocking.getBalanceMinutes());
    }


    //1. METODO PER TIMBRARE L'INGRESSO (Clock-In)
    public ClockingResponseDTO createClockIn(ClockingDTO payload, Account currentAccount) {

        // 1. Ricaviamo lo User dall'Account autenticato
        User user = currentAccount.getUser();
        LocalDate today = LocalDate.now();

        // 2. Cerchiamo l'assegnazione del turno per oggi per questo utente
        ShiftAssignment assignment =
                shiftAssignmentService.findByUserAndShiftDate(user, today);

        // 3. Controllo di sicurezza confrontando lo User ID con lo User ID dell'account loggato
        if (!assignment.getUser().getId().equals(currentAccount.getUser().getId())) {
            throw new UnauthorizedException(
                    "Non sei autorizzato a timbrare per il turno di un altro utente!"
            );
        }

        // 4. Controllo dell'orario di timbratura
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime scheduledStart = assignment.getShift().getStartTime();

        // La timbratura è consentita al massimo 10 minuti prima dell'inizio del turno
        LocalTime earliestClockIn = scheduledStart.minusMinutes(10);

        if (now.isBefore(earliestClockIn)) {
            throw new BadRequestException(
                    "Non puoi ancora timbrare. La timbratura sarà possibile a partire dalle "
                            + earliestClockIn
            );
        }

        // 5. Controllo doppioni
        clockingRepository.findByShiftAssignment(assignment).ifPresent(c -> {
            throw new BadRequestException(
                    "Hai già effettuato la timbratura per questo turno!"
            );
        });

        // 6. Recupero l'ufficio
        Office office = assignment.getShift().getOffice();

        // 7. Controllo di geolocalizzazione usando i dati del DTO
        boolean isValidPosition = checkGeoDistance(
                payload.latitude().doubleValue(),
                payload.longitude().doubleValue(),
                office.getLatitude().doubleValue(),
                office.getLongitude().doubleValue()
        );

        System.out.println(
                "Payload Lat: " + payload.latitude()
                        + " | Office Lat: " + office.getLatitude()
        );

        System.out.println(
                "Payload Lon: " + payload.longitude()
                        + " | Office Lon: " + office.getLongitude()
        );

        if (!isValidPosition) {
            throw new BadRequestException(
                    "Impossibile timbrare: ti trovi troppo distante dalla sede di lavoro!"
            );
        }

        // 8. Creo la nuova timbratura
        Clocking clocking = new Clocking();
        clocking.setShiftAssignment(assignment);
        clocking.setOffice(office);
        clocking.setActualStartTime(now);
        clocking.setLatitude(payload.latitude());
        clocking.setLongitude(payload.longitude());
        clocking.setPositionValid(true);
        clocking.setNote(payload.note());

        // 9. Calcolo lo stato
        LocalTime scheduledEnd = assignment.getShift().getEndTime();

        clocking.calculateStatus(scheduledStart, scheduledEnd);

        // 10. Salvataggio
        clockingRepository.save(clocking);

        return mapToResponseDTO(clocking);
    }

    // 2. METODO PER TIMBRARE L'USCITA (Clock-Out)
    public ClockingResponseDTO createClockOut(ClockingDTO payload, Account currentAccount) {

        // 1. Ricaviamo lo User dall'Account autenticato
        User user = currentAccount.getUser();
        LocalDate today = LocalDate.now();

        // 2. Cerchiamo l'assegnazione del turno per oggi per questo utente
        ShiftAssignment assignment = shiftAssignmentService.findByUserAndShiftDate(user, today);

        //3. Controllo di sicurezza confrontando lo User ID con lo User ID dell'account loggato
        if (!assignment.getUser().getId().equals(currentAccount.getUser().getId())) {
            throw new UnauthorizedException("Non sei autorizzato a timbrare per il turno di un altro utente!");
        }

        // 4. RECUPERO LA TIMBRATURA ESISTENTE (quella creata con il Clock-In)
        Clocking clocking = clockingRepository.findByShiftAssignment(assignment)
                .orElseThrow(() -> new NotFoundException("Nessuna timbratura di ingresso trovata per questo turno!"));

        //5. Controllo che non sia già stato effettuato un clock-out
        if (clocking.getActualEndTime() != null) {
            throw new BadRequestException("Hai già effettuato la timbratura di uscita per questo turno!");
        }

        // 6. Controllo geolocalizzazione (anche in uscita)
        Office office = assignment.getShift().getOffice();
        boolean isValidPosition = checkGeoDistance(
                payload.latitude().doubleValue(), payload.longitude().doubleValue(),
                office.getLatitude().doubleValue(), office.getLongitude().doubleValue()
        );

        if (!isValidPosition) {
            throw new BadRequestException("Impossibile timbrare l'uscita: ti trovi troppo distante dalla sede di lavoro!");
        }

        // 7. aggiorno il clocking
        clocking.setActualEndTime(LocalTime.now().withSecond(0).withNano(0));

        //8. Possiamo aggiornare la nota se l'utente ne aggiunge una nuova in uscita
        if (payload.note() != null && !payload.note().isBlank()) {

            String existingNote = clocking.getNote();

            if (existingNote != null && !existingNote.isBlank()) {
                clocking.setNote(
                        existingNote + " | Uscita: " + payload.note()
                );
            } else {
                clocking.setNote("Uscita: " + payload.note());
            }
        }

        // 9. Calcolo lo stato e i minuti
        LocalTime scheduledStart = assignment.getShift().getStartTime();
        LocalTime scheduledEnd = assignment.getShift().getEndTime();

        clocking.calculateStatus(scheduledStart, scheduledEnd);
        clocking.calculateMinutes(scheduledStart, scheduledEnd);

        // 10. Salvataggio
        clockingRepository.save(clocking);

        return mapToResponseDTO(clocking);
    }

    //3. METODO GET TIMBRATURE(PER UFFICIO, PER STATO, IN UNA FASCIA DI TEMPO)
    public Page<ClockingResponseDTO> getFilteredClockings(String officeName, LocalDate startDate, LocalDate endDate, AttendanceStatus status, Pageable pageable) {
        Page<Clocking> clockings = clockingRepository.findFilteredClockings(officeName, status, startDate, endDate, pageable);

        return clockings
                .map(this::mapToResponseDTO);

    }

    //4. METODO GET PROPRIE TIMBRATURE
    public Page<ClockingResponseDTO> getMyClockings(Account currentAccount, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // Navighiamo da Account -> User -> Id
        UUID userId = currentAccount.getUser().getId();

        Page<Clocking> clockingsPage = clockingRepository.findByShiftAssignment_User_IdAndShiftAssignment_ShiftDateBetween(
                userId, startDate, endDate, pageable
        );

        return clockingsPage.map(this::mapToResponseDTO);
    }

    //5. METODO GET TIMBRATURE DI UNO SPECIFICO USER
    public Page<ClockingResponseDTO> getClockingsBySearch(String name, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<Clocking> clockingsPage = clockingRepository.findClockingsBySearch(
                name, startDate, endDate, pageable
        );

        return clockingsPage.map(this::mapToResponseDTO);
    }

    //6. MODIFICA O CORREZIONE MANUALE
    public ClockingResponseDTO manualOverrideClocking(ManualClockingAdminDTO payload) {
        // 1. L'admin individua il turno grazie all'ID dell'assegnazione
        ShiftAssignment assignment = shiftAssignmentService.findById(payload.shiftAssignmentId());

        // 2. Cerca se esiste già una timbratura per questo turno, altrimenti la crea
        Clocking clocking = clockingRepository.findByShiftAssignmentId(payload.shiftAssignmentId())
                .orElse(new Clocking());

        if (clocking.getShiftAssignment() == null) {
            clocking.setShiftAssignment(assignment);
        }

        // 3. Aggiorna solo i campi valorizzati (supporto PATCH sicuro)
        if (payload.checkInTime() != null) {
            clocking.setActualStartTime(payload.checkInTime().withSecond(0).withNano(0));
        }

        if (payload.checkOutTime() != null) {
            clocking.setActualEndTime(payload.checkOutTime().withSecond(0).withNano(0));
        }

        if (payload.attendanceStatus() != null) {
            clocking.setAttendanceStatus(payload.attendanceStatus());
        }

        if (payload.notesFromAdmin() != null) {
            clocking.setNote(payload.notesFromAdmin());
        }

        clockingRepository.save(clocking);

        return mapToResponseDTO(clocking);
    }

    //7 GET CLOCKING BY USER
    public Page<ClockingResponseDTO> getClockingsByUser(UUID userId, LocalDate date, Pageable pageable) {
        Page<Clocking> clockingsPage;
        if (date != null) {
            clockingsPage = clockingRepository.findByUserIdAndDate(userId, date, pageable);
        } else {
            clockingsPage = clockingRepository.findByUserId(userId, pageable);
        }
        return clockingsPage.map(this::mapToResponseDTO);
    }

    //8. GET TIMBRATURA SINGOLO GIORNO
    public ClockingResponseDTO getMyClockingForDate(
            Account currentAccount,
            LocalDate date
    ) {
        UUID userId = currentAccount.getUser().getId();

        return clockingRepository
                .findByShiftAssignment_User_IdAndShiftAssignment_ShiftDate(userId, date)
                .map(this::mapToResponseDTO)
                .orElse(null);
    }
}

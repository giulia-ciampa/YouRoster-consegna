package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.ShiftCreateDTO;
import giuliaciampa.YouRoster.dto.requests.ShiftUpdateDTO;
import giuliaciampa.YouRoster.dto.responses.ShiftResponseDTO;
import giuliaciampa.YouRoster.entities.Office;
import giuliaciampa.YouRoster.entities.Shift;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.repositories.ShiftRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class ShiftService {
    private final ShiftRepository shiftRepository;
    private final OfficeService officeService;

    public ShiftService(ShiftRepository shiftRepository, OfficeService officeService) {
        this.shiftRepository = shiftRepository;
        this.officeService = officeService;
    }

    //SAVE
    public Shift save(Shift shift) {
        return shiftRepository.save(shift);
    }

    //FIND BY ID
    public Shift findById(UUID id) {
        return shiftRepository.findById(id).orElseThrow(() -> new NotFoundException("Il turno con id " + id + " non è stato trovato"));
    }

    //1. CREA NUOVI TURNI(ADMIN, SHIFT MANAGER)
    public ShiftResponseDTO saveNewShift(ShiftCreateDTO payload) {

        if (payload.officeName() == null) {
            throw new BadRequestException("Il turno deve essere associato a un ufficio.");
        }


        Office office = officeService.findByName(payload.officeName());

        if (payload.startTime().isBefore(office.getOpeningTime())) {
            throw new BadRequestException("Il turno non può iniziare prima dell'orario di apertura dell'ufficio");
        }

        if (payload.endTime().isAfter(office.getClosingTime())) {
            throw new BadRequestException("Il turno non può finire dopo l'orario di chiusura dell'ufficio");
        }


        boolean exists = shiftRepository.existsByOfficeNameAndStartTimeAndEndTimeAndIsActiveTrue(
                office.getName(), payload.startTime(), payload.endTime()
        );

        if (exists) {
            throw new BadRequestException("Esiste già un turno identico per questa sede.");
        }


        Shift newShift = new Shift();

        newShift.setOffice(office);
        newShift.setStartTime(payload.startTime());
        newShift.setEndTime(payload.endTime());


        Shift savedShift = shiftRepository.save(newShift);

        return new ShiftResponseDTO(
                savedShift.getId(),
                savedShift.getOffice().getName(),
                savedShift.getStartTime(),
                savedShift.getEndTime(),
                savedShift.isActive());

    }

    //2. MODIFICA TURNO(ADMIN, SHIFT MANAGER)
    public ShiftResponseDTO updateShift(UUID shiftId, ShiftUpdateDTO payload) {
        // 1. Cerco il turno esistente
        Shift existingShift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new NotFoundException("Il turno con id " + shiftId + " non esiste."));


        // Gestione dell'ufficio: se viene passato nel payload lo cerco, altrimenti tengo quello vecchio
        Office office = existingShift.getOffice();
        if (payload.officeName() != null) {
            if (payload.officeName().trim().isEmpty()) {
                throw new BadRequestException("L'ufficio non può essere vuoto per un turno di lavoro.");
            }
            office = officeService.findByName(payload.officeName());
        }

        // se nel payload viene inserito il campo lo uso, altrimenti tengo il vecchio
        Office targetOffice = office;
        LocalTime targetStart = (payload.startTime() != null) ? payload.startTime() : existingShift.getStartTime();
        LocalTime targetEnd = (payload.endTime() != null) ? payload.endTime() : existingShift.getEndTime();


        // 2. Controllo duplicati (solo se non è un riposo)

        boolean exists = shiftRepository.existsByOfficeNameAndStartTimeAndEndTimeAndIsActiveTrue(
                targetOffice.getName(), targetStart, targetEnd
        );

        // Verifichiamo se il turno "esistente" trovato nel DB è proprio quello che stiamo modificando
        // oppure un altro turno diverso.
        boolean isSameShift = existingShift.getOffice() != null &&
                existingShift.getOffice().getId().equals(targetOffice.getId()) &&
                Objects.equals(existingShift.getStartTime(), targetStart) &&
                Objects.equals(existingShift.getEndTime(), targetEnd);

        if (exists && !isSameShift) {
            throw new BadRequestException("Esiste già un turno identico per questa sede.");

        }

        // 3. Aggiorno i campi con i dati del payload
        if (payload.officeName() != null) {
            existingShift.setOffice(office);
        }
        if (payload.startTime() != null) {
            existingShift.setStartTime(payload.startTime());
        }
        if (payload.endTime() != null) {
            existingShift.setEndTime(payload.endTime());
        }

        if (payload.isActive() != null) {
            existingShift.setActive(payload.isActive());
        }


        // 4. Salvo e restituisco il turno aggiornato
        Shift savedShift = shiftRepository.save(existingShift);

        return new ShiftResponseDTO(savedShift.getId(), savedShift.getOffice().getName(), savedShift.getStartTime(), savedShift.getEndTime(), savedShift.isActive());
    }

    //3. DISATTIVA TURNO (Soft Delete - ADMIN, SHIFT MANAGER)
    public ShiftResponseDTO deactivateShift(UUID shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new NotFoundException("Il turno con id " + shiftId + " non esiste."));


        shift.setActive(false);
        Shift savedShift = shiftRepository.save(shift);

        return new ShiftResponseDTO(
                savedShift.getId(),
                savedShift.getOffice().getName(),
                savedShift.getStartTime(),
                savedShift.getEndTime(),
                savedShift.isActive());
    }

    //4. TROVA TURNI (TUTTI O FILTRATI PER STATO)
    public Page<ShiftResponseDTO> findShifts(Boolean isActive, Pageable pageable) {
        Page<Shift> shiftsPage;

        if (isActive == null) {
            shiftsPage = shiftRepository.findAll(pageable);
        } else if (isActive) {
            shiftsPage = shiftRepository.findByIsActiveTrue(pageable);
        } else {
            shiftsPage = shiftRepository.findByIsActiveFalse(pageable);
        }

        return shiftsPage
                .map(shift -> new ShiftResponseDTO(
                        shift.getId(),
                        shift.getOffice().getName(),
                        shift.getStartTime(),
                        shift.getEndTime(),
                        shift.isActive()
                ));

    }

    //5. RIATTIVAZIONE TURNO DISATTIVATO
    public ShiftResponseDTO reactivateShift(UUID shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new NotFoundException("Il turno con id " + shiftId + " non esiste."));


        // Controllo duplicati attivo (per i turni normali)
        if (shift.getOffice() != null && shiftRepository.existsByOfficeNameAndStartTimeAndEndTimeAndIsActiveTrue(
                shift.getOffice().getName(), shift.getStartTime(), shift.getEndTime())) {
            throw new BadRequestException("Non puoi riattivare questo turno perché ne esiste già uno attivo con gli stessi parametri.");
        }

        shift.setActive(true);
        Shift reactivatedShift = shiftRepository.save(shift);

        return new ShiftResponseDTO(
                reactivatedShift.getId(),
                reactivatedShift.getOffice().getName(),
                reactivatedShift.getStartTime(),
                reactivatedShift.getEndTime(),
                reactivatedShift.isActive());
    }

    
}

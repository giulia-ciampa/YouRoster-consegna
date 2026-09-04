package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.ShiftTaskTitleRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ShiftTaskTitleUpdateRequestDTO;
import giuliaciampa.YouRoster.dto.responses.ShiftTaskTitleResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.Office;
import giuliaciampa.YouRoster.entities.ShiftTaskTitle;
import giuliaciampa.YouRoster.exceptions.AlreadyExistsException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.repositories.ShiftTaskTitleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ShiftTaskTitleService {
    private final ShiftTaskTitleRepository shiftTaskTitleRepository;
    private final OfficeService officeService;

    public ShiftTaskTitleService(ShiftTaskTitleRepository shiftTaskTitleRepository, OfficeService officeService) {
        this.shiftTaskTitleRepository = shiftTaskTitleRepository;
        this.officeService = officeService;
    }

    // helper risposta
    private ShiftTaskTitleResponseDTO mapToResponse(ShiftTaskTitle title) {
        return new ShiftTaskTitleResponseDTO(
                title.getId(),
                title.getTitle(),
                title.getDescription(),
                title.getOffice().getName(),
                title.isActive()
        );
    }

    // helper verifica i permessi sull'ufficio
    private void validateUserOfficeAccess(Account currentAccount, Office targetOffice) {
        boolean isAdmin = currentAccount.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ADMIN"));

        if (!isAdmin) {
            if (currentAccount.getUser().getReferenceOffice() == null || !currentAccount.getUser().getReferenceOffice().getId().equals(targetOffice.getId())) {
                throw new UnauthorizedException("Non hai i permessi per gestire mansioni di un ufficio diverso dal tuo!");
            }
        }
    }

    //findbyid
    public ShiftTaskTitle findById(UUID id) {
        return shiftTaskTitleRepository.findById(id).orElseThrow(() -> new NotFoundException("Il titolo della mansione non è stato trovato"));
    }

    // 1. CREA NUOVO TITOLO
    public ShiftTaskTitleResponseDTO saveNewTaskTitle(ShiftTaskTitleRequestDTO payload, Account currentAccount) {
        Office office = officeService.findById(payload.officeId());

        // Controlla permessi basati sull'ufficio di destinazione
        validateUserOfficeAccess(currentAccount, office);

        if (shiftTaskTitleRepository.existsByTitle(payload.title())) {
            throw new AlreadyExistsException("Il titolo della mansione che si vuole aggiungere esiste già");
        }

        ShiftTaskTitle shiftTaskTitle = new ShiftTaskTitle();
        shiftTaskTitle.setTitle(payload.title().toUpperCase());
        shiftTaskTitle.setDescription(payload.description());
        shiftTaskTitle.setOffice(office);

        ShiftTaskTitle savedShiftTitle = shiftTaskTitleRepository.save(shiftTaskTitle);

        return mapToResponse(savedShiftTitle);
    }

    // 2. MODIFICA TITOLO
    public ShiftTaskTitleResponseDTO updateTaskTitle(UUID id, ShiftTaskTitleUpdateRequestDTO payload, Account currentAccount) {
        ShiftTaskTitle foundedShiftTaskTitle = shiftTaskTitleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Il titolo della mansione non è stato trovato"));

        // Controlla che l'utente possa agire sull'ufficio a cui appartiene questo titolo
        validateUserOfficeAccess(currentAccount, foundedShiftTaskTitle.getOffice());

        if (payload.title() != null) {
            if (!foundedShiftTaskTitle.getTitle().equals(payload.title().toUpperCase()) && shiftTaskTitleRepository.existsByTitle(payload.title())) {
                throw new AlreadyExistsException("Esiste già un titolo della mansione con questo nome.");
            }
            foundedShiftTaskTitle.setTitle(payload.title().toUpperCase());
        }

        if (payload.description() != null) {
            foundedShiftTaskTitle.setDescription(payload.description());
        }

        ShiftTaskTitle updatedTaskTitle = shiftTaskTitleRepository.save(foundedShiftTaskTitle);

        return mapToResponse(updatedTaskTitle);
    }

    // 3. DISATTIVA (SOFT DELETE)
    public ShiftTaskTitleResponseDTO deactivateTaskTitle(UUID id, Account currentAccount) {
        ShiftTaskTitle founded = shiftTaskTitleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Il titolo della mansione non è stato trovato"));

        validateUserOfficeAccess(currentAccount, founded.getOffice());

        founded.setActive(false);
        ShiftTaskTitle updated = shiftTaskTitleRepository.save(founded);

        return mapToResponse(updated);
    }

    // 4. RIATTIVA
    public ShiftTaskTitleResponseDTO reactivateTaskTitle(UUID id, Account currentAccount) {
        ShiftTaskTitle founded = shiftTaskTitleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Il titolo della mansione non è stato trovato"));

        validateUserOfficeAccess(currentAccount, founded.getOffice());

        founded.setActive(true);
        ShiftTaskTitle updated = shiftTaskTitleRepository.save(founded);

        return mapToResponse(updated);
    }

    //5 GET ALL PER UFFICIO (ATTIVI)
    public List<ShiftTaskTitleResponseDTO> GetAllActiveTitleByOffice(Account currentAccount) {

        List<ShiftTaskTitle> titles;

        boolean isAdmin = currentAccount.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (isAdmin) {
            // Admin vede tutto
            titles = shiftTaskTitleRepository.findAllByIsActiveTrue();
        } else {
            // Coordinatore vede solo il suo ufficio
            titles = shiftTaskTitleRepository.findByOfficeIdAndIsActiveTrue(currentAccount.getUser().getReferenceOffice().getId());
        }

        return titles.stream().map(this::mapToResponse).toList();
    }


    //6 PERMETTERE LA RICERCA DIGITANDO IL TITOLO
    public List<ShiftTaskTitleResponseDTO> searchActiveTitles(String query, Account currentAccount) {
        List<ShiftTaskTitle> titles;

        boolean isAdmin = currentAccount.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ADMIN"));

        if (isAdmin) {
            // L'admin cerca su tutti i titoli attivi
            titles = shiftTaskTitleRepository.findByTitleContainingIgnoreCaseAndIsActiveTrue(query);
        } else {
            // Il coordinatore cerca solo all'interno del proprio ufficio
            if (currentAccount.getUser().getReferenceOffice() == null) {
                return List.of(); // Se per sicurezza non ha un ufficio assegnato
            }
            titles = shiftTaskTitleRepository.findByOfficeIdAndTitleContainingIgnoreCaseAndIsActiveTrue(
                    currentAccount.getUser().getReferenceOffice().getId(), query
            );
        }

        return titles.stream().map(this::mapToResponse).toList();
    }


}
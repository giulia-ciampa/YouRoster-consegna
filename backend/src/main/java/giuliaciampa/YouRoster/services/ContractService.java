package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.ContractDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateContractDTO;
import giuliaciampa.YouRoster.dto.responses.ContractResponseDTO;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.repositories.ContractRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ContractService {
    private final ContractRepository contractRepository;
    private final UserService userService;


    public ContractService(ContractRepository contractRepository, UserService userService) {
        this.contractRepository = contractRepository;

        this.userService = userService;
    }

    //HELPER AUTENTICAZIONE
    private void validateAdminRole(Account currentAccount) {

        if (currentAccount == null || currentAccount.getRoles() == null ||
                currentAccount.getRoles().stream().noneMatch(role -> "ADMIN".equals(role.getName()) || "HR".equals(role.getName()))) {


            throw new UnauthorizedException("Accesso negato: solo l'amministratore può visualizzare queste richieste.");
        }
    }

    //HELPER DI RISPOSTA

    private ContractResponseDTO mapToResponse(Contract contract) {
        return new ContractResponseDTO(contract.getId(), contract.getUser().getId(), contract.getUser().getName(), contract.getUser().getSurname(), contract.getStartDate(), contract.getEndDate(), contract.getContractType(), contract.getHoursOfLeaveDuePerYear(), contract.getDaysOfLeaveDuePerYear(), contract.getWorkingHoursPerWeek(), contract.getContractStatus());
    }

    //FIND BY USER AND CONTRACT STATUS
    public Contract findByUserAndContractStatus(User user, ContractStatus contractStatus) {
        return contractRepository.findByUserAndContractStatus(user, contractStatus).orElseThrow(() -> new NotFoundException("Nessun contratto trovato per lo user e lo stato indicato"));
    }

    //EXIST BY USER AND CONTRACT STATUS
    public boolean existsByUserAndContractStatus(User user, ContractStatus contractStatus) {
        return contractRepository.existsByUserAndContractStatus(user, contractStatus);
    }

    //FIND BY CONTRACT STATUS
    public Page<Contract> findByContractStatus(ContractStatus contractStatus, Pageable pageable) {
        return contractRepository.findByContractStatus(contractStatus, pageable);
    }

    //1. CREA NUOVO CONTRATTO
    public ContractResponseDTO saveNewContract(ContractDTO payload) {

        //1. trovo l'utente
        User user = userService.findById(payload.userId());


        // 2. Controllo se ha già un contratto attivo
        if (contractRepository.existsByUserAndContractStatus(user, ContractStatus.ACTIVE)) {
            throw new BadRequestException("L'utente ha già un contratto attivo");
        }

        //controllo sul tipo di contratto per fixare la data di scadenza se determinato
        if ((payload.contractType() == ContractType.FULL_TIME_FIXED_TERM_CONTRACT ||
                payload.contractType() == ContractType.PART_TIME_FIXED_TERM_CONTRACT ||
                payload.contractType() == ContractType.APPRENTICESHIP_CONTRACT ||
                payload.contractType() == ContractType.EXTRACURRICULAR_INTERNSHIP)
                && payload.endDate() == null) {

            throw new BadRequestException(
                    "La data di fine contratto è obbligatoria per questo tipo di contratto"
            );
        }

        // 3. Popola l'entity
        Contract contract = new Contract();
        contract.setUser(user);
        contract.setStartDate(payload.startDate());
        contract.setEndDate(payload.endDate());
        contract.setContractType(payload.contractType());
        contract.setHoursOfLeaveDuePerYear(payload.hoursOfLeaveDuePerYear());
        contract.setDaysOfLeaveDuePerYear(payload.daysOfLeaveDuePerYear());
        contract.setWorkingHoursPerWeek(payload.workingHoursPerWeek());
        contract.setContractStatus(ContractStatus.ACTIVE);

        // 4. Salva e converti in DTO di risposta
        Contract savedContract = contractRepository.save(contract);

        //5. return
        return mapToResponse(savedContract);
    }


    //2. PATCH CONTRATTO
    public ContractResponseDTO updateContract(Account currentAccount, UpdateContractDTO payload, UUID contractId) {

        // 1. Controllo autenticazione
        validateAdminRole(currentAccount);

        // 2. Trovo il contratto esistente (se non esiste lancia un'eccezione)
        Contract existingContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contratto con ID " + contractId + " non trovato."));

        // 3. Se nel payload viene passato un nuovo stato e questo stato è ACTIVE,
        // verifichiamo che l'utente non abbia già un ALTRO contratto attivo
        if (payload.contractStatus() != null && payload.contractStatus() == ContractStatus.ACTIVE) {
            // Controlliamo se l'utente ha un contratto attivo diverso da questo che stiamo modificando
            boolean hasAnotherActive = contractRepository.existsByUserAndContractStatusAndIdNot(
                    existingContract.getUser(),
                    ContractStatus.ACTIVE,
                    contractId
            );

            if (hasAnotherActive) {
                throw new BadRequestException("Impossibile attivare il contratto: l'utente ha già un altro contratto attivo.");
            }
        }

        if (existingContract.getContractStatus() != ContractStatus.ACTIVE) {
            throw new BadRequestException("I contratti che non sono attivi, non possono essere modificati");
        }

        // 4. Aggiorniamo solo i campi non nulli (Patch parziale)
        if (payload.startDate() != null) {
            existingContract.setStartDate(payload.startDate());
        }
        if (payload.endDate() != null) {
            existingContract.setEndDate(payload.endDate());
        }
        if (payload.contractType() != null) {
            existingContract.setContractType(payload.contractType());
        }
        if (payload.hoursOfLeaveDuePerYear() != null) {
            existingContract.setHoursOfLeaveDuePerYear(
                    payload.hoursOfLeaveDuePerYear()
            );
        }

        if (payload.daysOfLeaveDuePerYear() != null) {
            existingContract.setDaysOfLeaveDuePerYear(
                    payload.daysOfLeaveDuePerYear()
            );
        }

        if (payload.workingHoursPerWeek() != null) {
            existingContract.setWorkingHoursPerWeek(
                    payload.workingHoursPerWeek()
            );
        }
        if (payload.contractStatus() != null) {
            existingContract.setContractStatus(payload.contractStatus());
        }

        // endDate obbligatoria per contratto determinato
        boolean requiresEndDate =
                existingContract.getContractType() == ContractType.FULL_TIME_FIXED_TERM_CONTRACT ||
                        existingContract.getContractType() == ContractType.PART_TIME_FIXED_TERM_CONTRACT ||
                        existingContract.getContractType() == ContractType.APPRENTICESHIP_CONTRACT ||
                        existingContract.getContractType() == ContractType.EXTRACURRICULAR_INTERNSHIP;

        if (requiresEndDate && existingContract.getEndDate() == null) {
            throw new BadRequestException(
                    "La data di fine contratto è obbligatoria per questo tipo di contratto"
            );
        }

        if (existingContract.getContractType() == ContractType.FULL_TIME_PERMANENT_CONTRACT ||
                existingContract.getContractType() == ContractType.PART_TIME_PERMANENT_CONTRACT) {

            existingContract.setEndDate(null);
        }

        // 5. Salvataggio e mapping della risposta
        Contract updatedContract = contractRepository.save(existingContract);
        return mapToResponse(updatedContract);
    }


    //3. GET ALL CONTRACTS BY START DATE, STATO E TIPO
    public Page<ContractResponseDTO> getAll(String name, LocalDate startDate,
                                            ContractType contractType,
                                            ContractStatus contractStatus,
                                            Pageable pageable) {


        Page<Contract> contracts = contractRepository.findByOptionalCriteria(name, startDate, contractType, contractStatus, pageable);

        return contracts.map(this::mapToResponse);
    }

    //4. GET MY CONTRACTS
    public List<ContractResponseDTO> getMyContracts(Account currentAccount) {

        User user = currentAccount.getUser();

        // 2. Cerchiamo tutti i contratti associati a questo utente
        List<Contract> contracts = contractRepository.findByUser(user);


        return contracts.stream()
                .map(this::mapToResponse)
                .toList();

    }


}

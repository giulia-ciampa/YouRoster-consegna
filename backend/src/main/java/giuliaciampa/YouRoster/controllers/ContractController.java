package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.ContractDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateContractDTO;
import giuliaciampa.YouRoster.dto.responses.ContractResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.ContractStatus;
import giuliaciampa.YouRoster.entities.ContractType;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.ContractService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/contracts")

public class ContractController {

    private final ContractService contractService;


    public ContractController(ContractService contractService) {
        this.contractService = contractService;

    }

    //1. CREA NUOVO CONTRATTO
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','HR')")
    public ContractResponseDTO saveNewContract(
            @RequestBody @Validated ContractDTO payload,
            BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }


        return contractService.saveNewContract(payload);

    }

    //2. PATCH CONTRATTO
    @PatchMapping("/{contractId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','HR')")
    public ContractResponseDTO updateContract(
            @AuthenticationPrincipal Account currentAccount,
            @RequestBody @Validated UpdateContractDTO payload,
            BindingResult validationResult,
            @PathVariable UUID contractId) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return contractService.updateContract(currentAccount, payload, contractId);


    }


    //3. GET ALL CONTRACTS BY START DATE, STATO E TIPO
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','HR')")
    public Page<ContractResponseDTO> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) ContractStatus contractStatus,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "startDate") String sortBy) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        return contractService.getAll(name, startDate, contractType, contractStatus, pageable);

    }

    //4. GET MY CONTRACTS
    @GetMapping("me")
    public List<ContractResponseDTO> getMyContracts(@AuthenticationPrincipal Account currentAccount) {
        return contractService.getMyContracts(currentAccount);
    }


}

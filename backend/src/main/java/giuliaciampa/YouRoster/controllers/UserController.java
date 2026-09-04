package giuliaciampa.YouRoster.controllers;


import giuliaciampa.YouRoster.dto.requests.UpdateUserProfileDTO;

import giuliaciampa.YouRoster.dto.responses.UserBasicInformationResponseDTO;
import giuliaciampa.YouRoster.dto.responses.UserForShiftAssignmentDTO;
import giuliaciampa.YouRoster.dto.responses.UserProfileResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.UserService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. GET PROFILO UTENTE LOGGATO
    @GetMapping("/me")
    public UserProfileResponseDTO getMyProfile(@AuthenticationPrincipal Account currentAccount) {
        return userService.getMyProfile(currentAccount);
    }

    //2. AGGIORNA PROFILO UTENTE LOGGATO
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserProfileResponseDTO updateMyProfile(
            @AuthenticationPrincipal Account currentAccount,
            @ModelAttribute @Validated UpdateUserProfileDTO payload, BindingResult validationResult
    ) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return userService.updateProfile(currentAccount, payload);
    }

    //3. GET TUTTI GLI UTENTI E LE SUE INFORMAZIONI(ADMIN, HR, PAYROLL)

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','HR','AP E PAYROLL SPECIALIST')")
    public UserProfileResponseDTO getUserProfileForManagement(@PathVariable UUID userId) {
        return userService.getUserProfileForManagement(userId);
    }

    //4.DATA UNA SEDE TROVA I COORDINATOR
    @GetMapping("coordinators/{officeId}")
    public Page<UserBasicInformationResponseDTO> findCoordinatorsByOfficeId(@PathVariable UUID officeId,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "15") int size,
                                                                            @RequestParam(defaultValue = "referenceOffice.name") String sortBy) {


        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return userService.findCoordinatorsByOfficeId(officeId, pageable);
    }


    //5.AGGIORNA SEDE DI UN UTENTE
    @PatchMapping("/{userId}/office/{officeId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserBasicInformationResponseDTO updateUserOffice(
            @PathVariable UUID userId,
            @PathVariable UUID officeId) {

        return userService.updateUserOffice(userId, officeId);
    }

    //UPDATE AVATAR
    @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserProfileResponseDTO updateMyAvatar(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam("photoUrl") MultipartFile avatarFile
    ) {
        return userService.updateAvatar(currentAccount, avatarFile);
    }

    //GET USER PER ASSEGNARE TURNO
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER', 'HR')")
    @GetMapping("/active")
    public List<UserForShiftAssignmentDTO> getActiveUsersForAssignment() {
        return userService.getAllActiveUsersForAssignment();
    }

    //GET TUTTI GLI UTENTI PER ADMIN, HR
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','HR','PAYROLL SPECIALIST')")
    public Page<UserProfileResponseDTO> getAllUsersForManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {

        if (size <= 0) {
            size = 15;
        }

        if (size > 15) {
            size = 15;
        }

        if (page < 0) {
            page = 0;
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("surname").ascending()
        );

        return userService.getAllUsersForManagement(pageable);
    }

}


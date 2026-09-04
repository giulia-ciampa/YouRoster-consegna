package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.*;
import giuliaciampa.YouRoster.dto.responses.*;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.AuthService;
import giuliaciampa.YouRoster.services.PasswordResetTokenService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetTokenService passwordResetTokenService;

    public AuthController(AuthService authService, PasswordResetTokenService passwordResetTokenService) {
        this.authService = authService;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    //1. REGISTRAZIONE
    @PostMapping(value = "/registration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegistrationResponseDTO register(@ModelAttribute @Validated UserRegistrationRequestDTO payload, BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return authService.registerUser(payload);
    }


    //2.LOGIN
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Validated LoginRequestDTO payload, BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return authService.login(payload);
    }

    //3.REFRESH TOKEN
    @PostMapping("/refresh")
    public LoginResponseDTO refresh(@RequestBody @Validated RefreshTokenRequestDTO payload, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return authService.refreshToken(payload.refreshToken());
    }

    //4. CAMBIA EMAIL, CAMBIA PASSWORD, O EMAIL E PASSWORD
    @PatchMapping("/credentials")
    public UpdateCredentialsResponseDTO updateCredentials(
            @AuthenticationPrincipal Account currentAccount,
            @RequestBody @Validated UpdateCredentialsRequestDTO payload,
            BindingResult validationResult) {

        if (currentAccount == null) {
            throw new UnauthorizedException("Sessione non valida o utente non autenticato");
        }

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return authService.updateCredentials(currentAccount.getId(), payload);

    }

    //RECUPER PASSWORD - RICHIESTA INIZIALE
    @PostMapping("/forgot-password")
    public ForgotPasswordResponseDTO forgotPassword(@RequestBody @Validated ForgotPasswordDTO payload, BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        passwordResetTokenService.requestPasswordReset(payload.email());
        return new ForgotPasswordResponseDTO("Email recupero password inviata con successo", LocalDateTime.now());
    }

    //CAMBIO PASSWORD EFFETTIVO
    @PostMapping("/reset-password")
    public ResetPasswordResponseDTO resetPassword(@RequestBody @Validated ResetPasswordDTO payload, BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        passwordResetTokenService.resetPassword(payload.token(), payload.newPassword());
        return new ResetPasswordResponseDTO("Password modificata con successo!", LocalDateTime.now());
    }


}

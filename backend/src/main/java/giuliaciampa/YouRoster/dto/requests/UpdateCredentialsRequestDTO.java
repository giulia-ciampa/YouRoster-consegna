package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateCredentialsRequestDTO(
        @Email(message = "Email non valida")
        String email,


        String oldPassword,


        @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{6,}$", message = "La password deve contenere almeno sei caratteri, un numero e una lettera maiuscola")
        String newPassword,


        String confirmNewPassword
) {
}

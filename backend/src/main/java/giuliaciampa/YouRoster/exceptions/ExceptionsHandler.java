package giuliaciampa.YouRoster.exceptions;

import giuliaciampa.YouRoster.dto.responses.ErrorsDTO;
import giuliaciampa.YouRoster.dto.responses.ErrorsListDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorsDTO handleUserAlreadyExistsException(AlreadyExistsException e) {
        return new ErrorsDTO(e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorsListDTO handleValidationException(ValidationException e) {
        return new ErrorsListDTO(e.getMessage(), LocalDateTime.now(), e.getErrors());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorsDTO handleUnauthorizedException(UnauthorizedException e) {
        return new ErrorsDTO(e.getMessage(), LocalDateTime.now());
    }


    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorsDTO handleBadRequestException(BadRequestException e) {
        return new ErrorsDTO(e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorsDTO handleNotFoundException(NotFoundException e) {
        return new ErrorsDTO(e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(EmailSenderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorsDTO handleEmailSendException(EmailSenderException e) {
        return new ErrorsDTO(e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorsDTO handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return new ErrorsDTO("I file caricati superano la dimensione massima consentita (6MB).", LocalDateTime.now());
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorsDTO handleGenericException(Exception e) {
        return new ErrorsDTO("Al momento il server non risponde", LocalDateTime.now());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorsListDTO handleIntegrityViolationException(DataIntegrityViolationException e) {

        e.printStackTrace();
        System.out.println("CAUSA ESATTA DB: " + e.getMostSpecificCause().getMessage());

        List<String> errorMessages = new ArrayList<>();
        String rootMessage = e.getMostSpecificCause().getMessage().toLowerCase();
        String message = "Errore nell'inserimento dei dati";

        // Controlliamo singolarmente ogni vincolo e accumuliamo i messaggi se presenti
        if (rootMessage.contains("phone_number")) {
            errorMessages.add("Il numero di telefono inserito è già associato a un altro account.");
        }
        if (rootMessage.contains("tax_code")) {
            errorMessages.add("Il codice fiscale inserito è già registrato nel sistema.");
        }
        if (rootMessage.contains("email")) {
            errorMessages.add("L'indirizzo email inserito è già in uso.");
        }

        // Fallback generico se il database lancia un'altra violazione non mappata sopra
        if (errorMessages.isEmpty()) {
            errorMessages.add("Errore di duplicazione o violazione dei vincoli nel database.");
        }

        return new ErrorsListDTO(message, LocalDateTime.now(), errorMessages);
    }


}


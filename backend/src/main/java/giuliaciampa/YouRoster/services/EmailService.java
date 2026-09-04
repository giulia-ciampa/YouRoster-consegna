package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.exceptions.EmailSenderException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    //ATTRIBUTO
    private final JavaMailSender mailSender;
    @Value("${email.from}")
    private String email;

    //COSTRUTTORE

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;

    }

    //METODO
    @Async
    public void sendHtmlEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(email);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true indica che il testo è HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSenderException("Errore durante l'invio dell'email: " + e.getMessage());
        }
    }


}

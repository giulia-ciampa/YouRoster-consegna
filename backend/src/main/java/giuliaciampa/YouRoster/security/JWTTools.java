package giuliaciampa.YouRoster.security;

import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JWTTools {
    //ATTRIBUTO
    private final String secret;

    //COSTRUTTORE
    public JWTTools(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    //GENERA TOKEN
    public String generateToken(Account account) {
        return Jwts.builder()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .subject(String.valueOf(account.getId()))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    //VERIFICA TOKEN
    public void verifyToken(String token) {
        try {
            Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes())).build().parse(token);
        } catch (Exception ex) {
            throw new UnauthorizedException("Ci sono problemi con il token, rifare il login!");
        }
    }

    //ESTRAI ID DAL TOKEN
    public UUID extractIdFromToken(String token) {
        return UUID.fromString(Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes())).build().parseSignedClaims(token).getPayload().getSubject());
    }
}

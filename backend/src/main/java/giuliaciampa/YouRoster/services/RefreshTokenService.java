package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.RefreshToken;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.repositories.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    //ATTRIBUTI
    private final long refreshTokenDurationMs = 24 * 60 * 60 * 1000;
    private final RefreshTokenRepository refreshTokenRepository;

    //COSTRUTTORE
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    //CREA TOKEN
    public RefreshToken generateRefreshToken(Account account) {
        RefreshToken refreshToken = refreshTokenRepository.findByAccount(account)
                .orElse(new RefreshToken());
        refreshToken.setAccount(account);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);

    }

    //VERIFICA VALIDITA' DEL TOKEN
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Sessione scaduta. Effettua nuovamente il login.");
        }
        return token;
    }

    //TROVA CON IL TOKEN
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh Token non non trovato."));
    }
}

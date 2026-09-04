package giuliaciampa.YouRoster.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refreshTokens")
public class RefreshToken {
    //ATTRIBUTI
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    //COSTRUTTORE
    public RefreshToken(String token, Instant expiryDate, Account account) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.account = account;
    }


    //COSTRUTTORE VUOTO
    public RefreshToken() {
    }

    //GETTER E SETTER
    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}

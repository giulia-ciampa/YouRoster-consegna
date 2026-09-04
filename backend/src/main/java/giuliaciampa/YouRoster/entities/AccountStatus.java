package giuliaciampa.YouRoster.entities;

public enum AccountStatus {
    PENDING,    // In attesa di approvazione registrazione
    ACTIVE,     // Attivo e operativo
    REJECTED,   // Registrazione rifiutata dall'Admin
    DISABLED,   // Disabilitato/Disattivato dall'Admin
}

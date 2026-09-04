package giuliaciampa.YouRoster.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class Role {

    //ATTRIBUTI

    @Id
    @GeneratedValue
    @Column(name = "role_id")
    private UUID id;

    @Column(name = "role_name", nullable = false, unique = true, length = 30)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<Account> accounts = new HashSet<>();

    //COSTRUTTORE

    public Role(String name) {
        this.name = name;
    }

    //COSTRUTTORE VUOTO

    protected Role() {
    }

    //GETTER E SETTER

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //TO STRING
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

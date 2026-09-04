package giuliaciampa.YouRoster.entities;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "shifts")
public class Shift {
    //ATTRIBUTI
    @Id
    @GeneratedValue
    @Column(name = "shift_id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "office_id")
    private Office office;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_active")
    private boolean isActive = true;


    //COSTRUTTORE
    public Shift(Office office, LocalTime startTime, LocalTime endTime) {
        this.office = office;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    //COSTRUTTORE VUOTO
    public Shift() {
    }

    //GETTER E SETTER
    public UUID getId() {
        return id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }


    //TO STRING


    @Override
    public String toString() {
        return "Shift{" +
                "id=" + id +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", isActive=" + isActive() +
                ", office=" + getOffice() +
                '}';
    }
}

package giuliaciampa.YouRoster.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@Entity
@Table(name = "change_holiday_requests")
public class ChangeHolidayRequest extends ChangeRequest {

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days", nullable = false)
    private int totalDays;

    //COSTRUTTORE
    public ChangeHolidayRequest(Request originalRequest, String employeeNotes, LocalDate startDate, LocalDate endDate) {
        super(originalRequest, employeeNotes);
        this.startDate = startDate;
        this.endDate = endDate;
        calculateTotalDays();
    }

    //COSTRUTTORE VUOTO
    public ChangeHolidayRequest() {
    }

    //GETTER E SETTER

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getTotalDays() {
        return totalDays;
    }

    //CALCOLO GIORNI TOTALI
    public void calculateTotalDays() {
        this.totalDays = (int) DAYS.between(this.startDate, this.endDate) + 1;
    }

    // TO STRING
    @Override
    public String toString() {
        return "ChangeHolidayRequest{" +
                "startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", totalDays=" + getTotalDays() +
                '}';
    }
}

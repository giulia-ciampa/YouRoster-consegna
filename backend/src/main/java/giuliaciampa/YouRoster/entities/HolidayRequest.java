package giuliaciampa.YouRoster.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@Entity
@Table(name = "holiday_requests")
@PrimaryKeyJoinColumn(name = "request_id")
public class HolidayRequest extends Request {

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days_requested")
    private int totalDaysRequested;

    //COSTRUTTORE
    public HolidayRequest(User employee, String employeeNotes, LocalDate startDate, LocalDate endDate) {
        super(employee, employeeNotes);
        this.startDate = startDate;
        this.endDate = endDate;
        calculateTotalDays();
    }

    //COSTRUTTORE VUOTO
    public HolidayRequest() {
        super();
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

    public int getTotalDaysRequested() {
        return totalDaysRequested;
    }

    public void setTotalDaysRequested(int totalDaysRequested) {
        this.totalDaysRequested = totalDaysRequested;
    }

    //METODO CALCOLO GIORNI TOTALI
    public void calculateTotalDays() {
        this.totalDaysRequested = (int) DAYS.between(this.startDate, this.endDate) + 1;
    }

    //TO STRING
    @Override
    public String toString() {
        return "HolidayRequest{" +
                "startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", totalDaysRequested=" + getTotalDaysRequested() +
                '}';
    }
}

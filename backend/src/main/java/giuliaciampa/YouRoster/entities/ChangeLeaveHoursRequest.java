package giuliaciampa.YouRoster.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "change_leave_hours_requests")
public class ChangeLeaveHoursRequest extends ChangeRequest {
    //ATTRIBUTI
    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "leave_hours_type", nullable = false)
    private LeaveHoursType changeLeaveHoursType;

    @Column(name = "total_hours", nullable = false)
    private BigDecimal totalHours;

    //COSTRUTTORE
    public ChangeLeaveHoursRequest(Request originalRequest, String employeeNotes, LocalDate date, LocalTime startTime, LocalTime endTime, LeaveHoursType changeLeaveHoursType) {
        super(originalRequest, employeeNotes);
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.changeLeaveHoursType = changeLeaveHoursType;
        calculateTotalHours();
    }


    //COSTRUTTORE VUOTO
    public ChangeLeaveHoursRequest() {
        super();
    }

    //GETTER E SETTER

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public LeaveHoursType getChangeLeaveHoursType() {
        return changeLeaveHoursType;
    }

    public void setChangeLeaveHoursType(LeaveHoursType changeLeaveHoursType) {
        this.changeLeaveHoursType = changeLeaveHoursType;
    }

    //METODO CALCOLO ORE TOTALI
    public void calculateTotalHours() {
        if (this.startTime != null && this.endTime != null) {
            long minutes = java.time.temporal.ChronoUnit.MINUTES.between(this.startTime, this.endTime);

            // Converte i minuti in ore decimali (es. 90 minuti -> 1.50 ore) e arrotonda a 2 decimali
            this.totalHours = BigDecimal.valueOf(minutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        }
    }

    //TO STRING
    @Override
    public String toString() {
        return "ChangeLeaveHoursRequest{" +
                "date=" + getDate() +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", changeLeaveHoursType=" + getChangeLeaveHoursType() +
                ", totalHours=" + getTotalHours() +
                '}';
    }
}

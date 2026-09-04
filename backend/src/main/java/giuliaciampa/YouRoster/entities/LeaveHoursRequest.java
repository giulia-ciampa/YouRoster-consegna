package giuliaciampa.YouRoster.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "leaves_hours_requests")
@PrimaryKeyJoinColumn(name = "request_id")
public class LeaveHoursRequest extends Request {

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "leave_hours_type")
    @Enumerated(EnumType.STRING)
    private LeaveHoursType leaveHoursType;

    @Column(name = "total_hours", nullable = false)
    private BigDecimal totalHours;

    //COSTRUTTORE
    public LeaveHoursRequest(User employee, String employeeNotes, LocalDate date, LocalTime startTime, LocalTime endTime, LeaveHoursType leaveHoursType) {
        super(employee, employeeNotes);
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.leaveHoursType = leaveHoursType;
        calculateTotalHours();
    }

    //COSTRUTTORE VUOTO
    public LeaveHoursRequest() {
    }

    //GETTER E SETTER

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public LeaveHoursType getLeaveHoursType() {
        return leaveHoursType;
    }

    public void setLeaveHoursType(LeaveHoursType leaveHoursType) {
        this.leaveHoursType = leaveHoursType;
    }

    // METODO CALCOLO ORE TOTALI
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
        return "LeaveHoursRequest{" +
                "date=" + getDate() +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", leaveHoursType=" + getLeaveHoursType() +
                ", totalHours=" + getTotalHours() +
                '}';
    }
}

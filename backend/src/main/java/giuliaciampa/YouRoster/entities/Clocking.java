package giuliaciampa.YouRoster.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "clockings")
public class Clocking {
//ATTRIBUTO

    @Id
    @GeneratedValue
    @Column(name = "clocking_id")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "shift_assignment_id", nullable = false, unique = true)
    private ShiftAssignment shiftAssignment;

    @Column(name = "acutal_start_time")
    private LocalTime actualStartTime;

    @Column(name = "acutal_end_time")
    private LocalTime actualEndTime;

    @Column(length = 100)
    private String note;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "is_position_valid")
    private boolean positionValid;

    @Column(name = "late_minutes")
    private int lateMinutes;

    @Column(name = "early_departure_minutes")
    private int earlyDepartureMinutes;

    @Column(name = "worked_minutes")
    private int workedMinutes;

    @Column(name = "balance_minutes")
    private int balanceMinutes; //differenza finale tra lavorato e previsto

    // COSTRUTTORE PER IL CLOCK-IN (Ingresso)
    public Clocking(ShiftAssignment shiftAssignment, Office office, LocalTime actualStartTime, BigDecimal latitude, BigDecimal longitude, boolean positionValid) {
        this.shiftAssignment = shiftAssignment;
        this.office = office;
        this.actualStartTime = actualStartTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.positionValid = positionValid;
    }

    //COSTRUTTORE VUOTO
    public Clocking() {
    }

    //GETTER E SETTER

    public UUID getId() {
        return id;
    }

    public ShiftAssignment getShiftAssignment() {
        return shiftAssignment;
    }

    public void setShiftAssignment(ShiftAssignment shiftAssignment) {
        this.shiftAssignment = shiftAssignment;
    }

    public LocalTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(AttendanceStatus attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public boolean isPositionValid() {
        return positionValid;
    }

    public void setPositionValid(boolean positionValid) {
        this.positionValid = positionValid;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public int getEarlyDepartureMinutes() {
        return earlyDepartureMinutes;
    }

    public void setEarlyDepartureMinutes(int earlyDepartureMinutes) {
        this.earlyDepartureMinutes = earlyDepartureMinutes;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(int lateMinutes) {
        this.lateMinutes = lateMinutes;
    }


    public int getBalanceMinutes() {
        return balanceMinutes;
    }

    public void setBalanceMinutes(int balanceMinutes) {
        this.balanceMinutes = balanceMinutes;
    }

    public int getWorkedMinutes() {
        return workedMinutes;
    }

    public void setWorkedMinutes(int workedMinutes) {
        this.workedMinutes = workedMinutes;
    }

    // METODO CALCOLO ATTENDANCE STATUS
    public void calculateStatus(LocalTime scheduledStartTime, LocalTime scheduledEndTime) {

        // 1. Se non ha fatto l'ingresso
        if (this.actualStartTime == null) {
            this.attendanceStatus = AttendanceStatus.ABSENT;
            return;
        }

        // 2. Se non ha ancora timbrato l'uscita (turno in corso)
        if (this.actualEndTime == null) {

            if (this.actualStartTime.isAfter(scheduledStartTime)) {
                this.attendanceStatus = AttendanceStatus.LATE;
            } else {
                this.attendanceStatus = AttendanceStatus.ON_TIME;
            }

            return;
        }

        //3. se timbra l'uscita prima
        if (this.actualEndTime.isBefore(scheduledEndTime)) {
            this.attendanceStatus = AttendanceStatus.EARLY_DEPARTURE;
        }

        //4. Ha timbrato sia ingresso che uscita nei tempi corretti
        if (this.actualStartTime == scheduledStartTime && this.actualEndTime == scheduledEndTime) {
            this.attendanceStatus = AttendanceStatus.COMPLETED;
        }
    }


    // METODO CALCOLO MINUTI
    public void calculateMinutes(LocalTime scheduledStartTime, LocalTime scheduledEndTime) {

        // Reset dei valori
        this.workedMinutes = 0;
        this.lateMinutes = 0;
        this.earlyDepartureMinutes = 0;
        this.balanceMinutes = 0;

        // Se manca entrata o uscita non possiamo calcolare le ore lavorate
        if (this.actualStartTime == null || this.actualEndTime == null) {
            return;
        }

        // Minuti previsti dal turno
        int scheduledMinutes = (int) ChronoUnit.MINUTES.between(
                scheduledStartTime,
                scheduledEndTime
        );

        // Minuti effettivamente lavorati
        this.workedMinutes = (int) ChronoUnit.MINUTES.between(
                this.actualStartTime,
                this.actualEndTime
        );


        // Ritardo
        if (this.actualStartTime.isAfter(scheduledStartTime)) {
            this.lateMinutes = (int) ChronoUnit.MINUTES.between(
                    scheduledStartTime,
                    this.actualStartTime
            );
        }

        // Uscita anticipata
        if (this.actualEndTime.isBefore(scheduledEndTime)) {
            this.earlyDepartureMinutes = (int) ChronoUnit.MINUTES.between(
                    this.actualEndTime,
                    scheduledEndTime
            );
        }

        // Saldo della giornata
        // Positivo = minuti lavorati in più
        // Negativo = minuti lavorati in meno
        this.balanceMinutes = this.workedMinutes - scheduledMinutes;
    }


    //TO STRING
    @Override
    public String toString() {
        return "Clocking{" +
                "id=" + getId() +
                ", shiftAssignment=" + getShiftAssignment() +
                ", actualStartTime=" + getActualStartTime() +
                ", actualEndTime=" + getActualEndTime() +
                ", note='" + getNote() + '\'' +
                ", attendanceStatus=" + getAttendanceStatus() +
                ", office=" + getOffice() +
                ", latitude=" + getLatitude() +
                ", longitude=" + getLongitude() +
                ", positionValid=" + isPositionValid() +
                ", lateMinutes=" + getLateMinutes() +
                ", earlyDepartureMinutes=" + getEarlyDepartureMinutes() +
                ", workedMinutes=" + getWorkedMinutes() +
                ", balanceMinutes=" + getBalanceMinutes() +
                '}';
    }
}


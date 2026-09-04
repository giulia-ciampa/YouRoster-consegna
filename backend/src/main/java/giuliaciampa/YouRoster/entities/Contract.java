package giuliaciampa.YouRoster.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue
    @Column(name = "contract_id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "contract_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @Column(name = "hours_of_leave_per_year")
    private int hoursOfLeaveDuePerYear;

    @Column(name = "days_of_leave_per_year")
    private int daysOfLeaveDuePerYear;

    @Column(name = "working_hours_per_week")
    private int workingHoursPerWeek;

    @Column(name = "contract_status")
    @Enumerated(EnumType.STRING)
    private ContractStatus contractStatus;

    //COSTRUTTORE
    public Contract(User user, LocalDate startDate, LocalDate endDate, ContractType contractType, int hoursOfLeaveDuePerYear, int daysOfLeaveDuePerYear, int workingHoursPerWeek, ContractStatus contractStatus) {
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.contractType = contractType;
        this.hoursOfLeaveDuePerYear = hoursOfLeaveDuePerYear;
        this.daysOfLeaveDuePerYear = daysOfLeaveDuePerYear;
        this.workingHoursPerWeek = workingHoursPerWeek;
        this.contractStatus = contractStatus;
    }

    //COSTRUTTORE VUOTO
    public Contract() {
    }

    //GETTER E SETTER

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ContractStatus getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(ContractStatus contractStatus) {
        this.contractStatus = contractStatus;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public int getDaysOfLeaveDuePerYear() {
        return daysOfLeaveDuePerYear;
    }

    public void setDaysOfLeaveDuePerYear(int daysOfLeaveDuePerYear) {
        this.daysOfLeaveDuePerYear = daysOfLeaveDuePerYear;
    }

    public int getHoursOfLeaveDuePerYear() {
        return hoursOfLeaveDuePerYear;
    }

    public void setHoursOfLeaveDuePerYear(int hoursOfLeaveDuePerYear) {
        this.hoursOfLeaveDuePerYear = hoursOfLeaveDuePerYear;
    }

    public int getWorkingHoursPerWeek() {
        return workingHoursPerWeek;
    }

    public void setWorkingHoursPerWeek(int workingHoursPerWeek) {
        this.workingHoursPerWeek = workingHoursPerWeek;
    }

    //TO STRING

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + getId() +
                ", user=" + getUser() +
                ", startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", contractType=" + getContractType() +
                ", hoursOfLeaveDuePerYear=" + getHoursOfLeaveDuePerYear() +
                ", daysOfLeaveDuePerYear=" + getDaysOfLeaveDuePerYear() +
                ", workingHoursPerWeek=" + getWorkingHoursPerWeek() +
                '}';
    }
}

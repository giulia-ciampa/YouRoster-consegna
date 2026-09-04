package giuliaciampa.YouRoster.entities;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "requests")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Request {

    @Id
    @GeneratedValue
    @Column(name = "request_id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_employee", nullable = false)
    private User employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus requestStatus = RequestStatus.SENT;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "employee_notes", length = 200)
    private String employeeNotes;

    @Column(name = "reviewer_notes", length = 200)
    private String reviewerNotes;

    @ManyToOne
    @JoinColumn(name = "id_reviewer")
    private User reviewer;

    @Column(name = "response_date")
    private LocalDate responseDate;

    //COSTRUTTORE
    public Request(User employee, String employeeNotes) {
        this.employee = employee;
        this.employeeNotes = employeeNotes;
    }

    //COSTRUTTORE VUOTO
    public Request() {
    }

    //GETTER E SETTER

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public LocalDate getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(LocalDate responseDate) {
        this.responseDate = responseDate;
    }

    public String getEmployeeNotes() {
        return employeeNotes;
    }

    public void setEmployeeNotes(String employeeNotes) {
        this.employeeNotes = employeeNotes;
    }

    public String getReviewerNotes() {
        return reviewerNotes;
    }

    public void setReviewerNotes(String reviewerNotes) {
        this.reviewerNotes = reviewerNotes;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    //TO STRING
    @Override
    public String toString() {
        return "Request{" +
                "id=" + getId() +
                ", employee=" + getEmployee() +
                ", requestStatus=" + getRequestStatus() +
                ", createdAt=" + getCreatedAt() +
                ", employeeNotes='" + getEmployeeNotes() + '\'' +
                ", reviewerNotes='" + getReviewerNotes() + '\'' +
                ", reviewer=" + getReviewer() +
                ", responseDate=" + getResponseDate() +
                '}';
    }
}

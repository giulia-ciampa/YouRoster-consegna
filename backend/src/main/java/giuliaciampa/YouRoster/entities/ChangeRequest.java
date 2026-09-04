package giuliaciampa.YouRoster.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "change_requests")
@Inheritance(strategy = InheritanceType.JOINED)
public class ChangeRequest {
    //ATTRIBUTI
    @Id
    @GeneratedValue
    @Column(name = "Change_request_id")
    private UUID id;

    @JoinColumn(name = "original_request_id", nullable = false)
    @ManyToOne
    private Request originalRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_request_status", nullable = false)
    private RequestStatus changeRequestStatus = RequestStatus.SENT;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "employee_notes", length = 200)
    private String employeeNotes;

    @JoinColumn(name = "reviewer_id")
    @ManyToOne
    private User reviewer;

    @Column(name = "reviewer_notes")
    private String reviewerNotes;

    @Column(name = "response_date")
    private LocalDate responseDate;

    @Column(name = "reviewCount")
    private int reviewCount = 0;

    //COSTRUTTORE
    public ChangeRequest(Request originalRequest, String employeeNotes) {
        this.originalRequest = originalRequest;
        this.employeeNotes = employeeNotes;
        this.reviewerNotes = reviewerNotes;
    }

    //COSTRUTTORE VUOTO
    public ChangeRequest() {
    }


    //GETTER E SETTER


    public String getReviewerNotes() {
        return reviewerNotes;
    }

    public void setReviewerNotes(String reviewerNotes) {
        this.reviewerNotes = reviewerNotes;
    }

    public UUID getId() {
        return id;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public String getEmployeeNotes() {
        return employeeNotes;
    }

    public void setEmployeeNotes(String employeeNotes) {
        this.employeeNotes = employeeNotes;
    }

    public LocalDate getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(LocalDate responseDate) {
        this.responseDate = responseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public RequestStatus getChangeRequestStatus() {
        return changeRequestStatus;
    }

    public void setChangeRequestStatus(RequestStatus changeRequestStatus) {
        this.changeRequestStatus = changeRequestStatus;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Request getOriginalRequest() {
        return originalRequest;
    }

    public void setOriginalRequest(Request originalRequest) {
        this.originalRequest = originalRequest;
    }

    //TO STRING
    @Override
    public String toString() {
        return "ChangeRequest{" +
                "id=" + getId() +
                ", originalRequest=" + getOriginalRequest() +
                ", changeRequestStatus=" + getChangeRequestStatus() +
                ", createdAt=" + getCreatedAt() +
                ", employeeNotes='" + getEmployeeNotes() + '\'' +
                ", reviewer=" + getReviewer() +
                ", reviewerNotes='" + getReviewerNotes() + '\'' +
                ", responseDate=" + getResponseDate() +
                ", reviewCount=" + getReviewCount() +
                '}';
    }
}

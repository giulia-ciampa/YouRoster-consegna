package giuliaciampa.YouRoster.entities;


import jakarta.persistence.*;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@Entity
@Table(name = "absence_certificate_requests")
@PrimaryKeyJoinColumn(name = "request_id")
public class AbsenceCertificationRequest extends Request {

    @Column(name = "protocol_code", length = 100)
    private String protocolCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days")
    private Integer totalDays;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", nullable = false)
    private CertificateType certificateType;

    //COSTRUTTORE
    public AbsenceCertificationRequest(User employee, String employeeNotes, LocalDate startDate, LocalDate endDate, CertificateType certificateType, String certificateUrl) {
        super(employee, employeeNotes);
        this.startDate = startDate;
        this.endDate = endDate;
        this.certificateType = certificateType;
        this.certificateUrl = certificateUrl;
    }

    //COSTRUTTORE VUOTO
    public AbsenceCertificationRequest() {
    }

    //GETTER E SETTER

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getProtocolCode() {
        return protocolCode;
    }

    public void setProtocolCode(String protocolCode) {
        this.protocolCode = protocolCode;
    }

    //METODO CALCOLO GIORNI TOTALI
    public void calculateTotalDays() {
        this.totalDays = (int) DAYS.between(this.startDate, this.endDate) + 1;
    }

    //TO STRING
    @Override
    public String toString() {
        return "AbsenceCertificationRequest{" +
                "protocolCode='" + getProtocolCode() + '\'' +
                ", startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", totalDays=" + getTotalDays() +
                ", issueDate=" + getIssueDate() +
                ", certificateUrl='" + getCertificateUrl() + '\'' +
                ", certificateType=" + getCertificateType() +
                '}';
    }
}

package giuliaciampa.YouRoster.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    //ATTRIBUTI
    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private UUID id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 30)
    private String surname;

    @Column(nullable = false, unique = true, length = 16)
    private String taxCode;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "place_of_birth", nullable = false, length = 50)
    private String placeOfBirth;

    @Column(nullable = false, length = 40)
    private String nationality;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 60)
    private String streetAddress;

    @Column(name = "house_number", length = 10, nullable = false)
    private String houseNumber;

    @Column(name = "zip_code", length = 5, nullable = false)
    private String zipCode;

    @Column(length = 20, nullable = false)
    private String city;

    @Column(length = 2, nullable = false)
    private String province;

    @Column(name = "photo_url")
    private String photoUrl;

    @ManyToOne //VEDI IN SEGUITO SE FARE UNA RELAZIONE BIDIREZIONALE
    @JoinColumn(name = "reference_office_id")
    private Office referenceOffice;

    @Column(unique = true, nullable = false, length = 50)
    private String iban;

    @Column(name = "document_number", nullable = false, unique = true, length = 20)
    private String documentNumber;

    @Column(name = "document_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "document_front_url", nullable = false)
    private String documentFrontUrl;

    @Column(name = "document_back_url", nullable = false)
    private String documentBackUrl;

    @Column(name = "tax_code_front_url", nullable = false)
    private String taxCodeCardFrontUrl;

    @Column(name = "tax_code_back_url", nullable = false)
    private String taxCodeCardBackUrl;

    @OneToOne
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    //COSTRUTTORE

    public User(String name, String surname, String taxCode, LocalDate dateOfBirth, String placeOfBirth, String nationality, String phoneNumber, String streetAddress, String houseNumber, String zipCode, String city, String province, Office referenceOffice, String iban, String documentNumber, DocumentType documentType, LocalDate issueDate, LocalDate expirationDate, String documentFrontUrl, String documentBackUrl, String taxCodeCardFrontUrl, String taxCodeCardBackUrl, Account account) {

        this.name = name;
        this.surname = surname;
        this.nationality = nationality;
        this.taxCode = taxCode;
        this.dateOfBirth = dateOfBirth;
        this.placeOfBirth = placeOfBirth;
        this.phoneNumber = phoneNumber;
        this.streetAddress = streetAddress;
        this.houseNumber = houseNumber;
        this.zipCode = zipCode;
        this.city = city;
        this.province = province;
        this.photoUrl = "https://ui-avatars.com/api/?name=" + name + "+" + surname + "&background=E35226&color=9E4289&rounded=true&bold=true&size=128&format=svg";
        this.referenceOffice = referenceOffice;
        this.iban = iban;
        this.documentNumber = documentNumber;
        this.documentType = documentType;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.documentFrontUrl = documentFrontUrl;
        this.documentBackUrl = documentBackUrl;
        this.taxCodeCardFrontUrl = taxCodeCardFrontUrl;
        this.taxCodeCardBackUrl = taxCodeCardBackUrl;
        this.account = account;
    }

    //COSTRUTTORE VUOTO

    public User() {
    }

    //GETTER E SETTER

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Office getReferenceOffice() {
        return referenceOffice;
    }

    public void setReferenceOffice(Office referenceOffice) {
        this.referenceOffice = referenceOffice;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDocumentFrontUrl() {
        return documentFrontUrl;
    }

    public void setDocumentFrontUrl(String documentFrontUrl) {
        this.documentFrontUrl = documentFrontUrl;
    }

    public String getDocumentBackUrl() {
        return documentBackUrl;
    }

    public void setDocumentBackUrl(String documentBackUrl) {
        this.documentBackUrl = documentBackUrl;
    }

    public String getTaxCodeCardFrontUrl() {
        return taxCodeCardFrontUrl;
    }

    public void setTaxCodeCardFrontUrl(String taxCodeCardFrontUrl) {
        this.taxCodeCardFrontUrl = taxCodeCardFrontUrl;
    }

    public String getTaxCodeCardBackUrl() {
        return taxCodeCardBackUrl;
    }

    public void setTaxCodeCardBackUrl(String taxCodeCardBackUrl) {
        this.taxCodeCardBackUrl = taxCodeCardBackUrl;
    }

    //TOSTRING


    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", taxCode='" + getTaxCode() + '\'' +
                ", dateOfBirth=" + getDateOfBirth() +
                ", placeOfBirth='" + getPlaceOfBirth() + '\'' +
                ", nationality='" + getNationality() + '\'' +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", streetAddress='" + getStreetAddress() + '\'' +
                ", houseNumber='" + getHouseNumber() + '\'' +
                ", zipCode='" + getZipCode() + '\'' +
                ", city='" + getCity() + '\'' +
                ", province='" + getProvince() + '\'' +
                ", photoUrl='" + getPhotoUrl() + '\'' +
                ", referenceOffice=" + getReferenceOffice() +
                ", iban='" + getIban() + '\'' +
                ", documentNumber='" + getDocumentNumber() + '\'' +
                ", documentType=" + getDocumentType() +
                ", issueDate=" + getIssueDate() +
                ", expirationDate=" + getExpirationDate() +
                ", documentFrontUrl='" + getDocumentFrontUrl() + '\'' +
                ", documentBackUrl='" + getDocumentBackUrl() + '\'' +
                ", taxCodeCardFrontUrl='" + getTaxCodeCardFrontUrl() + '\'' +
                ", taxCodeCardBackUrl='" + getTaxCodeCardBackUrl() + '\'' +
                ", account=" + getAccount() +
                '}';
    }
}

package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.AbsenceCertificationRequest;
import giuliaciampa.YouRoster.entities.RequestStatus;
import giuliaciampa.YouRoster.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface AbsenceCertificationRequestRepository extends JpaRepository<AbsenceCertificationRequest, UUID> {
    boolean existsByProtocolCode(String protocolCode);

    Page<AbsenceCertificationRequest> findByEmployee(User employee, Pageable pageable);

    Page<AbsenceCertificationRequest> findByEmployeeAndRequestStatus(User employee, RequestStatus requestStatus, Pageable pageable);

    Page<AbsenceCertificationRequest> findByEmployeeAndStartDateBetween(User employee, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<AbsenceCertificationRequest> findByEmployeeAndStartDate(User employee, LocalDate startDate, Pageable pageable);

    @Query("SELECT r FROM AbsenceCertificationRequest r WHERE " +
            "(:search = '' OR :search IS NULL OR LOWER(r.employee.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.employee.surname) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.requestStatus = :status) AND " +
            "(CAST(:startDate AS java.time.LocalDate) IS NULL OR r.startDate >= :startDate) AND " +
            "(CAST(:endDate AS java.time.LocalDate) IS NULL OR r.endDate <= :endDate)")
    Page<AbsenceCertificationRequest> findWithFilters(
            @Param("search") String name,
            @Param("status") RequestStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );


    @Query("""
                SELECT r FROM AbsenceCertificationRequest r
                WHERE r.requestStatus = :status
                AND r.startDate <= :endDate
                AND r.endDate >= :startDate
            """)
    Page<AbsenceCertificationRequest> findApprovedOverlappingRange(
            @Param("status") RequestStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

}

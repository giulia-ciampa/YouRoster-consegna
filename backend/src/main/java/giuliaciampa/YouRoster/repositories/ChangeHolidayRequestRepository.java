package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.ChangeHolidayRequest;
import giuliaciampa.YouRoster.entities.RequestStatus;
import giuliaciampa.YouRoster.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChangeHolidayRequestRepository extends JpaRepository<ChangeHolidayRequest, UUID> {

    Page<ChangeHolidayRequest> findByOriginalRequest_EmployeeAndStartDateBetween(User employee, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<ChangeHolidayRequest> findByOriginalRequest_EmployeeAndStartDate(User employee, LocalDate startDate, Pageable pageable);

    Page<ChangeHolidayRequest> findByOriginalRequest_EmployeeAndChangeRequestStatus(User employee, RequestStatus requestStatus, Pageable pageable);

    Page<ChangeHolidayRequest> findByOriginalRequest_Employee(User employee, Pageable pageable);

    @Query("SELECT r FROM ChangeHolidayRequest r WHERE " +
            "(:search = '' OR :search IS NULL OR LOWER(r.originalRequest.employee.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.originalRequest.employee.surname) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.changeRequestStatus = :status) AND " +
            "(CAST(:startDate AS java.time.LocalDate) IS NULL OR r.startDate >= :startDate) AND " +
            "(CAST(:endDate AS java.time.LocalDate) IS NULL OR r.endDate <= :endDate)")
    Page<ChangeHolidayRequest> findWithFilters(
            @Param("search") String name,
            @Param("status") RequestStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    Optional<ChangeHolidayRequest>
    findByOriginalRequest_IdAndOriginalRequest_Employee(
            UUID originalRequestId,
            User employee
    );

    boolean existsByOriginalRequest_IdAndChangeRequestStatus(UUID originalRequestId, RequestStatus requestStatus);

    @Query("SELECT r FROM ChangeHolidayRequest r WHERE " +
            "(:search = '' OR :search IS NULL OR LOWER(r.originalRequest.employee.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.originalRequest.employee.surname) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.changeRequestStatus = :status) AND " +
            "r.originalRequest.employee <> :employee AND " +
            "(:startDate IS NULL OR r.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR r.endDate <= :endDate)")
    Page<ChangeHolidayRequest> findPendingRequestsForShiftManager(
            @Param("search") String name,
            @Param("status") RequestStatus status,
            @Param("employee") User employee,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}

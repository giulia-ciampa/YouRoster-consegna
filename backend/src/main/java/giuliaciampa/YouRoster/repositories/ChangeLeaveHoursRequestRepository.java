package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.ChangeLeaveHoursRequest;
import giuliaciampa.YouRoster.entities.RequestStatus;
import giuliaciampa.YouRoster.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChangeLeaveHoursRequestRepository extends JpaRepository<ChangeLeaveHoursRequest, UUID> {

    Page<ChangeLeaveHoursRequest> findByOriginalRequest_EmployeeAndStartTimeBetween(User employee, LocalTime startTime, LocalTime endTime, Pageable pageable);

    Page<ChangeLeaveHoursRequest> findByOriginalRequest_EmployeeAndStartTime(User employee, LocalTime startTime, Pageable pageable);

    Page<ChangeLeaveHoursRequest> findByOriginalRequest_EmployeeAndChangeRequestStatus(User employee, RequestStatus requestStatus, Pageable pageable);

    Page<ChangeLeaveHoursRequest> findByOriginalRequest_Employee(User employee, Pageable pageable);

    @Query("SELECT r FROM ChangeLeaveHoursRequest r WHERE " +
            "(:search = '' OR :search IS NULL OR LOWER(r.originalRequest.employee.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.originalRequest.employee.surname) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.changeRequestStatus = :status) AND " +
            "(CAST(:date AS java.time.LocalDate) IS NULL OR r.date = :date) AND " +
            "(CAST(:startTime AS java.time.LocalTime) IS NULL OR r.startTime >= :startTime) AND " +
            "(CAST(:endTime AS java.time.LocalTime) IS NULL OR r.endTime <= :endTime)")
    Page<ChangeLeaveHoursRequest> findWithFilters(
            @Param("search") String name,
            @Param("status") RequestStatus status,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            Pageable pageable
    );


    @Query("SELECT r FROM ChangeLeaveHoursRequest r WHERE " +
            "(:search = '' OR :search IS NULL OR LOWER(r.originalRequest.employee.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.originalRequest.employee.surname) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.changeRequestStatus = :status) AND " +
            "r.originalRequest.employee <> :employee AND " +
            "(CAST(:date AS java.time.LocalDate) IS NULL OR r.date = :date) AND " +
            "(CAST(:startTime AS java.time.LocalTime) IS NULL OR r.startTime >= :startTime) AND " +
            "(CAST(:endTime AS java.time.LocalTime) IS NULL OR r.endTime <= :endTime)")
    Page<ChangeLeaveHoursRequest> findPendingRequestsForShiftManager(
            @Param("search") String name,
            @Param("status") RequestStatus status,
            @Param("employee") User employee,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            Pageable pageable
    );

    Optional<ChangeLeaveHoursRequest>
    findByOriginalRequest_IdAndOriginalRequest_Employee(
            UUID originalRequestId,
            User employee
    );

    boolean existsByOriginalRequest_IdAndChangeRequestStatus(UUID originalRequestId, RequestStatus requestStatus);
}

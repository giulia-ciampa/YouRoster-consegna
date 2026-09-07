package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.LeaveHoursRequest;
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
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveHoursRequestRepository extends JpaRepository<LeaveHoursRequest, UUID> {

    List<LeaveHoursRequest> findByEmployeeAndRequestStatusAndDateBetween(
            User employee,
            RequestStatus requestStatus,
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByDate(LocalDate date);

    Page<LeaveHoursRequest> findByEmployeeAndStartTimeBetween(User employee, LocalTime startTime, LocalTime endTime, Pageable pageable);

    Page<LeaveHoursRequest> findByEmployeeAndStartTime(User employee, LocalTime startTime, Pageable pageable);

    Page<LeaveHoursRequest> findByEmployeeAndRequestStatus(User employee, RequestStatus requestStatus, Pageable pageable);

    Page<LeaveHoursRequest> findByEmployee(User employee, Pageable pageable);

    @Query("SELECT r FROM LeaveHoursRequest r WHERE " +
            "(:search = '' OR :search IS NULL OR LOWER(r.employee.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.employee.surname) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.requestStatus = :status) AND " +
            "(CAST(:date AS java.time.LocalDate) IS NULL OR r.date = :date) AND " +
            "(CAST(:startTime AS java.time.LocalTime) IS NULL OR r.startTime >= :startTime) AND " +
            "(CAST(:endTime AS java.time.LocalTime) IS NULL OR r.endTime <= :endTime)")
    Page<LeaveHoursRequest> findWithFilters(
            @Param("search") String name,
            @Param("status") RequestStatus status,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime, // <-- Corretto da "endDate" a "endTime"
            Pageable pageable
    );

    @Query("SELECT r FROM LeaveHoursRequest r WHERE " +
            "(:search = '' OR :search IS NULL OR LOWER(r.employee.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.employee.surname) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.requestStatus = :status) AND " +
            "r.employee <> :employee AND " +
            "(CAST(:date AS java.time.LocalDate) IS NULL OR r.date = :date) AND " +
            "(CAST(:startTime AS java.time.LocalTime) IS NULL OR r.startTime >= :startTime) AND " +
            "(CAST(:endTime AS java.time.LocalTime) IS NULL OR r.endTime <= :endTime)")
    Page<LeaveHoursRequest> findPendingRequestsForShiftManager(
            @Param("search") String name,
            @Param("status") RequestStatus status,
            @Param("employee") User employee,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            Pageable pageable
    );


}

package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.HolidayRequest;
import giuliaciampa.YouRoster.entities.RequestStatus;
import giuliaciampa.YouRoster.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface HolidayRequestRepository extends JpaRepository<HolidayRequest, UUID> {

    List<HolidayRequest> findByEmployeeAndRequestStatusAndStartDateBetween(
            User employee,
            RequestStatus requestStatus,
            LocalDate startDate,
            LocalDate endDate
    );

    Page<HolidayRequest> findByEmployee(User employee, Pageable pageable);

    Page<HolidayRequest> findByEmployeeAndRequestStatus(User employee, RequestStatus requestStatus, Pageable pageable);

    Page<HolidayRequest> findByEmployeeAndStartDateBetween(User employee, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<HolidayRequest> findByEmployeeAndStartDate(User employee, LocalDate startDate, Pageable pageable);


    @Query("SELECT r FROM HolidayRequest r WHERE " +
            "(:search = '' OR :search IS NULL OR LOWER(r.employee.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.employee.surname) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.requestStatus = :status) AND " +
            "(CAST(:startDate AS java.time.LocalDate) IS NULL OR r.startDate >= :startDate) AND " +
            "(CAST(:endDate AS java.time.LocalDate) IS NULL OR r.endDate <= :endDate)")
    Page<HolidayRequest> findWithFilters(
            @Param("search") String name,
            @Param("status") RequestStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    boolean existsByStartDateAndEndDate(LocalDate startDate, LocalDate endDate);
}

package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.AttendanceStatus;
import giuliaciampa.YouRoster.entities.Clocking;
import giuliaciampa.YouRoster.entities.ShiftAssignment;
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
public interface ClockingRepository extends JpaRepository<Clocking, UUID> {

    Optional<Clocking> findByShiftAssignment(ShiftAssignment assignment);

    @Query("SELECT c FROM Clocking c JOIN c.office o JOIN c.shiftAssignment sa WHERE " +
            "(:officeName IS NULL OR LOWER(o.name) = LOWER(:officeName)) AND " +
            "(:status IS NULL OR c.attendanceStatus = :status) AND " +
            "sa.shiftDate BETWEEN :startDate AND :endDate")
    Page<Clocking> findFilteredClockings(
            @Param("officeName") String officeName,
            @Param("status") AttendanceStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    Page<Clocking> findByShiftAssignment_User_IdAndShiftAssignment_ShiftDateBetween(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Optional<Clocking> findByShiftAssignmentId(UUID shiftAssignment);


    @Query("SELECT c FROM Clocking c JOIN c.shiftAssignment sa JOIN sa.user u WHERE " +
            "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(cast(:startDate as date) IS NULL OR sa.shiftDate >= :startDate) AND " +
            "(cast(:endDate as date) IS NULL OR sa.shiftDate <= :endDate)")
    Page<Clocking> findClockingsBySearch(
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT c FROM Clocking c WHERE c.shiftAssignment.user.id = :userId")
    Page<Clocking> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT c FROM Clocking c WHERE c.shiftAssignment.user.id = :userId AND c.shiftAssignment.shiftDate = :date")
    Page<Clocking> findByUserIdAndDate(@Param("userId") UUID userId, @Param("date") LocalDate date, Pageable pageable);

    Optional<Clocking> findByShiftAssignment_User_IdAndShiftAssignment_ShiftDate(
            UUID userId,
            LocalDate shiftDate
    );
}

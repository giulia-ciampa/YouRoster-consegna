package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.AssignmentType;
import giuliaciampa.YouRoster.entities.ShiftAssignment;
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
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {

    boolean existsByUserAndShiftDate(User user, LocalDate shiftDate);

    Page<ShiftAssignment> findByUserAndShiftDateBetween(User user, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Optional<ShiftAssignment> findByUserAndShiftDate(User user, LocalDate shiftDate);

    Page<ShiftAssignment> findByUser(User user, Pageable pageable);


    @Query("SELECT sa FROM ShiftAssignment sa " +
            "WHERE sa.shiftDate = :shiftDate " +
            "AND sa.user != :user " +
            "AND sa.shift IS NOT NULL " +
            "AND sa.shift.office.id = :officeId " +
            "AND sa.shift.startTime < :myEndTime " +
            "AND sa.shift.endTime > :myStartTime")
    List<ShiftAssignment> findOverlappingColleagues(
            @Param("shiftDate") LocalDate shiftDate,
            @Param("user") User user,
            @Param("officeId") UUID officeId,
            @Param("myStartTime") LocalTime myStartTime,
            @Param("myEndTime") LocalTime myEndTime
    );


    @Query("SELECT sa FROM ShiftAssignment sa " +
            "WHERE sa.shiftDate = :shiftDate " +
            "AND sa.shift IS NOT NULL " +
            "AND sa.shift.office.id = :officeId")
    List<ShiftAssignment> findByShiftDateAndOfficeId(
            @Param("shiftDate") LocalDate shiftDate,
            @Param("officeId") UUID officeId
    );


    // 1. Ricerca per data singola con filtro opzionale per nome ufficio e tipo
    @Query("""
            SELECT sa FROM ShiftAssignment sa
            LEFT JOIN sa.shift s
            LEFT JOIN s.office o
            WHERE sa.shiftDate = :shiftDate
            AND (:officeName IS NULL OR o.name = :officeName)
            AND (:assignmentType IS NULL OR sa.assignmentType = :assignmentType)
            """)
    Page<ShiftAssignment> findByDateAndFilters(
            @Param("shiftDate") LocalDate shiftDate,
            @Param("officeName") String officeName,
            @Param("assignmentType") AssignmentType assignmentType,
            Pageable pageable
    );

    // 2. Ricerca per intervallo di date con filtro opzionale per nome ufficio e tipo
    @Query("""
            SELECT sa FROM ShiftAssignment sa
            LEFT JOIN sa.shift s
            LEFT JOIN s.office o
            WHERE sa.shiftDate BETWEEN :startDate AND :endDate
            AND (:officeName IS NULL OR o.name = :officeName)
            AND (:assignmentType IS NULL OR sa.assignmentType = :assignmentType)
            """)
    Page<ShiftAssignment> findByDateBetweenAndFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("officeName") String officeName,
            @Param("assignmentType") AssignmentType assignmentType,
            Pageable pageable
    );

    Optional<ShiftAssignment> findByUserIdAndShiftDate(UUID userId, LocalDate shiftDate);

    List<ShiftAssignment> findByUserAndShiftDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<ShiftAssignment> findByUserAndShiftDateAndShiftStartTimeAndShiftEndTime(
            User user,
            LocalDate shiftDate,
            LocalTime startTime,
            LocalTime endTime
    );

    Optional<ShiftAssignment> findOptionalByUserAndShiftDate(User user, LocalDate shiftDate);

}

package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.ShiftTask;
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
public interface ShiftTaskRepository extends JpaRepository<ShiftTask, UUID> {

    boolean existsByShiftAssignmentIdAndTaskTitleId(UUID shiftAssignmentId, UUID taskTitleId);

    List<ShiftTask> findByShiftAssignmentId(UUID id);

    @Query("SELECT t FROM ShiftTask t WHERE t.shiftAssignment.shiftDate = :date AND t.shiftAssignment.shift.office.id = :officeId")
    Page<ShiftTask> findByDateAndOffice(@Param("date") LocalDate date, @Param("officeId") UUID officeId, Pageable pageable);

    Page<ShiftTask> findByShiftAssignment_ShiftDate(LocalDate shiftDate, Pageable pageable);
}

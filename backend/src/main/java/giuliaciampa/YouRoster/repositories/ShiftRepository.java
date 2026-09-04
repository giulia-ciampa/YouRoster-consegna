package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.Shift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    boolean existsByOfficeNameAndStartTimeAndEndTimeAndIsActiveTrue(String officeName, LocalTime startTime, LocalTime endTime);

    Page<Shift> findByIsActiveTrue(Pageable pageable);

    Page<Shift> findByIsActiveFalse(Pageable pageable);


}

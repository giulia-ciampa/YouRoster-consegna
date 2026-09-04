package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.Office;
import giuliaciampa.YouRoster.entities.OfficeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfficeRepository extends JpaRepository<Office, UUID> {
    boolean existsByName(String name);

    List<Office> findByStatusOrderByNameAsc(OfficeStatus status);

    List<Office> findAllByOrderByNameAsc();

    List<Office> findByStatus(OfficeStatus status);

    Optional<Office> findByNameIgnoreCase(String name);

    Optional<Office> findByNameIgnoreCaseAndStatus(String name, OfficeStatus status);
}

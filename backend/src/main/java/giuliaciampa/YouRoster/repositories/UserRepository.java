package giuliaciampa.YouRoster.repositories;


import giuliaciampa.YouRoster.entities.AccountStatus;
import giuliaciampa.YouRoster.entities.Office;
import giuliaciampa.YouRoster.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByTaxCode(String taxCode);

    boolean existsByDocumentNumber(String documentNumber);

    Optional<User> findByAccount_Id(UUID accountId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.referenceOffice WHERE u.id = :id")
    Optional<User> findByIdWithOffice(@Param("id") UUID id);

    Page<User> findByReferenceOfficeAndAccountRolesName(Office referenceOffice, String roleName, Pageable pageable);

    List<User> findByAccount_Status(AccountStatus status);

   
}

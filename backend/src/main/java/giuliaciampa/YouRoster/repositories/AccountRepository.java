package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.AccountStatus;
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
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRoles_Name(String roleName);

    List<Account> findByRolesNameIn(List<String> roleName);

    Page<Account> findByStatus(AccountStatus accountStatus, Pageable pageable);

    Page<Account> findByRolesNameIgnoreCaseAndStatus(String roleName, AccountStatus status, Pageable pageable);

    List<Account> findByRoles_NameIn(List<String> roleNames);

    Page<Account> findByUser_NameContainingIgnoreCaseAndUser_SurnameContainingIgnoreCaseAndStatus(
            String name,
            String surname,
            AccountStatus status,
            Pageable pageable
    );

    @Query("SELECT a FROM Account a JOIN a.user u JOIN u.referenceOffice o JOIN a.roles r " +
            "WHERE a.status = :status AND r.name = :roleName AND o.id = :officeId")
    Page<Account> findCoordinatorsByOffice(
            @Param("status") AccountStatus status,
            @Param("roleName") String roleName,
            @Param("officeId") UUID officeId,
            Pageable pageable
    );
}

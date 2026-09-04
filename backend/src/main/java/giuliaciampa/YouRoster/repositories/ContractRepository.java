package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.Contract;
import giuliaciampa.YouRoster.entities.ContractStatus;
import giuliaciampa.YouRoster.entities.ContractType;
import giuliaciampa.YouRoster.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    boolean existsByUserAndContractStatus(User user, ContractStatus contractStatus);

    boolean existsByUserAndContractStatusAndIdNot(User user, ContractStatus contractStatus, UUID contractId);

    @Query("SELECT c FROM Contract c WHERE " +
            "(:name IS NULL OR " +
            "LOWER(c.user.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')) OR " +
            "LOWER(c.user.surname) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')) OR " +
            "LOWER(CONCAT(c.user.name, ' ', c.user.surname)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
            "(:startDate IS NULL OR c.startDate = :startDate) AND " +
            "(:contractType IS NULL OR c.contractType = :contractType) AND " +
            "(:contractStatus IS NULL OR c.contractStatus = :contractStatus)")
    Page<Contract> findByOptionalCriteria(
            @Param("name") String name,
            @Param("startDate") LocalDate startDate,
            @Param("contractType") ContractType contractType,
            @Param("contractStatus") ContractStatus contractStatus,
            Pageable pageable
    );

    List<Contract> findByUser(User user);

    Optional<Contract> findByUserAndContractStatus(User user, ContractStatus contractStatus);

    Page<Contract> findByContractStatus(ContractStatus contractStatus, Pageable pageable);

}

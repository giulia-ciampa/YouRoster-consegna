package giuliaciampa.YouRoster.repositories;

import giuliaciampa.YouRoster.entities.Request;
import giuliaciampa.YouRoster.entities.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    @Query("""
                SELECT r
                FROM Request r
                WHERE
                    TYPE(r) <> AbsenceCertificationRequest
                    OR r.requestStatus = :approvedStatus
            """)
    Page<Request> findRequestsForShiftManager(
            @Param("approvedStatus") RequestStatus approvedStatus,
            Pageable pageable
    );

   
}


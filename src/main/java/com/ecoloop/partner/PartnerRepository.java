package com.ecoloop.partner;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface PartnerRepository extends JpaRepository<Partner,UUID>, JpaSpecificationExecutor<Partner>{ List<Partner> findAllByStatus(String status); Optional<Partner> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Partner p WHERE p.id = :id")
    Optional<Partner> findByIdForUpdate(@Param("id") UUID id);
}
=======
import java.util.*;
public interface PartnerRepository extends JpaRepository<Partner,UUID>{ List<Partner> findAllByStatus(String status); Optional<Partner> findByUserId(UUID userId); }
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c

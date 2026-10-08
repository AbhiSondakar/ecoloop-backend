package com.ecoloop.pickup;

import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
<<<<<<< HEAD
import java.time.Instant;
import java.util.*;

public interface PickupRepository extends JpaRepository<PickupRequest,UUID>, JpaSpecificationExecutor<PickupRequest>{
    List<PickupRequest> findAllByUserIdOrderByCreatedAtDesc(UUID userId); 
    Optional<PickupRequest> findByIdAndUserId(UUID id,UUID userId); 
    List<PickupRequest> findAllByPartnerId(UUID partnerId);
    List<PickupRequest> findAllByPartnerIdOrderByCreatedAtDesc(UUID partnerId);
    long countByStatus(String status);
    List<PickupRequest> findAllByCreatedAtAfter(Instant createdAt);
    
    @Query("SELECT p FROM PickupRequest p WHERE p.deviceId = :deviceId AND p.status NOT IN ('completed', 'cancelled')")
    Optional<PickupRequest> findActiveByDeviceId(@Param("deviceId") UUID deviceId);

    @Query("SELECT p FROM PickupRequest p WHERE p.userId = :userId AND p.deviceId = :deviceId AND p.status NOT IN ('completed', 'cancelled')")
    Optional<PickupRequest> findActiveByUserIdAndDeviceId(@Param("userId") UUID userId, @Param("deviceId") UUID deviceId);

    @Query("SELECT COUNT(p) FROM PickupRequest p WHERE p.partnerId = :partnerId AND p.status IN ('accepted', 'in_progress', 'verified')")
    long countActiveJobsByPartnerId(@Param("partnerId") UUID partnerId);

    @Query("SELECT p.partnerId, COUNT(p) FROM PickupRequest p WHERE p.partnerId IN :partnerIds AND p.status IN ('accepted', 'in_progress', 'verified') GROUP BY p.partnerId")
    List<Object[]> countActiveJobsByPartnerIds(@Param("partnerIds") Collection<UUID> partnerIds);

=======
import java.util.*;

public interface PickupRepository extends JpaRepository<PickupRequest,UUID>{
    List<PickupRequest> findAllByUserIdOrderByCreatedAtDesc(UUID userId); 
    Optional<PickupRequest> findByIdAndUserId(UUID id,UUID userId); 
    List<PickupRequest> findAllByPartnerId(UUID partnerId);
    long countByStatus(String status);
    
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PickupRequest p WHERE p.id = :id")
    Optional<PickupRequest> findByIdForUpdate(@Param("id") UUID id);
}

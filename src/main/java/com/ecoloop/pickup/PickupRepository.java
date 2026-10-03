package com.ecoloop.pickup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface PickupRepository extends JpaRepository<PickupRequest,UUID>{
    List<PickupRequest> findAllByUserIdOrderByCreatedAtDesc(UUID userId); 
    Optional<PickupRequest> findByIdAndUserId(UUID id,UUID userId); 
    List<PickupRequest> findAllByPartnerId(UUID partnerId);
    long countByStatus(String status);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PickupRequest p WHERE p.id = :id")
    Optional<PickupRequest> findByIdForUpdate(@Param("id") UUID id);
}

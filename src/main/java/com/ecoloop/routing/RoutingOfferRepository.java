package com.ecoloop.routing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutingOfferRepository extends JpaRepository<RoutingOffer, UUID> {
    List<RoutingOffer> findAllByPartnerIdOrderByCreatedAtDesc(UUID partnerId);
    Optional<RoutingOffer> findByIdAndPartnerId(UUID id, UUID partnerId);
    List<RoutingOffer> findAllByPickupId(UUID pickupId);
}

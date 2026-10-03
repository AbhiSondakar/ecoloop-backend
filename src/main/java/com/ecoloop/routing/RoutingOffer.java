package com.ecoloop.routing;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "routing_offers")
public class RoutingOffer {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "pickup_id", nullable = false)
    private UUID pickupId;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(nullable = false, length = 20)
    private String status = "offered";

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected RoutingOffer() {}

    public RoutingOffer(UUID pickupId, UUID partnerId, Instant expiresAt) {
        this.pickupId = pickupId;
        this.partnerId = partnerId;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPickupId() { return pickupId; }
    public void setPickupId(UUID pickupId) { this.pickupId = pickupId; }
    public UUID getPartnerId() { return partnerId; }
    public void setPartnerId(UUID partnerId) { this.partnerId = partnerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public void accept() {
        if (!"offered".equals(status) || (expiresAt != null && expiresAt.isBefore(Instant.now()))) {
            throw new IllegalStateException("Offer is no longer available");
        }
        status = "accepted";
    }

    public void reject() {
        if (!"offered".equals(status) || (expiresAt != null && expiresAt.isBefore(Instant.now()))) {
            throw new IllegalStateException("Offer is no longer available");
        }
        status = "rejected";
    }
}

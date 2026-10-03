package com.ecoloop.pickup;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pickup_requests")
public class PickupRequest {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "partner_id")
    private UUID partnerId;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected PickupRequest() {}

    public PickupRequest(UUID userId, UUID deviceId, String address) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.address = address;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public UUID getPartnerId() { return partnerId; }
    public void setPartnerId(UUID partnerId) { this.partnerId = partnerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

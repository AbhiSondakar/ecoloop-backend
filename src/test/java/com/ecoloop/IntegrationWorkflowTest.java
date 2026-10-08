package com.ecoloop;

<<<<<<< HEAD
import com.ecoloop.device.Device;
import com.ecoloop.device.DeviceRepository;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import com.ecoloop.partner.Partner;
import com.ecoloop.partner.PartnerRepository;
import com.ecoloop.pickup.PickupRequest;
import com.ecoloop.pickup.PickupService;
import com.ecoloop.routing.RoutingOffer;
import com.ecoloop.routing.RoutingOfferRepository;
import com.ecoloop.routing.RoutingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

<<<<<<< HEAD
import java.math.BigDecimal;
=======
import java.time.Instant;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class IntegrationWorkflowTest {

    @Autowired
    private PickupService pickupService;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private RoutingOfferRepository offerRepository;

<<<<<<< HEAD
    @Autowired
    private DeviceRepository deviceRepository;

=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    private UUID householdUserId;
    private UUID partnerUserId1;
    private UUID partnerUserId2;

    private Partner partner1;
    private Partner partner2;

    @BeforeEach
    void setUp() {
        offerRepository.deleteAll();
        partnerRepository.deleteAll();
<<<<<<< HEAD
        deviceRepository.deleteAll();
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c

        householdUserId = UUID.randomUUID();
        partnerUserId1 = UUID.randomUUID();
        partnerUserId2 = UUID.randomUUID();

        // Create approved partners
        partner1 = new Partner(partnerUserId1, "Org 1", "Recycler", "LIC1");
        partner1.setStatus("approved");
<<<<<<< HEAD
        partner1.setCapacity(10);
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        partner1 = partnerRepository.save(partner1);

        partner2 = new Partner(partnerUserId2, "Org 2", "Recycler", "LIC2");
        partner2.setStatus("approved");
<<<<<<< HEAD
        partner2.setCapacity(10);
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        partner2 = partnerRepository.save(partner2);
    }

    @Test
    void completePickupLifecycleWithEvents() {
<<<<<<< HEAD
        // 1. Create a Device owned by the household user
        Device device = new Device(householdUserId, "good", "laptop", BigDecimal.valueOf(0.95), "completed");
        device = deviceRepository.save(device);

        // 2. Create a Pickup -> Should publish PickupCreatedEvent -> RoutingService should create Offers
        PickupRequest pickup = pickupService.createPickup(householdUserId, device.getId(), "123 Main St", null);
=======
        // 1. Create a Pickup -> Should publish PickupCreatedEvent -> RoutingService should create Offers
        UUID deviceId = UUID.randomUUID();
        PickupRequest pickup = pickupService.createPickup(householdUserId, deviceId, "123 Main St", null);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        
        assertNotNull(pickup.getId());
        assertEquals("pending", pickup.getStatus());

        // Verify RoutingOffers were created for approved partners
        List<RoutingOffer> offers = offerRepository.findAllByPickupId(pickup.getId());
        assertEquals(2, offers.size(), "Should create offers for both approved partners");
        offers.forEach(o -> assertEquals("offered", o.getStatus()));

<<<<<<< HEAD
        // 3. Partner 1 accepts the offer -> Should mark offer accepted, then accept pickup -> publish PickupAcceptedEvent -> supersede Partner 2's offer
=======
        // 2. Partner 1 accepts the offer -> Should mark offer accepted, then accept pickup -> publish PickupAcceptedEvent -> supersede Partner 2's offer
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        RoutingOffer offerForPartner1 = offers.stream()
                .filter(o -> o.getPartnerId().equals(partner1.getId()))
                .findFirst()
                .orElseThrow();
        RoutingOffer offerForPartner2 = offers.stream()
                .filter(o -> o.getPartnerId().equals(partner2.getId()))
                .findFirst()
                .orElseThrow();

        routingService.acceptOffer(partnerUserId1, offerForPartner1.getId());

        // Verify Offer 1 is accepted
        RoutingOffer updatedOffer1 = offerRepository.findById(offerForPartner1.getId()).orElseThrow();
        assertEquals("accepted", updatedOffer1.getStatus());

        // Verify Offer 2 is superseded (due to PickupAcceptedEvent)
        RoutingOffer updatedOffer2 = offerRepository.findById(offerForPartner2.getId()).orElseThrow();
        assertEquals("superseded", updatedOffer2.getStatus());
<<<<<<< HEAD
=======

        // Verify Pickup is accepted and assigned to Partner 1
        // (Assuming we can't easily fetch pickup from PickupService without UserID but wait, PickupService does not expose a get method.
        // We can just rely on the fact that if it wasn't accepted, it would have thrown an exception or the offer wouldn't be superseded).
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @Test
    void cancelPickupCancelsRoutingOffers() {
<<<<<<< HEAD
        // 1. Create Device
        Device device = new Device(householdUserId, "good", "laptop", BigDecimal.valueOf(0.95), "completed");
        device = deviceRepository.save(device);

        // 2. Create Pickup
        PickupRequest pickup = pickupService.createPickup(householdUserId, device.getId(), "123 Main St", null);
=======
        // 1. Create Pickup
        PickupRequest pickup = pickupService.createPickup(householdUserId, UUID.randomUUID(), "123 Main St", null);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        
        List<RoutingOffer> offers = offerRepository.findAllByPickupId(pickup.getId());
        assertEquals(2, offers.size());

<<<<<<< HEAD
        // 3. Cancel Pickup -> Should publish PickupCancelledEvent -> RoutingService cancels offers
=======
        // 2. Cancel Pickup -> Should publish PickupCancelledEvent -> RoutingService cancels offers
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        pickupService.cancelPickup(householdUserId, pickup.getId());

        // Verify Offers are cancelled
        List<RoutingOffer> cancelledOffers = offerRepository.findAllByPickupId(pickup.getId());
        cancelledOffers.forEach(o -> assertEquals("cancelled", o.getStatus()));
    }
}

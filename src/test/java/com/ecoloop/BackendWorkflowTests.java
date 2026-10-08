package com.ecoloop;

import com.ecoloop.classification.api.ClassificationResult;
import com.ecoloop.classification.internal.StubVisionProvider;
import com.ecoloop.routing.RoutingOffer;
<<<<<<< HEAD
import com.ecoloop.routing.RoutingOfferRepository;
import com.ecoloop.routing.RoutingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
=======
import org.junit.jupiter.api.Test;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

<<<<<<< HEAD
@SpringBootTest
@ActiveProfiles("test")
class BackendWorkflowTests {
  @Autowired
  private RoutingOfferRepository routingOfferRepository;

=======
class BackendWorkflowTests {
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
  @Test void stubProviderReturnsDefaultResult() {
    var result = new StubVisionProvider().classify("phone".getBytes(), "image/jpeg");
    assertEquals("other", result.category());
    assertEquals(0.0, result.confidence());
    assertEquals("stub", result.provider());
  }

  @Test void expiredRoutingOfferCannotBeAccepted() {
    var offer = new RoutingOffer(UUID.randomUUID(), UUID.randomUUID(), Instant.now().minusSeconds(1));
    assertThrows(IllegalStateException.class, offer::accept);
  }

<<<<<<< HEAD
  @Test void expiredRoutingOfferCannotBeRejected() {
    var offer = new RoutingOffer(UUID.randomUUID(), UUID.randomUUID(), Instant.now().minusSeconds(60));
    assertThrows(IllegalStateException.class, offer::reject);
    assertThrows(IllegalStateException.class, () -> offer.reject("too late"));
  }

  @Test void validNonExpiredOfferAcceptAndRejectWork() {
    var offer1 = new RoutingOffer(UUID.randomUUID(), UUID.randomUUID(), Instant.now().plusSeconds(60));
    offer1.accept();
    assertEquals("accepted", offer1.getStatus());

    var offer2 = new RoutingOffer(UUID.randomUUID(), UUID.randomUUID(), Instant.now().plusSeconds(60));
    offer2.reject("no thanks");
    assertEquals("rejected", offer2.getStatus());
    assertEquals("no thanks", offer2.getRejectionReason());
  }

=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
  @Test void routingOfferCanBeRejectedOnce() {
    var offer = new RoutingOffer(UUID.randomUUID(), UUID.randomUUID(), Instant.now().plusSeconds(60));
    offer.reject();
    assertEquals("rejected", offer.getStatus());
    assertThrows(IllegalStateException.class, offer::reject);
  }
<<<<<<< HEAD

  @Autowired
  private RoutingService routingService;

  @Test
  @Transactional
  void serviceEnforceExpireTransitionsStatusAndThrows() {
    assertNotNull(routingService, "RoutingService must be reachable from Spring context");
    RoutingOffer stale = routingOfferRepository.save(new RoutingOffer(UUID.randomUUID(), UUID.randomUUID(),
        Instant.now().minusSeconds(30)));
    routingOfferRepository.flush();
    assertEquals("offered", stale.getStatus());

    assertThrows(IllegalStateException.class, () -> routingService.enforceOfferNotExpired(stale));

    routingOfferRepository.flush();
    RoutingOffer refreshed = routingOfferRepository.findById(stale.getId()).orElseThrow();
    assertEquals("expired", refreshed.getStatus(),
        "Service enforcement must write status='expired' for stale offers so the UI no longer shows 'offered'");
  }
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
}

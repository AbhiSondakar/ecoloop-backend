package com.ecoloop;

import com.ecoloop.classification.api.ClassificationResult;
import com.ecoloop.classification.internal.StubVisionProvider;
import com.ecoloop.routing.RoutingOffer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BackendWorkflowTests {
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

  @Test void routingOfferCanBeRejectedOnce() {
    var offer = new RoutingOffer(UUID.randomUUID(), UUID.randomUUID(), Instant.now().plusSeconds(60));
    offer.reject();
    assertEquals("rejected", offer.getStatus());
    assertThrows(IllegalStateException.class, offer::reject);
  }
}

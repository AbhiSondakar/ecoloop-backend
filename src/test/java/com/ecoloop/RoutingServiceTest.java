package com.ecoloop;

import com.ecoloop.routing.RoutingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutingServiceTest {
    @Test
    void scoreUsesWeightedInputs() {
<<<<<<< HEAD
        double score = new RoutingService(null, null, null, null, null).score(true, 1, 1, 1, 1);
=======
        double score = new RoutingService(null, null, null).score(true, 1, 1, 1, 1);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        assertEquals(1.0, score, 0.0001);
    }
}

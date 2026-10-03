package com.ecoloop.classification.internal;

import com.ecoloop.classification.api.ClassificationApi;
import com.ecoloop.classification.api.ClassificationResult;
import com.ecoloop.classification.api.VisionProvider;
import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

    @Service
    public class ClassificationService implements ClassificationApi {

        private static final Logger log = LoggerFactory.getLogger(ClassificationService.class);

        private final List<VisionProvider> providers;
    private final StubVisionProvider stubProvider;
    private final PredictionRepository predictionRepository;

    public ClassificationService(
            List<VisionProvider> providers,
            StubVisionProvider stubProvider,
            PredictionRepository predictionRepository) {
        this.providers = providers;
        this.stubProvider = stubProvider;
        this.predictionRepository = predictionRepository;
    }

    @Override
    public ClassificationResult classify(byte[] image, String mime) {
        return classify(image, mime, null, null);
    }

    @Override
    @Transactional
    public ClassificationResult classify(byte[] image, String mime, UUID deviceId, String imageUrl) {
        long start = System.currentTimeMillis();
        ClassificationResult result = classifyWithFallbackChain(image, mime);
        long latencyMs = System.currentTimeMillis() - start;
        log.info("Classification performed: device={} category={} provider={} model={} confidence={} latency={}ms",
                deviceId, result.category(), result.provider(), result.model(), result.confidence(), latencyMs);

        Prediction prediction = new Prediction();
        prediction.setId(UUID.randomUUID());
        prediction.setRequestId(deviceId);
        prediction.setImageUrl(imageUrl);
        prediction.setCategory(result.category());
        prediction.setConfidence(BigDecimal.valueOf(result.confidence()));
        prediction.setProvider(result.provider());
        prediction.setModel(result.model());
        prediction.setLatencyMs((int) latencyMs);
        prediction.setRawResponse(result.provider() + ":" + result.model());
        prediction.setCreatedAt(Instant.now());
        predictionRepository.save(prediction);

        return result;
    }

    private ClassificationResult classifyWithFallbackChain(byte[] image, String mime) {
        List<VisionProvider> ordered = providers.stream()
            .filter(p -> !(p instanceof StubVisionProvider))
            .sorted((a, b) -> {
                int orderA = providerOrder(a);
                int orderB = providerOrder(b);
                return Integer.compare(orderA, orderB);
            })
            .toList();

        for (VisionProvider provider : ordered) {
            if (!provider.isConfigured()) continue;
            try {
                ClassificationResult result = provider.classify(image, mime);
                if (result != null && !"other".equals(result.category())) {
                    return result;
                }
            } catch (Exception ignored) {
            }
        }

        return stubProvider.classify(image, mime);
    }

    private int providerOrder(VisionProvider p) {
        return switch (p.name()) {
            case "roboflow" -> 1;
            default -> 99;
        };
    }
}

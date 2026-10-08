package com.ecoloop.classification.internal;

import com.ecoloop.classification.api.ClassificationApi;
import com.ecoloop.classification.api.ClassificationResult;
import com.ecoloop.classification.api.VisionProvider;
import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
=======

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

    @Service
    public class ClassificationService implements ClassificationApi {

        private static final Logger log = LoggerFactory.getLogger(ClassificationService.class);

<<<<<<< HEAD
    private final List<VisionProvider> providers;
    private final StubVisionProvider stubProvider;
    private final PredictionRepository predictionRepository;
    private final Semaphore classificationPermits;
=======
        private final List<VisionProvider> providers;
    private final StubVisionProvider stubProvider;
    private final PredictionRepository predictionRepository;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c

    public ClassificationService(
            List<VisionProvider> providers,
            StubVisionProvider stubProvider,
<<<<<<< HEAD
            PredictionRepository predictionRepository,
            @Value("${ecoloop.ai.max-concurrent-classifications:1}") int maxConcurrentClassifications) {
        this.providers = providers;
        this.stubProvider = stubProvider;
        this.predictionRepository = predictionRepository;
        this.classificationPermits = new Semaphore(Math.max(1, maxConcurrentClassifications), true);
=======
            PredictionRepository predictionRepository) {
        this.providers = providers;
        this.stubProvider = stubProvider;
        this.predictionRepository = predictionRepository;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @Override
    public ClassificationResult classify(byte[] image, String mime) {
        return classify(image, mime, null, null);
    }

    @Override
<<<<<<< HEAD
    public ClassificationResult classify(Path imagePath, String mime, UUID deviceId, String imageUrl) {
        boolean permitAcquired = false;
        try {
            classificationPermits.acquire();
            permitAcquired = true;
            return classify(Files.readAllBytes(imagePath), mime, deviceId, imageUrl);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Classification interrupted while waiting for an available processing slot");
            return new ClassificationResult("other", 0.0, "local", "queue", "failed");
        } catch (IOException e) {
            log.warn("Classification image could not be read from managed storage");
            return new ClassificationResult("other", 0.0, "local", "storage", "failed");
        } finally {
            if (permitAcquired) {
                classificationPermits.release();
            }
        }
    }

    @Override
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
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
<<<<<<< HEAD
                if (result != null) {
=======
                if (result != null && !"other".equals(result.category())) {
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
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

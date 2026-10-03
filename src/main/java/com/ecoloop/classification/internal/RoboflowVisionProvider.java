package com.ecoloop.classification.internal;

import com.ecoloop.classification.api.ClassificationResult;
import com.ecoloop.classification.api.VisionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Roboflow Serverless Workflow classification provider.
 * <p>
 * Calls a Roboflow hosted workflow at {@code https://serverless.roboflow.com} using
 * {@code Authorization: Bearer <api_key>}. The image (raw bytes) is sent inline as a
 * base64 data-URL inside the workflow {@code inputs.image} block. The workflow returns
 * predictions; the highest-confidence prediction's class is mapped to one of the canonical
 * e-waste categories, and the real Roboflow confidence score is returned.
 *
 * <p>Configuration:
 * <pre>
 * ai:
 *   roboflow:
 *     api-key: &lt;from ROBOFLOW_API_KEY env&gt;
 *     workflow: abhishek-sondakar-k3rfkb/workflows/e-waste-ve-waste-qmxtt-zuyip/1-yolo26n-t1-logic
 * </pre>
 */
@Component
public class RoboflowVisionProvider implements VisionProvider {

    private static final Logger log = LoggerFactory.getLogger(RoboflowVisionProvider.class);
    private static final Set<String> CANONICAL = Set.of("laptop", "phone", "tablet", "battery", "appliance", "other");
    private static final double CONFIDENCE_FLOOR = 0.2;

    private final RestClient restClient;
    private final String apiKey;
    private final String workflow;

    public RoboflowVisionProvider(@Qualifier("roboflowRestClient") RestClient restClient,
                                  @Value("${ai.roboflow.api-key:}") String apiKey,
                                  @Value("${ai.roboflow.workflow:}") String workflow) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.workflow = workflow;
    }

    @Override
    public String name() {
        return "roboflow";
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && workflow != null && !workflow.isBlank();
    }

    @Override
    public ClassificationResult classify(byte[] image, String mime) {
        String dataUrl = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(image);

        String path = "/" + workflow;

        try {
            ResponseEntity<Map> entity = restClient.post()
                .uri(path)
                .body(dataUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .retrieve()
                .toEntity(Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = entity.getBody();

            if (response == null) {
                log.warn("Roboflow returned null response (workflow={})", workflow);
                return new ClassificationResult("other", 0.0, "roboflow", workflow);
            }

            log.info("Roboflow raw response (workflow={}): {}", workflow, response);

            Prediction top = topPrediction(response);
            if (top == null || top.clazz == null) {
                log.warn("Roboflow returned no usable prediction (workflow={})", workflow);
                return new ClassificationResult("other", 0.0, "roboflow", workflow);
            }

            String category = mapLabel(top.clazz);
            double confidence = top.confidence;
            log.info("Roboflow classified: workflow={} class={} category={} confidence={}",
                    workflow, top.clazz, category, confidence);

            if ("other".equals(category) || confidence < CONFIDENCE_FLOOR) {
                return new ClassificationResult("other", confidence, "roboflow", workflow);
            }
            return new ClassificationResult(category, confidence, "roboflow", workflow);
        } catch (Exception e) {
            log.warn("Roboflow classification failed (workflow={}): {}", workflow, e.getMessage());
            return new ClassificationResult("other", 0.0, "roboflow", workflow);
        }
    }

    @SuppressWarnings("unchecked")
    private static Prediction topPrediction(Map<String, Object> response) {
        if (response == null) return null;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = null;

        // Shape A: {"outputs": [{"predictions": [...]}]}
        List<Map<String, Object>> outputs = (List<Map<String, Object>>) response.get("outputs");
        if (outputs != null && !outputs.isEmpty()) {
            candidates = (List<Map<String, Object>>) outputs.get(0).get("predictions");
        }

        // Shape B: top-level {"predictions": [...]}
        if ((candidates == null || candidates.isEmpty())
                && response.get("predictions") instanceof List) {
            candidates = (List<Map<String, Object>>) response.get("predictions");
        }

        if (candidates == null || candidates.isEmpty()) return null;

        Prediction best = null;
        for (Map<String, Object> p : candidates) {
            Object cls = p.getOrDefault("class", p.getOrDefault("label", null));
            Number conf = (Number) p.get("confidence");
            if (cls == null || conf == null) continue;
            double c = conf.doubleValue();
            if (best == null || c > best.confidence) {
                best = new Prediction(cls.toString(), c);
            }
        }
        return best;
    }

    private static String mapLabel(String label) {
        if (label == null) return "other";
        String l = label.trim().toLowerCase();
        if (l.contains("laptop") || l.contains("notebook") || l.contains("macbook") || l.contains("computer") || l.contains("pc")) return "laptop";
        if (l.contains("phone") || l.contains("smartphone") || l.contains("cellphone") || l.contains("mobile") || l.contains("iphone")) return "phone";
        if (l.contains("tablet") || l.contains("ipad") || l.contains("pad")) return "tablet";
        if (l.contains("battery") || l.contains("power bank")) return "battery";
        if (l.contains("appliance") || l.contains("fridge") || l.contains("refrigerator")
                || l.contains("washer") || l.contains("tv") || l.contains("television")
                || l.contains("monitor") || l.contains("keyboard") || l.contains("mouse")
                || l.contains("camera") || l.contains("headphone") || l.contains("charger")) return "appliance";
        if (CANONICAL.contains(l)) return l;
        // Strip plurals / common suffixes before giving up.
        if (l.endsWith("s")) {
            String singular = l.substring(0, l.length() - 1);
            if (CANONICAL.contains(singular)) return singular;
        }
        return "other";
    }

    private record Prediction(String clazz, double confidence) {}
}

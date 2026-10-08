package com.ecoloop.classification.api;

public record ClassificationResult(
    String category,
    double confidence,
    String provider,
<<<<<<< HEAD
    String model,
    String status
) {
    public ClassificationResult(String category, double confidence, String provider, String model) {
        this(category, confidence, provider, model, "completed");
    }

    public boolean isSuccessful() {
        return "completed".equalsIgnoreCase(status);
    }

    public boolean requiresManualReview() {
        return "manual_review".equalsIgnoreCase(status);
    }

    public String deviceAiStatus() {
        if (requiresManualReview()) {
            return "manual";
        }
        return status == null || status.isBlank() ? "completed" : status;
    }
}
=======
    String model
) {}
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c

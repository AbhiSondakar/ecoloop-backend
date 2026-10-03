package com.ecoloop.classification.api;

public record ClassificationResult(
    String category,
    double confidence,
    String provider,
    String model
) {}

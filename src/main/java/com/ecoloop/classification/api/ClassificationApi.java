package com.ecoloop.classification.api;

import java.util.UUID;

public interface ClassificationApi {
    ClassificationResult classify(byte[] image, String mime);
    ClassificationResult classify(byte[] image, String mime, UUID deviceId, String imageUrl);
}

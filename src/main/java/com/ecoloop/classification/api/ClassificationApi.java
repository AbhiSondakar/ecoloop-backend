package com.ecoloop.classification.api;

<<<<<<< HEAD
import java.nio.file.Path;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import java.util.UUID;

public interface ClassificationApi {
    ClassificationResult classify(byte[] image, String mime);
    ClassificationResult classify(byte[] image, String mime, UUID deviceId, String imageUrl);
<<<<<<< HEAD
    ClassificationResult classify(Path imagePath, String mime, UUID deviceId, String imageUrl);
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
}

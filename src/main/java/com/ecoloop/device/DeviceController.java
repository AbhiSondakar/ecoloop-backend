package com.ecoloop.device;

import com.ecoloop.classification.api.ClassificationApi;
import com.ecoloop.classification.api.ClassificationResult;
<<<<<<< HEAD
import com.ecoloop.common.SessionUser;
import com.ecoloop.common.upload.FileStorageService;
import com.ecoloop.pickup.PickupRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
=======
import com.ecoloop.pickup.PickupRepository;
import com.ecoloop.pickup.PickupRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
<<<<<<< HEAD
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
=======
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    private final ClassificationApi classificationApi;
    private final DeviceRepository devices;
<<<<<<< HEAD
    private final com.ecoloop.pickup.PickupService pickupService;
    private final FileStorageService fileStorageService;

    public DeviceController(ClassificationApi classificationApi,
                            DeviceRepository devices,
                            com.ecoloop.pickup.PickupService pickupService,
                            FileStorageService fileStorageService) {
        this.classificationApi = classificationApi;
        this.devices = devices;
        this.pickupService = pickupService;
        this.fileStorageService = fileStorageService;
    }

    private UUID user(HttpServletRequest r) {
        return SessionUser.require(r).id();
=======
    private final PickupRepository pickups;
    private final com.ecoloop.pickup.PickupService pickupService;
    private final Path uploadDir;

    public DeviceController(ClassificationApi classificationApi, DeviceRepository d, PickupRepository pickups,
                            com.ecoloop.pickup.PickupService pickupService,
                            @Value("${ecoloop.upload.dir:uploads/devices}") String uploadDir) throws IOException {
        this.classificationApi = classificationApi;
        this.devices = d;
        this.pickups = pickups;
        this.pickupService = pickupService;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    private UUID user(HttpServletRequest r) {
        var session = r.getSession(false);
        Object id = session == null ? null : session.getAttribute("USER_ID");
        if (id == null) throw new org.springframework.web.server.ResponseStatusException(
            HttpStatus.UNAUTHORIZED, "Authentication required");
        return UUID.fromString(String.valueOf(id));
    }

    public record Base64Request(
        @NotBlank String imageBase64,
        String condition,
        String mime) {}

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Device submitBase64(@Valid @RequestBody Base64Request body, HttpServletRequest r) throws IOException {
        String base64 = body.imageBase64();
        if (base64.contains(",")) {
            base64 = base64.substring(base64.indexOf(",") + 1);
        }
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        String mime = body.mime() != null ? body.mime() : "image/jpeg";
        String condition = body.condition() != null ? body.condition() : "good";
        String imageUrl = saveImage(imageBytes, mime);
        return createDevice(user(r), imageBytes, mime, condition, imageUrl);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Device submit(@RequestPart("image") MultipartFile image,
                         @RequestParam(defaultValue = "good") String condition,
                         HttpServletRequest r) throws IOException {
<<<<<<< HEAD
        UUID userId = user(r);
        FileStorageService.StoredFile stored = fileStorageService.storeFile(userId, "devices", image, false);
        String mime = stored.metadata().getContentType();
        String imageUrl = stored.publicUri();

        return createDevice(userId, stored.content(), mime, condition, imageUrl);
    }

    private Device createDevice(UUID userId, byte[] image, String mime, String condition, String imageUrl) {
        UUID deviceId = UUID.randomUUID();

        ClassificationResult result = classificationApi.classify(image, mime, deviceId, imageUrl);

        if ("failed".equals(result.status())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, 
                "AI classification failed due to a backend issue. Please try again later."
            );
        }

        if (result.confidence() == 0.0 && !result.requiresManualReview()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, 
                "AI model could not classify the image. Please provide a clearer picture."
            );
        }

        Device device = new Device();
        device.setId(deviceId);
        device.setUserId(userId);
        device.setCondition(condition != null ? condition.trim() : "good");
        device.setImageUrl(imageUrl);
        device.setCreatedAt(Instant.now());
=======
        if (image.isEmpty()) throw new IllegalArgumentException("Image must not be empty");
        byte[] imageBytes = image.getBytes();
        String mime = image.getContentType() != null ? image.getContentType() : "image/jpeg";
        String imageUrl = saveImage(imageBytes, mime);
        return createDevice(user(r), imageBytes, mime, condition, imageUrl);
    }

    private String saveImage(byte[] imageBytes, String mime) throws IOException {
        String ext = "jpg";
        if (mime != null) {
            String type = mime.split(";")[0].trim();
            if (type.contains("/")) {
                ext = type.split("/")[1];
            }
        }
        if ("jpeg".equals(ext)) ext = "jpg";
        String fileName = UUID.randomUUID() + "." + ext;
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, imageBytes);
        return "/api/devices/files/" + fileName;
    }

    private Device createDevice(UUID userId, byte[] imageBytes, String mime, String condition, String imageUrl) {
        Device device = new Device();
        device.setId(UUID.randomUUID());
        device.setUserId(userId);
        device.setCondition(condition);
        device.setImageUrl(imageUrl);
        device.setCreatedAt(Instant.now());
        device.setUpdatedAt(Instant.now());
        device.setAiStatus("pending");
        device = devices.save(device);

        ClassificationResult result = classificationApi.classify(imageBytes, mime, device.getId(), imageUrl);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        device.setCategory(result.category());
        device.setAiCategory(result.category());
        device.setAiConfidence(BigDecimal.valueOf(result.confidence()));
        device.setAiProvider(result.provider());
<<<<<<< HEAD
        device.setAiStatus(result.deviceAiStatus());
        device.setUpdatedAt(Instant.now());
        
        Device saved = devices.save(device);
        log.info("Device created and classified: id={} user={} category={} confidence={}",
                saved.getId(), userId, result.category(), result.confidence());
=======
        device.setAiStatus("completed");
        device.setUpdatedAt(Instant.now());
        Device saved = devices.save(device);
        log.info("Device classified: id={} user={} category={} provider={} confidence={}",
                device.getId(), userId, result.category(), result.provider(), result.confidence());
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        return saved;
    }

    @GetMapping
    public List<Device> list(HttpServletRequest r) {
        return devices.findAllByUserIdOrderByCreatedAtDesc(user(r));
    }

    @GetMapping("/{id}")
    public Device get(@PathVariable UUID id, HttpServletRequest r) {
        return devices.findByIdAndUserId(id, user(r))
            .orElseThrow(() -> new NoSuchElementException("Device not found"));
    }

<<<<<<< HEAD
=======
    @GetMapping("/files/{fileName}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) throws IOException {
        Path filePath = uploadDir.resolve(fileName).normalize();
        if (!filePath.startsWith(uploadDir) || !Files.exists(filePath)) {
            throw new NoSuchElementException("File not found");
        }
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";
        return ResponseEntity.ok()
            .header("Cache-Control", "private, max-age=31536000, immutable")
            .lastModified(Files.getLastModifiedTime(filePath).toMillis())
            .contentType(MediaType.parseMediaType(contentType))
            .body(new org.springframework.core.io.FileSystemResource(filePath.toFile()));
    }

>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    @PostMapping("/{id}/cancel-pickup")
    public PickupRequest cancelPickup(@PathVariable UUID id, HttpServletRequest r) {
        UUID userId = user(r);
        Device device = devices.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new NoSuchElementException("Device not found"));
        return pickupService.cancelPickupByDevice(userId, device.getId());
    }
}

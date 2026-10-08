package com.ecoloop.device;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

<<<<<<< HEAD
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Device> findByIdAndUserId(UUID id, UUID userId);
}
=======
public interface DeviceRepository extends JpaRepository<Device,UUID>{ List<Device> findAllByUserIdOrderByCreatedAtDesc(UUID userId); Optional<Device> findByIdAndUserId(UUID id,UUID userId); }
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c

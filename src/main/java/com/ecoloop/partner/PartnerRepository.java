package com.ecoloop.partner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PartnerRepository extends JpaRepository<Partner,UUID>{ List<Partner> findAllByStatus(String status); Optional<Partner> findByUserId(UUID userId); }

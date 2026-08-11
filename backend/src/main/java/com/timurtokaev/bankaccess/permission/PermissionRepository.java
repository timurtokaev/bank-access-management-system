package com.timurtokaev.bankaccess.permission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository
        extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCode(String code);

    Optional<Permission> findByCodeAndActiveTrue(String code);

    boolean existsByCode(String code);

    List<Permission> findAllByOrderByCodeAsc();

    List<Permission> findAllByActiveTrueOrderByCodeAsc();
}
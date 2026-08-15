package com.timurtokaev.bankaccess.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository
        extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);

    Optional<Role> findByCodeAndActiveTrue(String code);

    boolean existsByCode(String code);

    List<Role> findAllByOrderByCodeAsc();

    List<Role> findAllByActiveTrueOrderByCodeAsc();

    List<Role> findAllBySystemRoleTrueOrderByCodeAsc();
}
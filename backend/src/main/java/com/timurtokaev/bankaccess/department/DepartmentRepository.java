package com.timurtokaev.bankaccess.department;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository
        extends JpaRepository<Department, UUID> {

    Optional<Department> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    List<Department> findAllByActiveTrueOrderByNameAsc();

    List<Department> findAllByParentIsNullOrderByNameAsc();
}
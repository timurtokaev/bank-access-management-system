package com.timurtokaev.bankaccess.department;

import com.timurtokaev.bankaccess.common.error.ConflictException;
import com.timurtokaev.bankaccess.common.error.ResourceNotFoundException;
import com.timurtokaev.bankaccess.department.dto.DepartmentCreateRequest;
import com.timurtokaev.bankaccess.department.dto.DepartmentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentResponse> findAllActive() {
        return departmentRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DepartmentResponse findById(UUID id) {
        Department department = getDepartment(id);
        return toResponse(department);
    }

    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest request) {
        String code = normalizeCode(request.code());
        String name = normalizeName(request.name());

        if (departmentRepository.existsByCode(code)) {
            throw new ConflictException(
                    "Department with code '" + code + "' already exists"
            );
        }

        Department parent = null;

        if (request.parentId() != null) {
            parent = getDepartment(request.parentId());
        }

        Department department = new Department(code, name, parent);
        Department savedDepartment =
                departmentRepository.saveAndFlush(department);

        return toResponse(savedDepartment);
    }

    private Department getDepartment(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: " + id
                ));
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Department code must not be empty"
            );
        }

        String normalizedCode =
                code.trim().toUpperCase(Locale.ROOT);

        if (normalizedCode.length() > 50) {
            throw new IllegalArgumentException(
                    "Department code must not exceed 50 characters"
            );
        }

        return normalizedCode;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Department name must not be empty"
            );
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 150) {
            throw new IllegalArgumentException(
                    "Department name must not exceed 150 characters"
            );
        }

        return normalizedName;
    }

    private DepartmentResponse toResponse(Department department) {
        UUID parentId = department.getParent() == null
                ? null
                : department.getParent().getId();

        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName(),
                parentId,
                department.isActive(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
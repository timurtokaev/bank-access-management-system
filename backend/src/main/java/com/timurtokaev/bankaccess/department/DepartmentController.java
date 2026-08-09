package com.timurtokaev.bankaccess.department;

import com.timurtokaev.bankaccess.department.dto.DepartmentCreateRequest;
import com.timurtokaev.bankaccess.department.dto.DepartmentResponse;
import com.timurtokaev.bankaccess.department.dto.DepartmentUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(
            DepartmentService departmentService
    ) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public List<DepartmentResponse> findAllActive() {
        return departmentService.findAllActive();
    }

    @GetMapping("/{id}")
    public DepartmentResponse findById(
            @PathVariable UUID id
    ) {
        return departmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(
            @Valid @RequestBody DepartmentCreateRequest request
    ) {
        return departmentService.create(request);
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentUpdateRequest request
    ) {
        return departmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @PathVariable UUID id
    ) {
        departmentService.deactivate(id);
    }
}
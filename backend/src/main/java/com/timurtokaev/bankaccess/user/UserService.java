package com.timurtokaev.bankaccess.user;

import com.timurtokaev.bankaccess.common.error.ConflictException;
import com.timurtokaev.bankaccess.common.error.ResourceNotFoundException;
import com.timurtokaev.bankaccess.department.Department;
import com.timurtokaev.bankaccess.department.DepartmentRepository;
import com.timurtokaev.bankaccess.user.dto.UserCreateRequest;
import com.timurtokaev.bankaccess.user.dto.UserResponse;
import com.timurtokaev.bankaccess.user.dto.UserUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionRevoker userSessionRevoker;

    public UserService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder,
            UserSessionRevoker userSessionRevoker
    ) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSessionRevoker = userSessionRevoker;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse findById(UUID id) {
        return toResponse(getUser(id));
    }

    public List<UserResponse> findAllByStatus(
            UserStatus status
    ) {
        return userRepository
                .findAllByStatusOrderByLastNameAscFirstNameAsc(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<UserResponse> findAllByDepartment(
            UUID departmentId
    ) {
        return userRepository
                .findAllByDepartmentIdOrderByLastNameAscFirstNameAsc(
                        departmentId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse create(
            UserCreateRequest request
    ) {
        String employeeNumber =
                normalizeEmployeeNumber(request.employeeNumber());
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());
        String firstName = normalizeName(
                request.firstName(),
                "First name"
        );
        String lastName = normalizeName(
                request.lastName(),
                "Last name"
        );

        validateUniqueForCreate(
                employeeNumber,
                username,
                email
        );

        Department department =
                getActiveDepartment(request.departmentId());

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User(
                employeeNumber,
                username,
                email,
                passwordHash,
                firstName,
                lastName,
                department
        );

        User savedUser = userRepository.saveAndFlush(user);

        return toResponse(savedUser);
    }

    @Transactional
    public UserResponse update(
            UUID id,
            UserUpdateRequest request
    ) {
        User user = getUserForUpdate(id);

        String employeeNumber =
                normalizeEmployeeNumber(request.employeeNumber());
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());
        String firstName = normalizeName(
                request.firstName(),
                "First name"
        );
        String lastName = normalizeName(
                request.lastName(),
                "Last name"
        );

        validateUniqueForUpdate(
                id,
                employeeNumber,
                username,
                email
        );

        Department department =
                getActiveDepartment(request.departmentId());

        UserStatus newStatus = request.status();

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "User status must not be null"
            );
        }

        UserStatus previousStatus = user.getStatus();

        boolean revokeSessions =
                previousStatus != UserStatus.ACTIVE
                        || newStatus != UserStatus.ACTIVE;

        user.setEmployeeNumber(employeeNumber);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setDepartment(department);
        user.changeStatus(newStatus);

        if (newStatus == UserStatus.ACTIVE
                && previousStatus != UserStatus.ACTIVE) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        if (newStatus == UserStatus.INACTIVE) {
            user.setLockedUntil(null);
        }

        User savedUser = userRepository.saveAndFlush(user);

        if (revokeSessions) {
            userSessionRevoker.revokeAllActiveForUser(id);
        }

        return toResponse(savedUser);
    }

    @Transactional
    public void deactivate(UUID id) {
        User user = getUserForUpdate(id);

        if (user.getStatus() != UserStatus.INACTIVE) {
            user.changeStatus(UserStatus.INACTIVE);
            user.setLockedUntil(null);

            userRepository.saveAndFlush(user);
        }

        userSessionRevoker.revokeAllActiveForUser(id);
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + id
                ));
    }

    private User getUserForUpdate(UUID id) {
        return userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + id
                ));
    }

    private Department getActiveDepartment(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: " + id
                ));

        if (!department.isActive()) {
            throw new IllegalArgumentException(
                    "Department is inactive: " + id
            );
        }

        return department;
    }

    private void validateUniqueForCreate(
            String employeeNumber,
            String username,
            String email
    ) {
        if (userRepository.existsByEmployeeNumber(employeeNumber)) {
            throw new ConflictException(
                    "User with employee number '"
                            + employeeNumber
                            + "' already exists"
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException(
                    "User with username '"
                            + username
                            + "' already exists"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(
                    "User with email '"
                            + email
                            + "' already exists"
            );
        }
    }

    private void validateUniqueForUpdate(
            UUID id,
            String employeeNumber,
            String username,
            String email
    ) {
        if (userRepository.existsByEmployeeNumberAndIdNot(
                employeeNumber,
                id
        )) {
            throw new ConflictException(
                    "User with employee number '"
                            + employeeNumber
                            + "' already exists"
            );
        }

        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new ConflictException(
                    "User with username '"
                            + username
                            + "' already exists"
            );
        }

        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new ConflictException(
                    "User with email '"
                            + email
                            + "' already exists"
            );
        }
    }

    private String normalizeEmployeeNumber(
            String employeeNumber
    ) {
        return normalizeRequired(
                employeeNumber,
                "Employee number"
        ).toUpperCase(Locale.ROOT);
    }

    private String normalizeUsername(String username) {
        return normalizeRequired(
                username,
                "Username"
        ).toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return normalizeRequired(
                email,
                "Email"
        ).toLowerCase(Locale.ROOT);
    }

    private String normalizeName(
            String name,
            String fieldName
    ) {
        return normalizeRequired(name, fieldName);
    }

    private String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be empty"
            );
        }

        return value.trim();
    }

    private UserResponse toResponse(User user) {
        Department department = user.getDepartment();

        return new UserResponse(
                user.getId(),
                user.getEmployeeNumber(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                department.getId(),
                department.getCode(),
                department.getName(),
                user.getStatus(),
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

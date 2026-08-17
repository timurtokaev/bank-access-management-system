package com.timurtokaev.bankaccess.user;

import com.timurtokaev.bankaccess.department.Department;
import com.timurtokaev.bankaccess.department.DepartmentRepository;
import com.timurtokaev.bankaccess.user.dto.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final UUID USER_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000001"
    );

    private static final UUID DEPARTMENT_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000002"
    );

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserSessionRevoker userSessionRevoker;

    private UserService userService;
    private Department department;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                departmentRepository,
                passwordEncoder,
                userSessionRevoker
        );

        department = new Department(
                "SEC",
                "Security",
                null
        );

        ReflectionTestUtils.setField(
                department,
                "id",
                DEPARTMENT_ID
        );
    }

    @Test
    void shouldAdvanceAuthenticationVersionOnlyForRealStatusChanges() {
        User user = createUser();

        assertFalse(user.changeStatus(UserStatus.ACTIVE));
        assertEquals(0L, user.getAuthVersion());

        assertTrue(user.changeStatus(UserStatus.INACTIVE));
        assertEquals(1L, user.getAuthVersion());

        assertFalse(user.changeStatus(UserStatus.INACTIVE));
        assertEquals(1L, user.getAuthVersion());

        assertTrue(user.changeStatus(UserStatus.LOCKED));
        assertEquals(2L, user.getAuthVersion());

        assertTrue(user.changeStatus(UserStatus.ACTIVE));
        assertEquals(3L, user.getAuthVersion());
    }

    @Test
    void shouldNotRevokeSessionsForActiveToActiveUpdate() {
        User user = createUser();
        stubUpdate(user);

        userService.update(
                USER_ID,
                updateRequest(UserStatus.ACTIVE)
        );

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(0L, user.getAuthVersion());

        verify(
                userSessionRevoker,
                never()
        ).revokeAllActiveForUser(any(UUID.class));
    }

    @Test
    void shouldRevokeAfterActiveToInactiveUpdate() {
        User user = createUser();
        stubUpdate(user);

        userService.update(
                USER_ID,
                updateRequest(UserStatus.INACTIVE)
        );

        assertEquals(UserStatus.INACTIVE, user.getStatus());
        assertEquals(1L, user.getAuthVersion());

        verifySaveThenRevoke(user);
    }

    @Test
    void shouldRevokeBeforeCompletingInactiveToActiveUpdate() {
        User user = createUser();
        user.changeStatus(UserStatus.INACTIVE);
        user.setFailedLoginAttempts(4);
        stubUpdate(user);

        userService.update(
                USER_ID,
                updateRequest(UserStatus.ACTIVE)
        );

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(2L, user.getAuthVersion());
        assertEquals(0, user.getFailedLoginAttempts());

        verifySaveThenRevoke(user);
    }

    @Test
    void shouldRevokeOnRepeatedInactiveDeactivateForLegacyCleanup() {
        User user = createUser();
        user.changeStatus(UserStatus.INACTIVE);

        when(userRepository.findByIdForUpdate(USER_ID))
                .thenReturn(Optional.of(user));

        userService.deactivate(USER_ID);

        assertEquals(1L, user.getAuthVersion());

        verify(
                userRepository,
                never()
        ).saveAndFlush(any(User.class));

        verify(userSessionRevoker)
                .revokeAllActiveForUser(USER_ID);
    }

    @Test
    void shouldPropagateRevocationFailureAfterStatusSave() {
        User user = createUser();

        when(userRepository.findByIdForUpdate(USER_ID))
                .thenReturn(Optional.of(user));

        when(userRepository.saveAndFlush(user))
                .thenReturn(user);

        when(
                userSessionRevoker.revokeAllActiveForUser(
                        USER_ID
                )
        ).thenThrow(
                new IllegalStateException("revocation failed")
        );

        assertThrows(
                IllegalStateException.class,
                () -> userService.deactivate(USER_ID)
        );

        verifySaveThenRevoke(user);
    }

    private void stubUpdate(User user) {
        when(userRepository.findByIdForUpdate(USER_ID))
                .thenReturn(Optional.of(user));

        when(departmentRepository.findById(DEPARTMENT_ID))
                .thenReturn(Optional.of(department));

        when(userRepository.saveAndFlush(user))
                .thenReturn(user);
    }

    private void verifySaveThenRevoke(User user) {
        InOrder order = inOrder(
                userRepository,
                userSessionRevoker
        );

        order.verify(userRepository).saveAndFlush(user);
        order.verify(userSessionRevoker)
                .revokeAllActiveForUser(USER_ID);
    }

    private User createUser() {
        User user = new User(
                "EMP_001",
                "admin",
                "admin@example.com",
                "stored-password-hash",
                "Local",
                "Administrator",
                department
        );

        ReflectionTestUtils.setField(user, "id", USER_ID);

        return user;
    }

    private UserUpdateRequest updateRequest(
            UserStatus status
    ) {
        return new UserUpdateRequest(
                "EMP_001",
                "admin",
                "admin@example.com",
                "Local",
                "Administrator",
                DEPARTMENT_ID,
                status
        );
    }
}

package com.timurtokaev.bankaccess.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeNumber(String employeeNumber);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmployeeNumber(String employeeNumber);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByEmployeeNumberAndIdNot(
            String employeeNumber,
            UUID id
    );

    List<User> findAllByStatusOrderByLastNameAscFirstNameAsc(
            UserStatus status
    );

    List<User> findAllByDepartmentIdOrderByLastNameAscFirstNameAsc(
            UUID departmentId
    );
}
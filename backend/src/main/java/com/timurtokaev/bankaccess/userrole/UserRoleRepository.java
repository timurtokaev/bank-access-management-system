package com.timurtokaev.bankaccess.userrole;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UserRoleId> {

    @EntityGraph(
            attributePaths = {
                    "user",
                    "role",
                    "assignedBy"
            }
    )
    List<UserRole> findAllByUser_IdOrderByRole_CodeAsc(
            UUID userId
    );

    @EntityGraph(
            attributePaths = {
                    "user",
                    "role",
                    "assignedBy"
            }
    )
    List<UserRole>
    findAllByRole_IdOrderByUser_LastNameAscUser_FirstNameAsc(
            UUID roleId
    );
}
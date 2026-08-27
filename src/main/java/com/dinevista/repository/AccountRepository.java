package com.dinevista.repository;

import com.dinevista.model.UserAccountRecord;

import java.util.Optional;

public interface AccountRepository {
    Optional<UserAccountRecord> findByEmailAndRole(String email, String role);
    boolean emailExists(String email);
    UserAccountRecord create(String role, String firstName, String lastName,
                             String email, String phone, String passwordHash);
}

package com.humanize.auth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionEntity, String> {
}

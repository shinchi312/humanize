package com.humanize.auth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserJpaRepository extends JpaRepository<AuthUserEntity, String> {
}

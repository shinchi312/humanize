package com.humanize.library;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryBookJpaRepository extends JpaRepository<LibraryBookEntity, String> {
}

package com.humanize.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookProcessingStateJpaRepository extends JpaRepository<BookProcessingStateEntity, String> {
}

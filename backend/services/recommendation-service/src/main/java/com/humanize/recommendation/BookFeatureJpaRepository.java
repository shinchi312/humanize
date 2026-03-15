package com.humanize.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookFeatureJpaRepository extends JpaRepository<BookFeatureEntity, String> {
}

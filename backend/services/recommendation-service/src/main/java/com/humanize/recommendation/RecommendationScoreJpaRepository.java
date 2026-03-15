package com.humanize.recommendation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationScoreJpaRepository extends JpaRepository<RecommendationScoreEntity, String> {
    List<RecommendationScoreEntity> findTop20ByUserIdOrderByScoreDescUpdatedAtDesc(String userId);
}

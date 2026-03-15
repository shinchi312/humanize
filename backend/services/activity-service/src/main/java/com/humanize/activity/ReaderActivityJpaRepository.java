package com.humanize.activity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderActivityJpaRepository extends JpaRepository<ReaderActivityEntity, String> {
    List<ReaderActivityEntity> findTop100ByUserIdOrderByCreatedAtDesc(String userId);
    List<ReaderActivityEntity> findTop100ByBookIdOrderByCreatedAtDesc(String bookId);
}

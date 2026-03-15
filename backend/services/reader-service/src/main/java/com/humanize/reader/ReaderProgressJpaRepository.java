package com.humanize.reader;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderProgressJpaRepository extends JpaRepository<ReaderProgressEntity, String> {
    List<ReaderProgressEntity> findByUserIdOrderByUpdatedAtDesc(String userId);
}

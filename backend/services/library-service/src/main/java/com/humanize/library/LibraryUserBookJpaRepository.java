package com.humanize.library;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryUserBookJpaRepository extends JpaRepository<LibraryUserBookEntity, String> {
    List<LibraryUserBookEntity> findByUserIdOrderByUpdatedAtDesc(String userId);
}

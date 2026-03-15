package com.humanize.recommendation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookSignalJpaRepository extends JpaRepository<UserBookSignalEntity, String> {
    List<UserBookSignalEntity> findByBookId(String bookId);
}

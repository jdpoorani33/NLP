package com.nlp.platform.repository;

import com.nlp.platform.model.LeetCodeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeetCodeDataRepository extends JpaRepository<LeetCodeData, Long> {
}

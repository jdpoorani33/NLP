package com.nlp.platform.repository;

import com.nlp.platform.model.StudentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentClassRepository extends JpaRepository<StudentClass, Long> {
    Optional<StudentClass> findByClassName(String className);
    boolean existsByClassName(String className);
}

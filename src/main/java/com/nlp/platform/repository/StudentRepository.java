package com.nlp.platform.repository;

import com.nlp.platform.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRegisterNumber(String registerNumber);

    boolean existsByRegisterNumber(String registerNumber);

    boolean existsByRegisterNumberAndIdNot(String registerNumber, Long id);

    List<Student> findByStudentClassId(Long classId);

    List<Student> findAllByOrderByOverallScoreDesc();

    List<Student> findByStudentClassIdOrderByOverallScoreDesc(Long classId);

    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.registerNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.leetcodeUsername) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Student> searchStudents(@Param("query") String query);

    @Query("SELECT s FROM Student s WHERE " +
           "(:classId IS NULL OR s.studentClass.id = :classId) AND " +
           "(:dept IS NULL OR s.department = :dept) AND " +
           "(:year IS NULL OR s.year = :year) AND " +
           "(:sec IS NULL OR s.section = :sec) AND " +
           "(:status IS NULL OR s.status = :status)")
    List<Student> filterStudents(@Param("classId") Long classId,
                                 @Param("dept") String dept,
                                 @Param("year") Integer year,
                                 @Param("sec") String sec,
                                 @Param("status") Student.ProfileStatus status);
}

package com.nlp.platform.config;

import com.nlp.platform.model.LeetCodeData;
import com.nlp.platform.model.Student;
import com.nlp.platform.model.StudentClass;
import com.nlp.platform.repository.LeetCodeDataRepository;
import com.nlp.platform.repository.StudentClassRepository;
import com.nlp.platform.repository.StudentRepository;
import com.nlp.platform.service.RankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final StudentClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final LeetCodeDataRepository leetCodeDataRepository;
    private final RankingService rankingService;

    public DataInitializer(StudentClassRepository classRepository,
                           StudentRepository studentRepository,
                           LeetCodeDataRepository leetCodeDataRepository,
                           RankingService rankingService) {
        this.classRepository = classRepository;
        this.studentRepository = studentRepository;
        this.leetCodeDataRepository = leetCodeDataRepository;
        this.rankingService = rankingService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (classRepository.count() == 0 && studentRepository.count() == 0) {
            logger.info("Initializing sample classes and students in database...");

            // Create Sample Classes
            StudentClass classA = new StudentClass("B.Tech IT - III Year - A Section", "Information Technology", 3, "A", "2023-2027");
            StudentClass classB = new StudentClass("B.Tech CSE - III Year - B Section", "Computer Science", 3, "B", "2023-2027");
            classRepository.save(classA);
            classRepository.save(classB);

            // Create Sample Students
            createSampleStudent("23IT101", "Rahul Kumar", "rahul@example.com", "Information Technology", 3, "A", 9.2, "rahulkumar123", classA, 350, 180, 140, 30, 1780, 15);
            createSampleStudent("23IT102", "Priya Sharma", "priya@example.com", "Information Technology", 3, "A", 9.5, "priyasharma_algo", classA, 420, 200, 180, 40, 1850, 22);
            createSampleStudent("23IT103", "Anish Verma", "anish@example.com", "Information Technology", 3, "A", 8.8, "anish_v", classA, 280, 150, 110, 20, 1620, 10);
            createSampleStudent("23CS201", "Sneha Roy", "sneha@example.com", "Computer Science", 3, "B", 9.1, "sneha_codes", classB, 490, 220, 210, 60, 1920, 28);
            createSampleStudent("23CS202", "Karthik Raja", "karthik@example.com", "Computer Science", 3, "B", 8.4, null, classB, 0, 0, 0, 0, 0, 0);
            createSampleStudent("23CS203", "Divya Menon", "divya@example.com", "Computer Science", 3, "B", 9.0, "divyam_leetcode", classB, 310, 160, 130, 20, 1580, 8);

            // Recalculate Rankings
            rankingService.recalculateAllRankings();
            logger.info("Sample dataset successfully loaded into database.");
        }
    }

    private void createSampleStudent(String regNo, String name, String email, String dept, int year, 
                                     String sec, double cgpa, String lcUser, StudentClass sClass,
                                     int total, int easy, int med, int hard, int rating, int count) {
        Student student = new Student(regNo, name, email, dept, year, sec, cgpa, lcUser);
        student.setStudentClass(sClass);
        student.setLastUpdated(LocalDateTime.now());

        if (lcUser != null && !lcUser.isEmpty()) {
            LeetCodeData lcData = new LeetCodeData(total, easy, med, hard, rating, count);
            lcData.setStatusMessage("Sample profile data loaded.");
            student.setLeetcodeData(lcData);
            student.setStatus(Student.ProfileStatus.CONNECTED);
        } else {
            student.setStatus(Student.ProfileStatus.NOT_CONNECTED);
        }

        studentRepository.save(student);
    }
}

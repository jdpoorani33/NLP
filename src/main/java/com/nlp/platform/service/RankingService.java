package com.nlp.platform.service;

import com.nlp.platform.model.LeetCodeData;
import com.nlp.platform.model.Student;
import com.nlp.platform.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class RankingService {

    private final StudentRepository studentRepository;

    public RankingService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Calculates the performance scores for a single student.
     * Formula:
     * Academic Score (40%): (CGPA / 10.0) * 100
     * Problem Solving Score (40%): Raw = (Easy*1 + Med*2 + Hard*3), Normalized to max target
     * Contest Score (20%): Normalized contest rating + participation bonus
     */
    public void calculateStudentScore(Student student) {
        // 1. Academic Score (0-100)
        double cgpa = student.getCgpa() != null ? student.getCgpa() : 0.0;
        double academicScore = (cgpa / 10.0) * 100.0;

        // 2. Problem Solving Score (0-100)
        double problemSolvingScore = 0.0;
        LeetCodeData lcData = student.getLeetcodeData();
        if (lcData != null) {
            int easy = lcData.getEasySolved();
            int medium = lcData.getMediumSolved();
            int hard = lcData.getHardSolved();
            int rawProblemPoints = (easy * 1) + (medium * 2) + (hard * 3);
            
            // Normalize problem score against 600 points benchmark
            problemSolvingScore = Math.min(100.0, (rawProblemPoints / 600.0) * 100.0);
        }

        // 3. Contest Score (0-100)
        double contestScore = 0.0;
        if (lcData != null && lcData.getContestRating() > 0) {
            int rating = lcData.getContestRating();
            int contestCount = lcData.getContestCount();
            
            double ratingComponent = Math.min(80.0, (rating / 2000.0) * 80.0);
            double participationComponent = Math.min(20.0, contestCount * 2.0);
            contestScore = ratingComponent + participationComponent;
        }

        // 4. Overall Weighted Score (40% Academic + 40% Problem Solving + 20% Contest)
        double overallScore = (0.40 * academicScore) + (0.40 * problemSolvingScore) + (0.20 * contestScore);

        student.setAcademicScore(roundToTwoDecimals(academicScore));
        student.setProblemSolvingScore(roundToTwoDecimals(problemSolvingScore));
        student.setContestScore(roundToTwoDecimals(contestScore));
        student.setOverallScore(roundToTwoDecimals(overallScore));
    }

    /**
     * Recalculates scores and updates rankings for all students in the database.
     * Implements deterministic tie handling.
     */
    @Transactional
    public List<Student> recalculateAllRankings() {
        List<Student> students = studentRepository.findAll();
        
        // Calculate individual scores
        for (Student student : students) {
            calculateStudentScore(student);
        }

        // Sort students deterministically (Req 12)
        students.sort(new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                // Primary: Overall Score Descending
                int scoreComp = Double.compare(s2.getOverallScore(), s1.getOverallScore());
                if (scoreComp != 0) return scoreComp;

                // Tie-Breaker 1: Problem Solving Score Descending
                int probComp = Double.compare(s2.getProblemSolvingScore(), s1.getProblemSolvingScore());
                if (probComp != 0) return probComp;

                // Tie-Breaker 2: Contest Rating Descending
                int s1Rating = s1.getLeetcodeData() != null ? s1.getLeetcodeData().getContestRating() : 0;
                int s2Rating = s2.getLeetcodeData() != null ? s2.getLeetcodeData().getContestRating() : 0;
                int ratingComp = Integer.compare(s2Rating, s1Rating);
                if (ratingComp != 0) return ratingComp;

                // Tie-Breaker 3: CGPA Descending
                int cgpaComp = Double.compare(s2.getCgpa(), s1.getCgpa());
                if (cgpaComp != 0) return cgpaComp;

                // Tie-Breaker 4: Name Alphabetical Ascending
                return s1.getName().compareToIgnoreCase(s2.getName());
            }
        });

        // Assign ranks and save to DB
        int rank = 1;
        for (Student student : students) {
            student.setRank(rank++);
        }

        return studentRepository.saveAll(students);
    }

    /**
     * Recalculates rankings specifically for a selected class.
     */
    @Transactional
    public List<Student> recalculateClassRankings(Long classId) {
        List<Student> students = studentRepository.findByStudentClassId(classId);
        
        for (Student student : students) {
            calculateStudentScore(student);
        }

        students.sort((s1, s2) -> {
            int scoreComp = Double.compare(s2.getOverallScore(), s1.getOverallScore());
            if (scoreComp != 0) return scoreComp;

            int probComp = Double.compare(s2.getProblemSolvingScore(), s1.getProblemSolvingScore());
            if (probComp != 0) return probComp;

            int s1Rating = s1.getLeetcodeData() != null ? s1.getLeetcodeData().getContestRating() : 0;
            int s2Rating = s2.getLeetcodeData() != null ? s2.getLeetcodeData().getContestRating() : 0;
            int ratingComp = Integer.compare(s2Rating, s1Rating);
            if (ratingComp != 0) return ratingComp;

            return Double.compare(s2.getCgpa(), s1.getCgpa());
        });

        int rank = 1;
        for (Student student : students) {
            student.setRank(rank++);
        }

        return studentRepository.saveAll(students);
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}

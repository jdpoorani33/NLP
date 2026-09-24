package com.nlp.platform.controller;

import com.nlp.platform.model.Student;
import com.nlp.platform.model.StudentClass;
import com.nlp.platform.service.RankingService;
import com.nlp.platform.service.StudentClassService;
import com.nlp.platform.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class DashboardController {

    private final StudentService studentService;
    private final StudentClassService classService;
    private final RankingService rankingService;

    public DashboardController(StudentService studentService,
                               StudentClassService classService,
                               RankingService rankingService) {
        this.studentService = studentService;
        this.classService = classService;
        this.rankingService = rankingService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@RequestParam(value = "classId", required = false) Long classId,
                            Model model) {
        List<StudentClass> classes = classService.getAllClasses();
        List<Student> students;

        if (classId != null && classId > 0) {
            students = studentService.getStudentsByClass(classId);
            model.addAttribute("selectedClassId", classId);
        } else {
            students = studentService.getAllStudents();
            model.addAttribute("selectedClassId", null);
        }

        // Summary Cards Metrics
        int totalStudents = students.size();
        long connectedProfiles = students.stream()
            .filter(s -> s.getStatus() == Student.ProfileStatus.CONNECTED)
            .count();

        double avgCgpa = students.stream()
            .mapToDouble(s -> s.getCgpa() != null ? s.getCgpa() : 0.0)
            .average()
            .orElse(0.0);

        double avgProblemsSolved = students.stream()
            .mapToInt(s -> s.getLeetcodeData() != null ? s.getLeetcodeData().getTotalSolved() : 0)
            .average()
            .orElse(0.0);

        double topScore = students.stream()
            .mapToDouble(Student::getOverallScore)
            .max()
            .orElse(0.0);

        // Top 3 Performers (Req 14)
        List<Student> topPerformers = students.stream()
            .limit(3)
            .toList();

        // Last Refresh Timestamp
        String lastRefreshTime = students.stream()
            .filter(s -> s.getLastUpdated() != null)
            .map(Student::getLastUpdated)
            .max(LocalDateTime::compareTo)
            .map(dt -> dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")))
            .orElse("N/A");

        model.addAttribute("classes", classes);
        model.addAttribute("students", students);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("connectedProfiles", connectedProfiles);
        model.addAttribute("avgCgpa", Math.round(avgCgpa * 100.0) / 100.0);
        model.addAttribute("avgProblemsSolved", Math.round(avgProblemsSolved * 10.0) / 10.0);
        model.addAttribute("topScore", topScore);
        model.addAttribute("topPerformers", topPerformers);
        model.addAttribute("lastRefreshTime", lastRefreshTime);

        return "dashboard";
    }

    @GetMapping("/rankings")
    public String viewRankings(@RequestParam(value = "classId", required = false) Long classId, Model model) {
        return dashboard(classId, model);
    }
}

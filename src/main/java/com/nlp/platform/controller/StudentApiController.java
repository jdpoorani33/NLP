package com.nlp.platform.controller;

import com.nlp.platform.dto.StudentDTO;
import com.nlp.platform.model.Student;
import com.nlp.platform.service.RankingService;
import com.nlp.platform.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StudentApiController {

    private final StudentService studentService;
    private final RankingService rankingService;

    public StudentApiController(StudentService studentService, RankingService rankingService) {
        this.studentService = studentService;
        this.rankingService = rankingService;
    }

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable("id") Long id) {
        return studentService.getStudentById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null));
    }

    @PostMapping("/students")
    public ResponseEntity<?> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        try {
            Student savedStudent = studentService.saveStudent(studentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Error creating student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable("id") Long id, @Valid @RequestBody StudentDTO studentDTO) {
        try {
            studentDTO.setId(id);
            Student updatedStudent = studentService.saveStudent(studentDTO);
            return ResponseEntity.ok(updatedStudent);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Error updating student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable("id") Long id) {
        try {
            studentService.deleteStudent(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Student deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Failed to delete student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @PostMapping("/students/{id}/leetcode/connect")
    public ResponseEntity<?> connectLeetCodeProfile(@PathVariable("id") Long id, 
                                                    @RequestBody Map<String, String> payload) {
        String username = payload.get("leetcodeUsername");
        if (username == null || username.trim().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "LeetCode username must be entered manually by faculty.");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            Student student = studentService.connectLeetCodeProfile(id, username);
            return ResponseEntity.ok(student);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unable to retrieve profile for username '" + username.trim() + "'. Please verify the username.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }

    @PostMapping("/students/{id}/leetcode/refresh")
    public ResponseEntity<?> refreshLeetCodeData(@PathVariable("id") Long id) {
        try {
            boolean success = studentService.refreshLeetCodeData(id);
            Student student = studentService.getStudentById(id).orElse(null);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("student", student);
            if (!success) {
                response.put("message", "Last refresh failed — showing previous valid data.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping("/classes/{classId}/ranking")
    public ResponseEntity<List<Student>> getClassRanking(@PathVariable("classId") Long classId) {
        List<Student> ranking = rankingService.recalculateClassRankings(classId);
        return ResponseEntity.ok(ranking);
    }
}

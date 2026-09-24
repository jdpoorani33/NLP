package com.nlp.platform.service;

import com.nlp.platform.dto.StudentDTO;
import com.nlp.platform.model.Student;
import com.nlp.platform.model.StudentClass;
import com.nlp.platform.repository.StudentClassRepository;
import com.nlp.platform.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentClassRepository classRepository;
    private final RankingService rankingService;
    private final LeetCodeService leetCodeService;

    public StudentService(StudentRepository studentRepository,
                          StudentClassRepository classRepository,
                          RankingService rankingService,
                          LeetCodeService leetCodeService) {
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.rankingService = rankingService;
        this.leetCodeService = leetCodeService;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAllByOrderByOverallScoreDesc();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByRegisterNumber(String registerNumber) {
        return studentRepository.findByRegisterNumber(registerNumber);
    }

    public List<Student> getStudentsByClass(Long classId) {
        return studentRepository.findByStudentClassIdOrderByOverallScoreDesc(classId);
    }

    @Transactional
    public Student saveStudent(StudentDTO dto) throws Exception {
        // Validation: Unique Register Number
        if (dto.getId() == null) {
            if (studentRepository.existsByRegisterNumber(dto.getRegisterNumber())) {
                throw new IllegalArgumentException("Student with Register Number '" + dto.getRegisterNumber() + "' already exists.");
            }
        } else {
            if (studentRepository.existsByRegisterNumberAndIdNot(dto.getRegisterNumber(), dto.getId())) {
                throw new IllegalArgumentException("Register Number '" + dto.getRegisterNumber() + "' is already assigned to another student.");
            }
        }

        Student student;
        if (dto.getId() != null) {
            student = studentRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student with ID " + dto.getId() + " not found."));
        } else {
            student = new Student();
        }

        student.setRegisterNumber(dto.getRegisterNumber());
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setDepartment(dto.getDepartment());
        student.setYear(dto.getYear());
        student.setSection(dto.getSection());
        student.setCgpa(dto.getCgpa());
        student.setLastUpdated(LocalDateTime.now());

        if (dto.getClassId() != null) {
            StudentClass sClass = classRepository.findById(dto.getClassId()).orElse(null);
            student.setStudentClass(sClass);
        }

        // LeetCode Username Handling: ONLY faculty entered!
        String lcUsername = dto.getLeetcodeUsername();
        if (lcUsername != null && !lcUsername.trim().isEmpty()) {
            student.setLeetcodeUsername(lcUsername.trim());
            // If profile username is updated or newly added, connect profile
            try {
                leetCodeService.connectProfile(student, lcUsername.trim());
            } catch (Exception e) {
                // If invalid username or fetch fails during initial add, set status to ERROR or NOT_CONNECTED with warning
                student.setStatus(Student.ProfileStatus.ERROR);
            }
        } else {
            student.setLeetcodeUsername(null);
            student.setStatus(Student.ProfileStatus.NOT_CONNECTED);
        }

        // Save student
        Student savedStudent = studentRepository.save(student);

        // Recalculate rankings dynamically across the system (Req 29)
        rankingService.recalculateAllRankings();

        return studentRepository.findById(savedStudent.getId()).orElse(savedStudent);
    }

    @Transactional
    public Student connectLeetCodeProfile(Long studentId, String manuallyEnteredUsername) throws Exception {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        leetCodeService.connectProfile(student, manuallyEnteredUsername);
        studentRepository.save(student);

        // Recalculate rankings after profile update
        rankingService.recalculateAllRankings();

        return studentRepository.findById(studentId).orElse(student);
    }

    @Transactional
    public boolean refreshLeetCodeData(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        boolean success = leetCodeService.refreshStudentData(student);
        
        // Recalculate rankings (even if refresh failed, score is calculated from DB values)
        rankingService.recalculateAllRankings();

        return success;
    }

    @Transactional
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
        // Recalculate rankings after student deletion (Req 29)
        rankingService.recalculateAllRankings();
    }

    public List<Student> searchStudents(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllStudents();
        }
        return studentRepository.searchStudents(query.trim());
    }

    public List<Student> filterStudents(Long classId, String dept, Integer year, String sec, String statusStr) {
        Student.ProfileStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = Student.ProfileStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (Exception ignored) {}
        }
        return studentRepository.filterStudents(classId, dept, year, sec, status);
    }
}

package com.nlp.platform.controller;

import com.nlp.platform.dto.StudentDTO;
import com.nlp.platform.model.Student;
import com.nlp.platform.model.StudentClass;
import com.nlp.platform.service.RankingService;
import com.nlp.platform.service.StudentClassService;
import com.nlp.platform.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final StudentClassService classService;
    private final RankingService rankingService;

    public StudentController(StudentService studentService,
                             StudentClassService classService,
                             RankingService rankingService) {
        this.studentService = studentService;
        this.classService = classService;
        this.rankingService = rankingService;
    }

    @GetMapping
    public String listStudents(@RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "classId", required = false) Long classId,
                               @RequestParam(value = "department", required = false) String department,
                               @RequestParam(value = "year", required = false) Integer year,
                               @RequestParam(value = "section", required = false) String section,
                               @RequestParam(value = "status", required = false) String status,
                               Model model) {
        
        List<Student> students;
        if (search != null && !search.trim().isEmpty()) {
            students = studentService.searchStudents(search);
        } else if (classId != null || department != null || year != null || section != null || status != null) {
            students = studentService.filterStudents(classId, department, year, section, status);
        } else {
            students = studentService.getAllStudents();
        }

        List<StudentClass> classes = classService.getAllClasses();
        model.addAttribute("students", students);
        model.addAttribute("classes", classes);
        model.addAttribute("search", search);
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("selectedDept", department);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedSec", section);
        model.addAttribute("selectedStatus", status);

        return "students";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        StudentDTO studentDTO = new StudentDTO();
        List<StudentClass> classes = classService.getAllClasses();
        model.addAttribute("studentDTO", studentDTO);
        model.addAttribute("classes", classes);
        return "add-student";
    }

    @PostMapping("/save")
    public String saveStudent(@Valid @ModelAttribute("studentDTO") StudentDTO studentDTO,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("classes", classService.getAllClasses());
            return studentDTO.getId() == null ? "add-student" : "edit-student";
        }

        try {
            Student savedStudent = studentService.saveStudent(studentDTO);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Student '" + savedStudent.getName() + "' saved successfully!");
            return "redirect:/students/" + savedStudent.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("classes", classService.getAllClasses());
            return studentDTO.getId() == null ? "add-student" : "edit-student";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while saving: " + e.getMessage());
            model.addAttribute("classes", classService.getAllClasses());
            return studentDTO.getId() == null ? "add-student" : "edit-student";
        }
    }

    @GetMapping("/{id}")
    public String viewStudentDetails(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Student student = studentService.getStudentById(id).orElse(null);
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Student not found.");
            return "redirect:/students";
        }

        model.addAttribute("student", student);
        return "student-details";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Student student = studentService.getStudentById(id).orElse(null);
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Student not found.");
            return "redirect:/students";
        }

        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setRegisterNumber(student.getRegisterNumber());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setDepartment(student.getDepartment());
        dto.setYear(student.getYear());
        dto.setSection(student.getSection());
        dto.setCgpa(student.getCgpa());
        dto.setLeetcodeUsername(student.getLeetcodeUsername());
        if (student.getStudentClass() != null) {
            dto.setClassId(student.getStudentClass().getId());
        }

        model.addAttribute("studentDTO", dto);
        model.addAttribute("classes", classService.getAllClasses());
        return "edit-student";
    }

    @PostMapping("/{id}/connect")
    public String connectLeetCodeProfile(@PathVariable("id") Long id,
                                         @RequestParam("leetcodeUsername") String leetcodeUsername,
                                         RedirectAttributes redirectAttributes) {
        try {
            studentService.connectLeetCodeProfile(id, leetcodeUsername);
            redirectAttributes.addFlashAttribute("successMessage", 
                "LeetCode profile '" + leetcodeUsername.trim() + "' connected successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Unable to retrieve profile for username '" + leetcodeUsername.trim() + "'. Please verify the username.");
        }
        return "redirect:/students/" + id;
    }

    @PostMapping("/{id}/refresh")
    public String refreshLeetCodeData(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean success = studentService.refreshLeetCodeData(id);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "LeetCode data refreshed successfully!");
            } else {
                redirectAttributes.addFlashAttribute("warningMessage", 
                    "Refresh completed with warning: Last refresh failed — showing previous data.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error refreshing data: " + e.getMessage());
        }
        return "redirect:/students/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteStudent(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student record removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete student: " + e.getMessage());
        }
        return "redirect:/students";
    }

    @PostMapping("/recalculate-rankings")
    public String recalculateRankings(RedirectAttributes redirectAttributes) {
        try {
            rankingService.recalculateAllRankings();
            redirectAttributes.addFlashAttribute("successMessage", "Rankings recalculated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to recalculate rankings: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }
}

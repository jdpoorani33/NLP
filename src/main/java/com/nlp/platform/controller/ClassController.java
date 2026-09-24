package com.nlp.platform.controller;

import com.nlp.platform.dto.ClassDTO;
import com.nlp.platform.model.StudentClass;
import com.nlp.platform.service.StudentClassService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/classes")
public class ClassController {

    private final StudentClassService classService;

    public ClassController(StudentClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public String listClasses(Model model) {
        List<StudentClass> classes = classService.getAllClasses();
        model.addAttribute("classes", classes);
        model.addAttribute("classDTO", new ClassDTO());
        return "classes";
    }

    @PostMapping("/save")
    public String saveClass(@Valid @ModelAttribute("classDTO") ClassDTO classDTO,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("classes", classService.getAllClasses());
            return "classes";
        }

        try {
            StudentClass createdClass = classService.createClass(classDTO);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Class '" + createdClass.getClassName() + "' created successfully!");
            return "redirect:/classes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("classes", classService.getAllClasses());
            return "classes";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteClass(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            classService.deleteClass(id);
            redirectAttributes.addFlashAttribute("successMessage", "Class removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting class: " + e.getMessage());
        }
        return "redirect:/classes";
    }
}

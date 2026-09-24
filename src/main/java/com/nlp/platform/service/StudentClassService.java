package com.nlp.platform.service;

import com.nlp.platform.dto.ClassDTO;
import com.nlp.platform.model.StudentClass;
import com.nlp.platform.repository.StudentClassRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentClassService {

    private final StudentClassRepository classRepository;

    public StudentClassService(StudentClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public List<StudentClass> getAllClasses() {
        return classRepository.findAll();
    }

    public Optional<StudentClass> getClassById(Long id) {
        return classRepository.findById(id);
    }

    public StudentClass createClass(ClassDTO classDTO) {
        if (classRepository.existsByClassName(classDTO.getClassName())) {
            throw new IllegalArgumentException("Class with name '" + classDTO.getClassName() + "' already exists.");
        }

        StudentClass studentClass = new StudentClass(
            classDTO.getClassName(),
            classDTO.getDepartment(),
            classDTO.getYear(),
            classDTO.getSection(),
            classDTO.getAcademicYear()
        );

        return classRepository.save(studentClass);
    }

    public void deleteClass(Long id) {
        classRepository.deleteById(id);
    }
}

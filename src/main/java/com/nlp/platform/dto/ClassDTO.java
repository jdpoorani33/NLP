package com.nlp.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ClassDTO {

    private Long id;

    @NotBlank(message = "Class name is required (e.g., B.Tech IT - III Year - A Section)")
    private String className;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotBlank(message = "Section is required")
    private String section;

    private String academicYear;

    public ClassDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
}

package com.nlp.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {

    public enum ProfileStatus {
        NOT_CONNECTED,
        CONNECTED,
        UPDATE_REQUIRED,
        ERROR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Register number is required")
    @Column(name = "register_number", nullable = false, unique = true)
    private String registerNumber;

    @NotBlank(message = "Student name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "Year is required")
    @Min(value = 1, message = "Year must be at least 1")
    @Max(value = 4, message = "Year cannot exceed 4")
    @Column(name = "study_year", nullable = false)
    private Integer year;

    @NotBlank(message = "Section is required")
    private String section;

    @NotNull(message = "CGPA is required")
    @DecimalMin(value = "0.0", message = "CGPA cannot be negative")
    @DecimalMax(value = "10.0", message = "CGPA cannot exceed 10.0")
    private Double cgpa;

    @Column(name = "leetcode_username")
    private String leetcodeUsername;

    @Column(name = "leetcode_profile_url")
    private String leetcodeProfileUrl;

    @Column(name = "academic_score")
    private Double academicScore = 0.0;

    @Column(name = "problem_solving_score")
    private Double problemSolvingScore = 0.0;

    @Column(name = "contest_score")
    private Double contestScore = 0.0;

    @Column(name = "overall_score")
    private Double overallScore = 0.0;

    @Column(name = "student_rank")
    private Integer rank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus status = ProfileStatus.NOT_CONNECTED;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id")
    private StudentClass studentClass;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "leetcode_data_id")
    private LeetCodeData leetcodeData;

    public Student() {
    }

    public Student(String registerNumber, String name, String email, String department, 
                   Integer year, String section, Double cgpa, String leetcodeUsername) {
        this.registerNumber = registerNumber;
        this.name = name;
        this.email = email;
        this.department = department;
        this.year = year;
        this.section = section;
        this.cgpa = cgpa;
        this.leetcodeUsername = leetcodeUsername;
        if (leetcodeUsername != null && !leetcodeUsername.trim().isEmpty()) {
            this.leetcodeProfileUrl = "https://leetcode.com/" + leetcodeUsername.trim() + "/";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public String getLeetcodeUsername() {
        return leetcodeUsername;
    }

    public void setLeetcodeUsername(String leetcodeUsername) {
        this.leetcodeUsername = leetcodeUsername != null ? leetcodeUsername.trim() : null;
        if (this.leetcodeUsername != null && !this.leetcodeUsername.isEmpty()) {
            this.leetcodeProfileUrl = "https://leetcode.com/" + this.leetcodeUsername + "/";
        } else {
            this.leetcodeProfileUrl = null;
        }
    }

    public String getLeetcodeProfileUrl() {
        return leetcodeProfileUrl;
    }

    public void setLeetcodeProfileUrl(String leetcodeProfileUrl) {
        this.leetcodeProfileUrl = leetcodeProfileUrl;
    }

    public Double getAcademicScore() {
        return academicScore != null ? academicScore : 0.0;
    }

    public void setAcademicScore(Double academicScore) {
        this.academicScore = academicScore;
    }

    public Double getProblemSolvingScore() {
        return problemSolvingScore != null ? problemSolvingScore : 0.0;
    }

    public void setProblemSolvingScore(Double problemSolvingScore) {
        this.problemSolvingScore = problemSolvingScore;
    }

    public Double getContestScore() {
        return contestScore != null ? contestScore : 0.0;
    }

    public void setContestScore(Double contestScore) {
        this.contestScore = contestScore;
    }

    public Double getOverallScore() {
        return overallScore != null ? overallScore : 0.0;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public ProfileStatus getStatus() {
        return status;
    }

    public void setStatus(ProfileStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public StudentClass getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(StudentClass studentClass) {
        this.studentClass = studentClass;
    }

    public LeetCodeData getLeetcodeData() {
        return leetcodeData;
    }

    public void setLeetcodeData(LeetCodeData leetcodeData) {
        this.leetcodeData = leetcodeData;
    }
}

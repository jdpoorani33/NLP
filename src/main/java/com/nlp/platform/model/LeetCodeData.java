package com.nlp.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leetcode_data")
public class LeetCodeData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_solved")
    private Integer totalSolved = 0;

    @Column(name = "easy_solved")
    private Integer easySolved = 0;

    @Column(name = "medium_solved")
    private Integer mediumSolved = 0;

    @Column(name = "hard_solved")
    private Integer hardSolved = 0;

    @Column(name = "rating")
    private Integer rating = 0;

    @Column(name = "contest_count")
    private Integer contestCount = 0;

    @Column(name = "contest_rating")
    private Integer contestRating = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "status_message", length = 500)
    private String statusMessage;

    public LeetCodeData() {
    }

    public LeetCodeData(Integer totalSolved, Integer easySolved, Integer mediumSolved, Integer hardSolved, 
                        Integer contestRating, Integer contestCount) {
        this.totalSolved = totalSolved != null ? totalSolved : 0;
        this.easySolved = easySolved != null ? easySolved : 0;
        this.mediumSolved = mediumSolved != null ? mediumSolved : 0;
        this.hardSolved = hardSolved != null ? hardSolved : 0;
        this.contestRating = contestRating != null ? contestRating : 0;
        this.rating = this.contestRating;
        this.contestCount = contestCount != null ? contestCount : 0;
        this.lastUpdated = LocalDateTime.now();
        this.statusMessage = "Profile data updated successfully";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTotalSolved() {
        return totalSolved != null ? totalSolved : 0;
    }

    public void setTotalSolved(Integer totalSolved) {
        this.totalSolved = totalSolved;
    }

    public Integer getEasySolved() {
        return easySolved != null ? easySolved : 0;
    }

    public void setEasySolved(Integer easySolved) {
        this.easySolved = easySolved;
    }

    public Integer getMediumSolved() {
        return mediumSolved != null ? mediumSolved : 0;
    }

    public void setMediumSolved(Integer mediumSolved) {
        this.mediumSolved = mediumSolved;
    }

    public Integer getHardSolved() {
        return hardSolved != null ? hardSolved : 0;
    }

    public void setHardSolved(Integer hardSolved) {
        this.hardSolved = hardSolved;
    }

    public Integer getRating() {
        return rating != null ? rating : 0;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getContestCount() {
        return contestCount != null ? contestCount : 0;
    }

    public void setContestCount(Integer contestCount) {
        this.contestCount = contestCount;
    }

    public Integer getContestRating() {
        return contestRating != null ? contestRating : 0;
    }

    public void setContestRating(Integer contestRating) {
        this.contestRating = contestRating;
        this.rating = contestRating;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}

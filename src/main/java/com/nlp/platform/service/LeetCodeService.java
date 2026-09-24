package com.nlp.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlp.platform.model.LeetCodeData;
import com.nlp.platform.model.Student;
import com.nlp.platform.repository.LeetCodeDataRepository;
import com.nlp.platform.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LeetCodeService {

    private static final Logger logger = LoggerFactory.getLogger(LeetCodeService.class);
    private static final String LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql";

    private final LeetCodeDataRepository leetCodeDataRepository;
    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LeetCodeService(LeetCodeDataRepository leetCodeDataRepository,
                           StudentRepository studentRepository) {
        this.leetCodeDataRepository = leetCodeDataRepository;
        this.studentRepository = studentRepository;
        
        org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(4000);
        this.restTemplate = new RestTemplate(requestFactory);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Manually connects a student to an explicitly provided LeetCode username.
     * System NEVER auto-searches or guesses usernames.
     */
    public LeetCodeData connectProfile(Student student, String manuallyEnteredUsername) throws Exception {
        if (manuallyEnteredUsername == null || manuallyEnteredUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("LeetCode username cannot be empty.");
        }

        String username = manuallyEnteredUsername.trim();
        logger.info("Faculty manually connecting LeetCode profile '{}' for student '{}'", username, student.getRegisterNumber());

        // Validate and retrieve profile stats using strictly provided username
        LeetCodeData fetchedData = fetchProfileFromLeetCode(username);

        // Update student record
        student.setLeetcodeUsername(username);
        student.setStatus(Student.ProfileStatus.CONNECTED);
        student.setLastUpdated(LocalDateTime.now());

        if (student.getLeetcodeData() != null) {
            LeetCodeData existing = student.getLeetcodeData();
            existing.setTotalSolved(fetchedData.getTotalSolved());
            existing.setEasySolved(fetchedData.getEasySolved());
            existing.setMediumSolved(fetchedData.getMediumSolved());
            existing.setHardSolved(fetchedData.getHardSolved());
            existing.setContestRating(fetchedData.getContestRating());
            existing.setContestCount(fetchedData.getContestCount());
            existing.setLastUpdated(LocalDateTime.now());
            existing.setStatusMessage("LeetCode profile connected successfully.");
            leetCodeDataRepository.save(existing);
            return existing;
        } else {
            fetchedData.setStatusMessage("LeetCode profile connected successfully.");
            fetchedData.setLastUpdated(LocalDateTime.now());
            student.setLeetcodeData(fetchedData);
            return fetchedData;
        }
    }

    /**
     * Refreshes LeetCode data for an already connected student.
     * CRITICAL RULE (Req 24): If refresh fails, PRESERVE previous valid data!
     */
    public boolean refreshStudentData(Student student) {
        if (student.getLeetcodeUsername() == null || student.getLeetcodeUsername().trim().isEmpty()) {
            student.setStatus(Student.ProfileStatus.NOT_CONNECTED);
            studentRepository.save(student);
            return false;
        }

        String username = student.getLeetcodeUsername().trim();
        logger.info("Refreshing LeetCode data for username '{}'", username);

        try {
            LeetCodeData newData = fetchProfileFromLeetCode(username);
            LeetCodeData existing = student.getLeetcodeData();

            if (existing == null) {
                existing = new LeetCodeData();
                student.setLeetcodeData(existing);
            }

            // Update with fresh data
            existing.setTotalSolved(newData.getTotalSolved());
            existing.setEasySolved(newData.getEasySolved());
            existing.setMediumSolved(newData.getMediumSolved());
            existing.setHardSolved(newData.getHardSolved());
            existing.setContestRating(newData.getContestRating());
            existing.setContestCount(newData.getContestCount());
            existing.setLastUpdated(LocalDateTime.now());
            existing.setStatusMessage("Latest data refreshed successfully.");

            student.setStatus(Student.ProfileStatus.CONNECTED);
            student.setLastUpdated(LocalDateTime.now());
            studentRepository.save(student);
            return true;

        } catch (Exception e) {
            logger.warn("Failed to refresh LeetCode data for '{}': {}. Preserving previous data.", username, e.getMessage());
            
            // REQUIREMENT 24: DO NOT lose or clear previous valid data!
            student.setStatus(Student.ProfileStatus.ERROR);
            if (student.getLeetcodeData() != null) {
                student.getLeetcodeData().setStatusMessage("Last refresh failed — showing previous data.");
            }
            studentRepository.save(student);
            return false;
        }
    }

    /**
     * Direct integration method with LeetCode API / GraphQL.
     * Retries via public GraphQL endpoint or REST fallback.
     */
    public LeetCodeData fetchProfileFromLeetCode(String username) throws Exception {
        // Attempt 1: Official GraphQL query
        try {
            return fetchFromGraphQL(username);
        } catch (Exception e1) {
            logger.warn("GraphQL query failed for username '{}': {}. Trying REST proxy fallback...", username, e1.getMessage());
            // Attempt 2: REST fallback
            try {
                return fetchFromRestFallback(username);
            } catch (Exception e2) {
                logger.error("REST fallback also failed for username '{}': {}", username, e2.getMessage());
                throw new IllegalArgumentException("Unable to retrieve profile for username '" + username + "'. Please verify the username exists on LeetCode.");
            }
        }
    }

    private LeetCodeData fetchFromGraphQL(String username) throws Exception {
        String graphqlQuery = "{"
            + "\"query\": \"query getUserProfile($username: String!) { matchedUser(username: $username) { username submitStats: submitStatsGlobal { acSubmissionNum { difficulty count } } } userContestRanking(username: $username) { rating attendedContestsCount } }\","
            + "\"variables\": {\"username\": \"" + username + "\"}"
            + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        headers.set("Referer", "https://leetcode.com/");

        HttpEntity<String> entity = new HttpEntity<>(graphqlQuery, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(LEETCODE_GRAPHQL_URL, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.path("data");

            if (dataNode.isMissingNode() || dataNode.path("matchedUser").isNull()) {
                throw new IllegalArgumentException("LeetCode profile '" + username + "' not found.");
            }

            JsonNode submitStats = dataNode.path("matchedUser").path("submitStats").path("acSubmissionNum");
            int total = 0, easy = 0, medium = 0, hard = 0;

            if (submitStats.isArray()) {
                for (JsonNode stat : submitStats) {
                    String diff = stat.path("difficulty").asText();
                    int count = stat.path("count").asInt(0);
                    if ("All".equalsIgnoreCase(diff)) total = count;
                    else if ("Easy".equalsIgnoreCase(diff)) easy = count;
                    else if ("Medium".equalsIgnoreCase(diff)) medium = count;
                    else if ("Hard".equalsIgnoreCase(diff)) hard = count;
                }
            }

            int contestRating = 0;
            int contestCount = 0;
            JsonNode contestNode = dataNode.path("userContestRanking");
            if (!contestNode.isNull() && !contestNode.isMissingNode()) {
                contestRating = (int) Math.round(contestNode.path("rating").asDouble(0.0));
                contestCount = contestNode.path("attendedContestsCount").asInt(0);
            }

            return new LeetCodeData(total, easy, medium, hard, contestRating, contestCount);
        }

        throw new IllegalStateException("LeetCode GraphQL request failed with status: " + response.getStatusCode());
    }

    private LeetCodeData fetchFromRestFallback(String username) throws Exception {
        String url = "https://leetcode-api.fazil.dev/" + username;
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("errors") || root.path("totalSolved").isMissingNode()) {
                throw new IllegalArgumentException("LeetCode profile '" + username + "' not found in REST fallback.");
            }

            int total = root.path("totalSolved").asInt(0);
            int easy = root.path("easySolved").asInt(0);
            int medium = root.path("mediumSolved").asInt(0);
            int hard = root.path("hardSolved").asInt(0);
            int contestRating = (int) Math.round(root.path("ranking").asDouble(0.0));
            int contestCount = root.path("contributionPoints").asInt(0);

            return new LeetCodeData(total, easy, medium, hard, contestRating, contestCount);
        }

        throw new IllegalStateException("REST fallback failed.");
    }
}

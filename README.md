# NLP — Student Competitive Programming & Academic Ranking Platform

A complete, professional full-stack web application built in **Java** with **Spring Boot**, **Spring Data JPA**, **Hibernate**, **Thymeleaf**, and **MySQL** (with H2 persistent fallback). Designed for college faculty to manage students, track academic performance (CGPA), manually map LeetCode profiles, retrieve performance metrics, and automatically rank students based on transparent scoring logic.

---

## 🌟 Key Features

1. **Faculty-Controlled Manual LeetCode Mapping**
   - Faculty explicitly enters student LeetCode usernames.
   - **Strict Policy:** The system **never** automatically searches, guesses, or assigns LeetCode profiles.
   - Profile validation ensures only valid usernames are connected.
   
2. **Dynamic Data Retrieval & Fail-Safe Persistence (Requirement 24)**
   - Retrieves total solved, easy/medium/hard breakdown, contest rating, and contest participation.
   - **Data Preservation Rule:** If a refresh attempt fails (network/API failure), the application preserves previous valid database records and flags status as `ERROR` ("Last refresh failed — showing previous data").

3. **Transparent Performance Scoring Formula**
   - **Academic Score (40%):** $(\text{CGPA} / 10.0) \times 100$
   - **Problem Solving Score (40%):** $\text{Raw Points} = (\text{Easy} \times 1 + \text{Medium} \times 2 + \text{Hard} \times 3)$ normalized against benchmark target.
   - **Contest Score (20%):** Normalized contest rating and participation bonus.
   - **Overall Score:** Weighted combination rounded to 2 decimal places.

4. **Automatic Deterministic Ranking & Tie Handling**
   - Ranks update automatically whenever a student is added, CGPA is modified, LeetCode data refreshes, or a record is deleted.
   - **Tie-Breaker Rules:** Overall Score $\rightarrow$ Problem Solving Score $\rightarrow$ Contest Rating $\rightarrow$ CGPA $\rightarrow$ Student Name (Alphabetical).

5. **Faculty Dashboard & Management UI**
   - Summary cards: Total Students, Connected Profiles, Average CGPA, Average Solved, Top Score, Last Refresh.
   - Top 3 Performers highlight cards.
   - Search, Filter, Sort, Edit, Delete, Refresh, and Class Management.
   - RESTful API endpoints for external integrations.

---

## 🏗️ Technology Stack & Architecture

- **Backend:** Java 21 / 17, Spring Boot 3.2, Spring MVC, Spring Data JPA, Hibernate.
- **Database:** MySQL (Production) / H2 File Database (Local Persistent Fallback).
- **Frontend:** HTML5, CSS3, JavaScript, Thymeleaf, Bootstrap 5, FontAwesome.
- **Build Tool:** Apache Maven.

### System Architecture Flow
```
Faculty User
    ↓
Web UI (Thymeleaf / HTML / CSS / JS)
    ↓
Controllers (DashboardController, StudentController, ClassController, StudentApiController)
    ↓
Services (StudentService, LeetCodeService, RankingService, StudentClassService)
    ↓
Repositories (StudentRepository, LeetCodeDataRepository, StudentClassRepository)
    ↓
Relational Database (MySQL / H2 File Storage)
```

---

## 📂 Project Structure

```
NLP/
├── pom.xml
├── README.md
├── src/
│   └── main/
│       ├── java/
│       │   └── com/nlp/platform/
│       │       ├── NlpApplication.java
│       │       ├── config/
│       │       │   ├── DataInitializer.java
│       │       │   └── WebConfig.java
│       │       ├── controller/
│       │       │   ├── ClassController.java
│       │       │   ├── DashboardController.java
│       │       │   ├── StudentApiController.java
│       │       │   └── StudentController.java
│       │       ├── dto/
│       │       │   ├── ClassDTO.java
│       │       │   └── StudentDTO.java
│       │       ├── model/
│       │       │   ├── LeetCodeData.java
│       │       │   ├── Student.java
│       │       │   └── StudentClass.java
│       │       ├── repository/
│       │       │   ├── LeetCodeDataRepository.java
│       │       │   ├── StudentClassRepository.java
│       │       │   └── StudentRepository.java
│       │       └── service/
│       │           ├── LeetCodeService.java
│       │           ├── RankingService.java
│       │           ├── StudentClassService.java
│       │           └── StudentService.java
│       └── resources/
│           ├── application.properties
│           ├── static/
│           │   ├── css/
│           │   │   └── style.css
│           │   └── js/
│           │       └── script.js
│           └── templates/
│               ├── add-student.html
│               ├── classes.html
│               ├── dashboard.html
│               ├── edit-student.html
│               ├── student-details.html
│               ├── students.html
│               └── fragments/
│                   ├── footer.html
│                   └── navbar.html
```

---

## 🗄️ Database Setup

### 1. MySQL Configuration (Recommended for Production)

Create a MySQL database:
```sql
CREATE DATABASE nlp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Update `src/main/resources/application.properties`:
```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/nlp_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

### 2. Default Out-of-the-Box Persistence (H2 File Database)
By default, `application.properties` uses H2 file persistence stored at `./data/nlp_db`, ensuring all database records persist across application restarts even without setting up a local MySQL instance.

---

## 🚀 How to Run the Application

1. Open terminal in project root directory:
   ```bash
   cd c:\Users\JD Poorani\Downloads\NLP
   ```

2. Build and run using Maven:
   ```bash
   mvn spring-boot:run
   ```

3. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

4. H2 Console (Optional Debugging):
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:file:./data/nlp_db`
   - Username: `sa`
   - Password: `password`

---

## 🌐 API Reference Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/students` | List all students ranked by overall score |
| `GET` | `/api/students/{id}` | Get student details by ID |
| `POST` | `/api/students` | Create new student record |
| `PUT` | `/api/students/{id}` | Update student record |
| `DELETE` | `/api/students/{id}` | Delete student record |
| `POST` | `/api/students/{id}/leetcode/connect` | Manually connect LeetCode profile |
| `POST` | `/api/students/{id}/leetcode/refresh` | Refresh student LeetCode metrics |
| `GET` | `/api/classes/{classId}/ranking` | Get rankings for specific class |

---

## 🛡️ Manual LeetCode Profile Mapping Rules

1. The system **never** attempts to guess or infer profiles based on student names or emails.
2. The faculty member must explicitly enter the username (e.g. `rahulkumar123`).
3. If valid, the profile is connected (`CONNECTED`).
4. If invalid or not found on LeetCode, an error is reported without suggesting alternative profiles.
5. Previous valid data is preserved if a refresh fails.

---

## 📜 License & Project Credits
Created for College Faculty Academic & Competitive Programming Evaluation. Built with Java and Spring Boot.

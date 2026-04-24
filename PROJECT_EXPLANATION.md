# Comprehensive ERP Project Explanation

This document serves as your **Master Blueprint** for the ERP Java Mini Project. It is designed to explain **what every significant file does** and **how data flows from start to finish**. Read this carefully to prepare for your project review.

---

## 1. The Core Architecture & Data Pipeline

The application follows a strict **Layered MVC (Model-View-Controller)** pattern. Here is the exact lifecycle of a request (The Pipeline):

### The Pipeline: Step-by-Step (Example: A Student Checking Their Grades)

1. **The Request (Browser):** The student clicks "My Grades". The browser sends a `GET` request to `http://localhost:8080/users/student/grades/`.
2. **Security Check (`SecurityConfiguration.java`):** Before anything happens, Spring Security intercepts the request. It checks the session cookie. "Is this user logged in? Are they a STUDENT?" If yes, it allows the request through.
3. **The Controller (`UsersController.java`):** The request reaches the `@GetMapping("/student/grades/")` method. The controller acts as the traffic cop. It doesn't calculate the grades; it simply asks the Service layer for them.
4. **The Service (`DashboardService.java` & `GpaService.java`):** The controller calls `dashboardService.getStudentDashboard(...)`. 
   - The Service holds the **Business Logic**. It needs to find out who the student is, fetch their grades, and calculate their GPA.
5. **The Repository (`GradeRepository.java` & `StudentRepository.java`):** The Service asks the Repositories for data. The Repositories use Spring Data JPA to automatically translate Java method calls (`findByStudentId(...)`) into SQL queries.
6. **The Database (`db.sqlite3`):** The SQL executes against the SQLite database, fetching rows from the `grade` and `student` tables. 
7. **The Entities (`Grade.java`, `Course.java`):** Hibernate (the ORM) maps those SQL rows back into Java Objects (Entities) and returns them up the chain: DB -> Repository -> Service.
8. **The DTO/Model Packaging:** The Service packages this data into a specific format (e.g., `StudentDashboardContext`) and returns it to the Controller. The Controller binds this data into a Spring `Model`.
9. **The View (`student_grades.html`):** The Controller returns the string `"students/student_grades"`. Spring passes the `Model` data to Thymeleaf. Thymeleaf reads the HTML file, loops through the grades using `th:each`, and generates a final HTML page.
10. **The Response:** The fully rendered HTML is sent back to the student's browser.

---

## 2. Exhaustive File Directory Breakdown

Here is a breakdown of what every file and folder does in your codebase.

### A. Project Root Files
* **`pom.xml`**: The Maven build file. It declares all dependencies (Spring Boot, Thymeleaf, SQLite driver, Hibernate, Lombok) and build plugins.
* **`db.sqlite3`**: The entire relational database stored in a single file. (Portable and easy to use).
* **`app-run.log`**: Console output log file (generated when running in the background).
* **`.gitignore`**: Tells Git which files to ignore (like `target/` or IDE files).

### B. Java Source Files (`src/main/java/com/example/stmgt/`)

#### 1. Configuration & Application Entry
* **`StmgtApplication.java`**: The main method. Bootstraps and starts the embedded Tomcat server.
* **`config/SecurityConfiguration.java`**: Configures Spring Security. Sets up CSRF protection, URL access rules (who can access `/admin/**` vs `/student/**`), and defines the login/logout behavior.

#### 2. The Controllers (`controller/`) - *The Traffic Cops*
* **`GlobalExceptionHandler.java`**: A global safety net. Catches Java Exceptions (like `NotFoundException`) and displays a friendly error page instead of crashing the app.
* **`SessionAuthHelper.java`**: Utility to manually establish or check HTTP Sessions and Roles.
* **`DjangoPasswordHasher.java`**: Bridge utility to verify passwords utilizing the old Django PBKDF2 hashing format (used if migrated from Python).
* **Domain Controllers** (`CoursesController`, `FacultyController`, `StudentsController`, `TasksController`, `UsersController`): These classes map URLs (like `/faculty/manage/`) to specific Java methods. They receive input, pass it to Services, and return HTML template names.

#### 3. The Entities (`domain/entity/`) - *The Database Tables*
*These classes map 1-to-1 with database tables using `@Entity` and `@Table` mappings.*
* **`CustomUser.java`**: The core authentication table (username, password, role).
* **`Student.java` & `Faculty.java`**: Profile tables linked to `CustomUser` via a `@OneToOne` mapping.
* **`Course.java`**: The academic courses available.
* **`Grade.java`**: Links a Student to a Course with a specific grade score (A, B, C).
* **`Task.java` & `TaskProgress.java`**: Represents homework/assignments created by Faculty and the submission progress tracked by Students.
* *(Legacy Auth tables like `AuthGroup`, `AuthPermission`, `DjangoContentType` are included for database schema backwards-compatibility).*

#### 4. Converters & Enums (`domain/converter/`, `domain/enums/`)
* **`enums/UserRole.java`, `AcademicLevel.java`, `TaskStatus.java`**: Fixed constants. Ensures a user is exactly "ADMIN", "FACULTY", or "STUDENT", preventing typos.
* **`converter/*`**: Automatically converts Java Enums into Strings when saving to the database, and back into Enums when reading.

#### 5. Data Transfer Objects (`dto/`) - *The Middlemen*
*DTOs exist for Security. Instead of letting a user edit an `Entity` directly via a web form, they submit a DTO. The controller validates the DTO before making DB changes.*
* **`*CreateRequestDto` / `*UpdateRequestDto`**: Form backing objects (e.g., `StudentCreateRequestDto` captures the "Create Student" form).
* **`validation/`**: Contains custom validation annotations. 
  * `PasswordMatches.java`: Ensures "Confirm Password" matches "Password".
  * `UniqueCourseCodeOnCreate.java`: Reaches into the DB to ensure a teacher isn't creating a course code that already exists.
* **`service/`**: Contains highly complex isolated logic specifically for doing "Grade Upserts" (inserting finding out if a grade exists and updating it, or creating a new one).

#### 6. The Repositories (`repository/`) - *The Database Queries*
*These are interfaces extending `JpaRepository`. You don't write SQL here; Spring generates it based on method names.*
* **`StudentRepository`, `FacultyRepository`, `CourseRepository`, `GradeRepository`, `TaskRepository`, `TaskProgressRepository`, `CustomUserRepository`**.
* Example: `CustomUserRepository.findByUsername(String username)` automatically executes `SELECT * FROM custom_user WHERE username = ?`.

#### 7. Security (`security/`) - *Auth Implementations*
* **`CustomUserDetailsService.java`**: Required by Spring Security. Pulls the `CustomUser` from the database during the login process to verify the password.
* **`StmgtUserPrincipal.java`**: An implementation of Spring's `UserDetails` interface. Acts as a wrapper around our `CustomUser` to hold session data.
* **`RoleBasedAuthenticationSuccessHandler.java`**: A custom redirector. If you log in successfully, it dictates: "Admins go to `/admin/dashboard/`, Students go to `/student/dashboard/`."

#### 8. The Services (`service/`) - *The Business Brains*
* **`CourseLifecycleService`**: Manages creating, updating, or archiving courses safely.
* **`UserLifecycleService`**: Handles the exact steps to register a new user, hash their password, and save them.
* **`StudentProfileService` & `FacultyLifecycleService`**: Manages the profiles attached to the core users.
* **`GradeManagementService` & `GpaService`**: Applies grading logic and calculates mathematical GPA totals for the dashboards.
* **`TaskCommandOrchestrationService`**: Orchestrates multi-step processes for creating tasks and assigning them to specific students.

#### 9. Events (`event/`) - *Database Triggers*
* **`FacultyDomainEventPublisher`, `FacultyProfileDeletedListener`**: If an Admin deletes a Faculty member, this system "listens" for that deletion event and automatically cleans up or reassigns courses/tasks that the Faculty member owned.

### C. Resource Files (`src/main/resources/`)

#### 1. Configuration & Migrations
* **`application.yml`**: Server variables. Port 8080 setup, SQLite connection string, Hibernate dialect, and Thymeleaf cache dropping.
* **`db/migration/V3__enforce_django_on_delete_parity.sql`**: A raw SQL file defining cascading deletes for the SQLite database.

#### 2. Static Assets (`static/`) - *Sent directly to Browser*
* **`css/`**: Contains role-specific stylesheets to make dashboards look good (`admin_styles.css`, `student_styles.css`).
* **`js/`**: Client-side JavaScript. Handles UI logic like popping up a "Are you sure you want to delete this?" modal (`delete_manage.js`, `task_manage.js`).
* **`images/`**: Static visuals like backgrounds.

#### 3. Templates (`templates/`) - *The Web Pages*
*Thymeleaf HTML files. These are templates with `th:*` tags that act as variables.*
* **`users/login.html`**: The portal entry point.
* **`users/admin_dashboard.html`, `faculty_dashboard.html`, `student_dashboard.html`**: The command centers. Displays metrics (Total Users, GPAs, Pending Tasks) injected from the Service layer models.
* **`students/`, `faculty/`, `courses/`, `tasks/` folders**: Lists, management pages, and forms for CRUD (Create, Read, Update, Delete) operations across the app.

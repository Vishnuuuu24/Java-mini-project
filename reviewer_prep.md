# 🎓 Project Review Prep Guide
### ERP Student Management System — Java Spring Boot

---

## 1. 🎤 The 30-Second Elevator Pitch

> *"This is a full-stack, role-based ERP system for an educational institution built in Java using Spring Boot.
> It follows the MVC pattern — controllers handle requests, services hold the business logic, and
> JPA repositories talk to a SQLite database. Thymeleaf renders server-side HTML pages.
> It has three portals — Admin, Faculty, and Student — each with their own permissions enforced by
> Spring Security. The project was originally a Django (Python) application and was fully
> migrated to Java Spring Boot."*

---

## 2. 🛠️ Tech Stack — Know WHY you chose each one

| Technology | What it does | Why this choice |
|---|---|---|
| **Spring Boot 3.2.5** | Application framework | Convention over configuration — auto-configures everything (embedded Tomcat, JPA, Security) |
| **Thymeleaf** | HTML templating (View) | Native Spring Boot integration; renders HTML server-side before sending to browser; no separate frontend needed |
| **Spring Data JPA / Hibernate** | ORM (Object-Relational Mapping) | Avoids writing raw SQL; maps Java classes directly to DB tables |
| **SQLite** | Database | Zero-setup, file-based DB — perfect for a college project (no server to install) |
| **Spring Security** | Auth & Authorization | Battle-tested security library; handles sessions, CSRF, login/logout, and URL-level access control |
| **Lombok** | Code generation | Removes boilerplate: `@Getter`, `@Setter`, `@NoArgsConstructor` auto-generate getters/setters |
| **Maven** | Build tool | Manages all dependencies in `pom.xml`, compiles and packages the app |

---

## 3. 🏛️ Architecture — MVC Pattern

This is the most important thing to explain clearly.

```
  BROWSER
     │
     │ HTTP Request (e.g. GET /students/list)
     ▼
┌─────────────────────────────────┐
│   Spring Security               │  ← First checkpoint: Is user logged in? Right role?
└────────────────┬────────────────┘
                 │ (if authorized)
                 ▼
┌─────────────────────────────────┐
│   Controller  (C)               │  ← Receives request, calls service, returns view name
│   StudentsController.java       │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│   Service  (Business Logic)     │  ← Validates, calculates, orchestrates
│   StudentProfileService.java    │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│   Repository  (DB Access)       │  ← JPA generates SQL automatically
│   StudentRepository.java        │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│   SQLite Database               │  ← db.sqlite3 (the actual data)
│   Entities map to tables        │
└─────────────────────────────────┘
                 │
                 │  Data comes back up the chain
                 ▼
         Thymeleaf Template        ← HTML rendered with real data
         student_list.html
                 │
                 ▼
           BROWSER (rendered HTML)
```

**One-liner to remember:** *"The Controller is the traffic cop — it receives requests and delegates work. The Service is the brain — it holds business logic. The Repository is the librarian — it fetches data. The View is the display — it shows the result."*

---

## 4. 🗃️ Entity Relationships (ER) — Know This Cold

```
CustomUser ──1:1── Student
CustomUser ──1:1── Faculty

Faculty ──M:M── Course   (via junction table: faculty_faculty_courses)
Student ──M:M── Course   (via junction table: student_student_courses)

Student ──1:M── Grade    (one student has many grades)
Course  ──1:M── Grade    (one course has many grades)
          │
          └── Grade is the junction between Student and Course WITH a score

Faculty (as CustomUser) ──1:M── Task  (faculty creates many tasks)
Task ──M:M── CustomUser (Student)     (a task assigned to many students)

Student ──1:M── TaskProgress          (one student has many task-progress records)
Task    ──1:M── TaskProgress          (one task has many progress records)
```

### Key relationships to verbalize:
- **Faculty ↔ Student:** **Indirect Many-to-Many** via `Course`. They don't have a direct FK. A faculty's students are whoever enrolled in their courses.
- **Grade:** Acts as an **association table with an extra column** (`grade` score) between Student and Course.
- **TaskProgress:** Same concept — it's the bridge between `Task` and a `Student`, but carries state (submitted, pending, etc.).

---

## 5. 🔐 Security — How Authentication Works

1. User visits `http://localhost:8080/` → redirected to `/users/login/`
2. User submits username + password
3. `CustomUserDetailsService.java` loads the user from DB by username
4. Spring Security verifies the hashed password
5. On success → `RoleBasedAuthenticationSuccessHandler` redirects:
   - `ADMIN` → `/admin/dashboard/`
   - `FACULTY` → `/faculty/dashboard/`
   - `STUDENT` → `/student/dashboard/`
6. A **session cookie** is set in the browser
7. Every subsequent request checks this cookie

**URL-level protection** is declared in `SecurityConfiguration.java`:
```
/admin/**   → only ADMIN role
/faculty/** → only FACULTY role
/student/** → only STUDENT role
/users/**   → public (login page)
```

**CSRF protection** is enabled — every form submission includes a hidden token to prevent cross-site attacks.

> **If asked:** *"Passwords are stored as hashed values, not plain text. The project also includes a Django PBKDF2 password hasher as a bridge utility for migrated users."*

---

## 6. 🧩 Design Patterns Used

Be able to name these — reviewers love this.

| Pattern | Where | Explanation |
|---|---|---|
| **MVC (Model-View-Controller)** | Entire app | Entities = Model, Thymeleaf = View, Controllers = Controller |
| **Repository Pattern** | `repository/` package | Abstracts DB access; services never write SQL directly |
| **Service Layer Pattern** | `service/` package | Business logic is isolated from controllers and repositories |
| **DTO Pattern (Data Transfer Object)** | `dto/` package | Forms bind to DTOs, not directly to JPA entities — safer, prevents mass-assignment |
| **Observer / Event Pattern** | `event/` package | When a Faculty is deleted, an event fires and listeners clean up their courses/tasks automatically |
| **Converter Pattern** | `domain/converter/` | Automatically converts Enums ↔ String when reading/writing to DB |

---

## 7. 📦 Package Structure — Be Able to Walk Through It

```
com.example.stmgt/
├── StmgtApplication.java     ← main() — starts Tomcat
├── config/                   ← SecurityConfiguration
├── controller/               ← HTTP request handlers (the "C" in MVC)
├── domain/
│   ├── entity/               ← JPA Entities (the "M" — DB tables)
│   ├── enums/                ← UserRole, AcademicLevel, TaskStatus
│   └── converter/            ← Enum ↔ String DB converters
├── dto/                      ← Form objects + validation annotations
├── event/                    ← Domain events (Faculty delete cascade)
├── repository/               ← JPA interfaces (DB queries)
├── security/                 ← UserDetailsService, Principal, SuccessHandler
└── service/                  ← Business logic (the "brain")
```

---

## 8. 💡 Key Design Decisions — Know the Rationale

**Q: Why SQLite instead of MySQL/PostgreSQL?**
> SQLite is file-based (`db.sqlite3`) — zero configuration, portable, and perfect for a demo/college project. No separate DB server needed. In a production deployment, we'd swap to PostgreSQL by changing only `application.yml` and the JDBC driver.

**Q: Why Thymeleaf instead of React/Angular?**
> Spring Boot has native Thymeleaf integration. It renders HTML on the server before sending it to the browser — simpler architecture, no REST API + separate frontend needed. Great for monolithic college projects.

**Q: Why DTOs instead of using entities directly in forms?**
> Security — if forms bind directly to entities, a malicious user could inject fields that shouldn't be user-controlled (like `role` or `id`). DTOs expose only what the form should modify.

**Q: What is the event system for?**
> SQLite has limited support for cascading deletes. The `event/` package implements the **Observer pattern** — when a Faculty member is deleted, an application-level event fires, and a listener cleans up their orphaned courses and tasks.

**Q: Why `@ManyToMany` for Faculty-Course and Student-Course instead of a direct FK?**
> Real-world reality: one course can have multiple instructors (co-teaching), and one instructor can teach multiple courses. Same for students. A direct foreign key would only allow one-to-one.

---

## 9. 🔄 Data Flow Example — "Student Checks Their Grades"

Walk through this if asked to explain the system:

1. Student clicks "My Grades" → browser sends `GET /users/student/grades/`
2. **Security** checks session cookie: is user logged in as STUDENT? ✅
3. **`UsersController`** receives the request, calls `dashboardService.getStudentDashboard(...)`
4. **`DashboardService`** calls `GradeRepository.findByStudentId(...)` and `GpaService.calculate(...)`
5. **`GradeRepository`** generates SQL: `SELECT * FROM student_grade WHERE student_id = ?`
6. Hibernate maps the result rows → `Grade` Java objects
7. Service packages everything into a `StudentDashboardContext` model
8. Controller adds model to Spring's `Model` object, returns `"students/student_grades"`
9. **Thymeleaf** finds `templates/students/student_grades.html`, loops through grades with `th:each`
10. Rendered HTML sent to browser ✅

---

## 10. ❓ Likely Reviewer Questions — With Answers

| Question | Your Answer |
|---|---|
| What pattern does this project follow? | Layered MVC with a Service layer and Repository pattern |
| How is authentication handled? | Spring Security session-based auth; cookie stored in browser after login |
| How are passwords stored? | Hashed — never plain text; legacy support via Django PBKDF2 hasher |
| What is JPA/Hibernate? | ORM — maps Java classes to DB tables; auto-generates SQL from method names |
| What is Thymeleaf? | Server-side HTML templating engine; renders final HTML before sending to browser |
| What is the relationship between Faculty and Student? | Indirect Many-to-Many via Course; no direct FK between them |
| What is CSRF protection? | Prevents malicious sites from submitting forms on behalf of a logged-in user; Spring Security handles it automatically |
| Why does Grade exist as a separate entity? | It's a junction table between Student and Course, but with an extra column (the grade score) — can't be represented by a simple M:M join table |
| What does `ddl-auto: update` do? | Hibernate automatically creates/updates DB tables to match entity definitions on startup |
| What does Lombok do? | Auto-generates boilerplate code (getters, setters, constructors) at compile time via annotations |

---

## 11. ⚠️ Limitations to Be Upfront About (Shows Maturity)

- **No unit tests** — this is a college project; in production we'd add JUnit + Mockito
- **SQLite is not production-grade** for concurrent writes
- **No API** — purely server-rendered HTML; no REST endpoints for mobile or external integration
- **Java version mismatch** — `pom.xml` says Java 17 but README/dev environment uses Java 21
- **No CI/CD pipeline** — manual `mvn spring-boot:run` deployment only

---

## 12. 🗣️ Vocabulary to Use Confidently

- **ORM** — Object Relational Mapping (how Hibernate works)
- **JPA** — Java Persistence API (the standard Hibernate implements)
- **SSR** — Server-Side Rendering (what Thymeleaf does)
- **CSRF** — Cross-Site Request Forgery (what Spring Security protects against)
- **Session-based auth** — the login mechanism used (cookie stored in browser)
- **Entity** — a Java class that maps to a DB table
- **Repository** — interface that generates DB queries
- **DTO** — Data Transfer Object (form-backing bean)
- **Cascade delete** — when parent is deleted, children are auto-deleted
- **Upsert** — insert if not exists, update if it does (used in grade management)

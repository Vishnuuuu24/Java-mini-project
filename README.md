# ERP Student Management System (Java Full-Stack Mini Project)

This is a full-stack, role-based ERP (Enterprise Resource Planning) and Student Management System built for an educational institution. It provides specific dashboards for Admins, Faculty, and Students to manage profiles, courses, academic grades, and tasks.

## 🛠 Tech Stack
* **Backend:** Java 21, Spring Boot 3.2.x
* **Frontend:** Thymeleaf (Server-Side Rendering), HTML5, CSS3, Bootstrap 5
* **Architecture:** Layered MVC (Model-View-Controller)
* **Database:** Embedded SQLite (`db.sqlite3`)
* **ORM:** Spring Data JPA / Hibernate
* **Security:** Spring Security (Cookie-based Session Auth, CSRF Protection, Password Hashing)
* **Build Tool:** Maven

## ✨ Key Features & Roles
* **Admin:** Manage the entire system. Create users, assign roles, manage faculty/student profiles, and build the course catalog.
* **Faculty:** View assigned courses, grade students, and manage academic tasks for students.
* **Student:** View personal dashboards, track cumulative GPA, view specific course grades, and monitor assigned tasks.

---

## 🚀 Quick Run Guide (macOS)

This section gives copy-paste commands to run, stop, and recover the server quickly.

## Project Path

```bash
cd "/Users/vishnu/Documents/Documents - Mac/CSE(AI&ML)/Projects/University/Sem-6/Java Mini Project/Java Project CA-4"
```

## 1) First-Time Setup (only if needed)

```bash
brew install openjdk@21 maven
```

Verify:

```bash
java -version
mvn -version
```

## 2) Start Server (foreground)

Use this when you want to see live logs in the terminal.

```bash
cd "/Users/vishnu/Documents/Documents - Mac/CSE(AI&ML)/Projects/University/Sem-6/Java Mini Project/Java Project CA-4"
export JAVA_HOME="$('/usr/libexec/java_home' -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
mvn clean spring-boot:run
```

Open:

- Login page: http://localhost:8080/users/login/

## 3) Start Server (background)

Use this when you want terminal free while app keeps running.

```bash
cd "/Users/vishnu/Documents/Documents - Mac/CSE(AI&ML)/Projects/University/Sem-6/Java Mini Project/Java Project CA-4"
export JAVA_HOME="$('/usr/libexec/java_home' -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
nohup mvn clean spring-boot:run > app-run.log 2>&1 &
echo $! > app.pid
```

Watch logs:

```bash
tail -f app-run.log
```

## 4) Stop Server

### Safe stop (SIGTERM)

```bash
lsof -ti:8080 | xargs kill -15 2>/dev/null || true
```

### Force stop (SIGKILL)

```bash
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
```

### Stop using saved PID

```bash
cd "/Users/vishnu/Documents/Documents - Mac/CSE(AI&ML)/Projects/University/Sem-6/Java Mini Project/Java Project CA-4"
if [ -f app.pid ]; then kill -9 "$(cat app.pid)" 2>/dev/null || true; rm -f app.pid; fi
```

### Kill all likely Java/Spring app processes

```bash
pkill -f "spring-boot:run|StmgtApplication" 2>/dev/null || true
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
```

## 5) Hard Reset + Run (best recovery command)

Use this if you keep seeing repeated startup/template/port issues.

```bash
bash -lc '
set -euo pipefail
cd "/Users/vishnu/Documents/Documents - Mac/CSE(AI&ML)/Projects/University/Sem-6/Java Mini Project/Java Project CA-4"
export JAVA_HOME="$('/usr/libexec/java_home' -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
export MAVEN_OPTS="-Xms256m -Xmx1024m"
pkill -f "spring-boot:run|StmgtApplication" 2>/dev/null || true
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
rm -rf target
mvn clean spring-boot:run
'
```

## 6) Common Errors and Fixes

### Error: Port 8080 already in use

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
lsof -ti:8080 | xargs kill -9
```

### Error: Wrong Java version / build fails unexpectedly

```bash
export JAVA_HOME="$('/usr/libexec/java_home' -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
mvn -version
```

### Error: mvn command not found

```bash
brew install maven
mvn -version
```

### Error: Template not found or old template still served

```bash
cd "/Users/vishnu/Documents/Documents - Mac/CSE(AI&ML)/Projects/University/Sem-6/Java Mini Project/Java Project CA-4"
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
rm -rf target
export JAVA_HOME="$('/usr/libexec/java_home' -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean spring-boot:run
```

### Error: Process terminated with exit code 137

```bash
cd "/Users/vishnu/Documents/Documents - Mac/CSE(AI&ML)/Projects/University/Sem-6/Java Mini Project/Java Project CA-4"
export JAVA_HOME="$('/usr/libexec/java_home' -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
export MAVEN_OPTS="-Xms256m -Xmx1024m"
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
rm -rf target
mvn clean spring-boot:run
```

## 7) Quick Health Check

```bash
curl -i http://localhost:8080/users/login/
```

## 8) Test Login Credentials (seeded)

- admin / admin
- faculty / faculty
- student / student

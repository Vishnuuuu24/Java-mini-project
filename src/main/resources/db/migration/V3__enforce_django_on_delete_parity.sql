-- Flyway migration example: V3__enforce_django_on_delete_parity.sql
-- PostgreSQL syntax

-- Main FK parity
ALTER TABLE student_student DROP CONSTRAINT IF EXISTS fk_student_user;
ALTER TABLE student_student
    ADD CONSTRAINT fk_student_user
    FOREIGN KEY (user_id) REFERENCES users_customuser(id) ON DELETE CASCADE;

ALTER TABLE faculty_faculty DROP CONSTRAINT IF EXISTS fk_faculty_user;
ALTER TABLE faculty_faculty
    ADD CONSTRAINT fk_faculty_user
    FOREIGN KEY (user_id) REFERENCES users_customuser(id) ON DELETE CASCADE;

ALTER TABLE student_grade DROP CONSTRAINT IF EXISTS fk_grade_student;
ALTER TABLE student_grade
    ADD CONSTRAINT fk_grade_student
    FOREIGN KEY (student_id) REFERENCES student_student(id) ON DELETE CASCADE;

ALTER TABLE student_grade DROP CONSTRAINT IF EXISTS fk_grade_course;
ALTER TABLE student_grade
    ADD CONSTRAINT fk_grade_course
    FOREIGN KEY (course_id) REFERENCES courses_course(id) ON DELETE CASCADE;

ALTER TABLE task_taskprogress DROP CONSTRAINT IF EXISTS fk_taskprogress_task;
ALTER TABLE task_taskprogress
    ADD CONSTRAINT fk_taskprogress_task
    FOREIGN KEY (task_id) REFERENCES task_task(id) ON DELETE CASCADE;

ALTER TABLE task_taskprogress DROP CONSTRAINT IF EXISTS fk_taskprogress_student;
ALTER TABLE task_taskprogress
    ADD CONSTRAINT fk_taskprogress_student
    FOREIGN KEY (student_id) REFERENCES users_customuser(id) ON DELETE CASCADE;

ALTER TABLE task_task DROP CONSTRAINT IF EXISTS fk_task_created_by;
ALTER TABLE task_task
    ADD CONSTRAINT fk_task_created_by
    FOREIGN KEY (created_by_id) REFERENCES users_customuser(id) ON DELETE SET NULL;

ALTER TABLE auth_permission DROP CONSTRAINT IF EXISTS fk_permission_content_type;
ALTER TABLE auth_permission
    ADD CONSTRAINT fk_permission_content_type
    FOREIGN KEY (content_type_id) REFERENCES django_content_type(id) ON DELETE CASCADE;

-- M2M FK parity (Django default CASCADE behavior on through-table FKs)
ALTER TABLE student_student_courses DROP CONSTRAINT IF EXISTS fk_student_courses_student;
ALTER TABLE student_student_courses
    ADD CONSTRAINT fk_student_courses_student
    FOREIGN KEY (student_id) REFERENCES student_student(id) ON DELETE CASCADE;

ALTER TABLE student_student_courses DROP CONSTRAINT IF EXISTS fk_student_courses_course;
ALTER TABLE student_student_courses
    ADD CONSTRAINT fk_student_courses_course
    FOREIGN KEY (course_id) REFERENCES courses_course(id) ON DELETE CASCADE;

ALTER TABLE faculty_faculty_courses DROP CONSTRAINT IF EXISTS fk_faculty_courses_faculty;
ALTER TABLE faculty_faculty_courses
    ADD CONSTRAINT fk_faculty_courses_faculty
    FOREIGN KEY (faculty_id) REFERENCES faculty_faculty(id) ON DELETE CASCADE;

ALTER TABLE faculty_faculty_courses DROP CONSTRAINT IF EXISTS fk_faculty_courses_course;
ALTER TABLE faculty_faculty_courses
    ADD CONSTRAINT fk_faculty_courses_course
    FOREIGN KEY (course_id) REFERENCES courses_course(id) ON DELETE CASCADE;

ALTER TABLE courses_course_faculty DROP CONSTRAINT IF EXISTS fk_course_faculty_course;
ALTER TABLE courses_course_faculty
    ADD CONSTRAINT fk_course_faculty_course
    FOREIGN KEY (course_id) REFERENCES courses_course(id) ON DELETE CASCADE;

ALTER TABLE courses_course_faculty DROP CONSTRAINT IF EXISTS fk_course_faculty_user;
ALTER TABLE courses_course_faculty
    ADD CONSTRAINT fk_course_faculty_user
    FOREIGN KEY (customuser_id) REFERENCES users_customuser(id) ON DELETE CASCADE;

ALTER TABLE task_task_assigned_to DROP CONSTRAINT IF EXISTS fk_task_assigned_task;
ALTER TABLE task_task_assigned_to
    ADD CONSTRAINT fk_task_assigned_task
    FOREIGN KEY (task_id) REFERENCES task_task(id) ON DELETE CASCADE;

ALTER TABLE task_task_assigned_to DROP CONSTRAINT IF EXISTS fk_task_assigned_user;
ALTER TABLE task_task_assigned_to
    ADD CONSTRAINT fk_task_assigned_user
    FOREIGN KEY (customuser_id) REFERENCES users_customuser(id) ON DELETE CASCADE;

ALTER TABLE users_customuser_groups DROP CONSTRAINT IF EXISTS fk_customuser_groups_user;
ALTER TABLE users_customuser_groups
    ADD CONSTRAINT fk_customuser_groups_user
    FOREIGN KEY (customuser_id) REFERENCES users_customuser(id) ON DELETE CASCADE;

ALTER TABLE users_customuser_groups DROP CONSTRAINT IF EXISTS fk_customuser_groups_group;
ALTER TABLE users_customuser_groups
    ADD CONSTRAINT fk_customuser_groups_group
    FOREIGN KEY (group_id) REFERENCES auth_group(id) ON DELETE CASCADE;

ALTER TABLE users_customuser_user_permissions DROP CONSTRAINT IF EXISTS fk_customuser_permissions_user;
ALTER TABLE users_customuser_user_permissions
    ADD CONSTRAINT fk_customuser_permissions_user
    FOREIGN KEY (customuser_id) REFERENCES users_customuser(id) ON DELETE CASCADE;

ALTER TABLE users_customuser_user_permissions DROP CONSTRAINT IF EXISTS fk_customuser_permissions_permission;
ALTER TABLE users_customuser_user_permissions
    ADD CONSTRAINT fk_customuser_permissions_permission
    FOREIGN KEY (permission_id) REFERENCES auth_permission(id) ON DELETE CASCADE;

ALTER TABLE auth_group_permissions DROP CONSTRAINT IF EXISTS fk_group_permissions_group;
ALTER TABLE auth_group_permissions
    ADD CONSTRAINT fk_group_permissions_group
    FOREIGN KEY (group_id) REFERENCES auth_group(id) ON DELETE CASCADE;

ALTER TABLE auth_group_permissions DROP CONSTRAINT IF EXISTS fk_group_permissions_permission;
ALTER TABLE auth_group_permissions
    ADD CONSTRAINT fk_group_permissions_permission
    FOREIGN KEY (permission_id) REFERENCES auth_permission(id) ON DELETE CASCADE;

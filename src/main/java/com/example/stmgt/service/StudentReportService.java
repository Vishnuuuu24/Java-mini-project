package com.example.stmgt.service;

import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.repository.GradeRepository;
import com.example.stmgt.repository.StudentRepository;
import com.example.stmgt.service.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class StudentReportService {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;

    public StudentReportService(
        StudentRepository studentRepository,
        GradeRepository gradeRepository
    ) {
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
    }

    public String generateCsvReport(Long studentId) {
        StringBuilder csv = new StringBuilder();

        if (studentId != null) {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

            List<Grade> grades = gradeRepository.findByStudentId(studentId);
            csv.append("Student Name:,")
                .append(student.getUser().getFirstName())
                .append(" ")
                .append(student.getUser().getLastName())
                .append("\n");
            csv.append("Register No:,").append(student.getRegisterNo()).append("\n\n");
            csv.append("Course Code,Course Name,Grade\n\n");

            for (Grade grade : grades) {
                csv.append(grade.getCourse().getCode()).append(",")
                    .append(grade.getCourse().getName()).append(",")
                    .append(grade.getGrade() == null ? "" : grade.getGrade())
                    .append("\n");
            }

            csv.append("\nTotal GPA:,").append(student.getGpa());
            return csv.toString();
        }

        List<Grade> allGrades = gradeRepository.findAll();
        allGrades.sort(Comparator.comparing(grade -> grade.getStudent().getRegisterNo()));

        Student currentStudent = null;
        for (Grade grade : allGrades) {
            Student student = grade.getStudent();
            if (!Objects.equals(currentStudent, student)) {
                if (currentStudent != null) {
                    csv.append("Total GPA:,").append(currentStudent.getGpa()).append("\n\n");
                }
                csv.append("Register No:,").append(student.getRegisterNo()).append("\n");
                csv.append("Course Code,Course Name,Grade\n\n");
                currentStudent = student;
            }

            csv.append(grade.getCourse().getCode()).append(",")
                .append(grade.getCourse().getName()).append(",")
                .append(grade.getGrade() == null ? "" : grade.getGrade())
                .append("\n");
        }

        if (currentStudent != null) {
            csv.append("\nTotal GPA:,").append(currentStudent.getGpa()).append("\n");
        }

        return csv.toString();
    }

    public String generatePlainTextGradeReport(Long studentId) {
        StringBuilder report = new StringBuilder();
        report.append("Grades Report").append("\n\n");

        if (studentId != null) {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));
            report.append("Register No: ").append(student.getRegisterNo()).append("\n");
            report.append("Student: ")
                .append(student.getUser().getFirstName())
                .append(" ")
                .append(student.getUser().getLastName())
                .append("\n\n");

            for (Grade grade : gradeRepository.findByStudentId(studentId)) {
                report.append(grade.getCourse().getCode())
                    .append(" | ")
                    .append(grade.getCourse().getName())
                    .append(" | ")
                    .append(grade.getGrade() == null ? "-" : grade.getGrade())
                    .append("\n");
            }
            report.append("\nTotal GPA: ").append(student.getGpa());
            return report.toString();
        }

        List<Grade> allGrades = gradeRepository.findAll();
        allGrades.sort(Comparator.comparing(grade -> grade.getStudent().getRegisterNo()));

        Student currentStudent = null;
        for (Grade grade : allGrades) {
            Student student = grade.getStudent();
            if (!Objects.equals(currentStudent, student)) {
                if (currentStudent != null) {
                    report.append("Total GPA: ").append(currentStudent.getGpa()).append("\n\n");
                }
                report.append("Register No: ").append(student.getRegisterNo()).append("\n");
                currentStudent = student;
            }
            report.append(grade.getCourse().getCode())
                .append(" | ")
                .append(grade.getCourse().getName())
                .append(" | ")
                .append(grade.getGrade() == null ? "-" : grade.getGrade())
                .append("\n");
        }

        if (currentStudent != null) {
            report.append("Total GPA: ").append(currentStudent.getGpa()).append("\n");
        }

        return report.toString();
    }
}

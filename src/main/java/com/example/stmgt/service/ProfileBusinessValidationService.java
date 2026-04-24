package com.example.stmgt.service;

import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.domain.enums.AcademicLevel;
import com.example.stmgt.service.exception.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class ProfileBusinessValidationService {

    public void validateFacultyId(Integer facultyId) {
        if (facultyId != null && (facultyId < 100 || facultyId > 999)) {
            throw new ValidationException("Faculty id must be between 100 and 999");
        }
    }

    public void validateStudentAttendanceAndLevel(Student student) {
        if (student == null) {
            throw new ValidationException("Student is required");
        }

        validateStudentAttendance(student.getAttendance());
        validateStudentLevel(student.getLevel());
    }

    public void validateStudentAttendance(Double attendance) {
        if (attendance == null || attendance < 0.0d || attendance > 100.0d) {
            throw new ValidationException("Student attendance must be between 0 and 100");
        }
    }

    public void validateStudentLevel(AcademicLevel level) {
        if (level == null || (level != AcademicLevel.UG && level != AcademicLevel.PG)) {
            throw new ValidationException("Student level must be UG or PG");
        }
    }
}

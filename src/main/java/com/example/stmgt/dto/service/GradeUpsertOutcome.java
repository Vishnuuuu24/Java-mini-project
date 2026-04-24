package com.example.stmgt.dto.service;

import com.example.stmgt.domain.entity.Grade;

import java.util.Objects;

public class GradeUpsertOutcome {

    private final Grade grade;
    private final GradeUpsertAction action;

    private GradeUpsertOutcome(Grade grade, GradeUpsertAction action) {
        this.grade = Objects.requireNonNull(grade, "grade must not be null");
        this.action = Objects.requireNonNull(action, "action must not be null");
    }

    public static GradeUpsertOutcome created(Grade grade) {
        return new GradeUpsertOutcome(grade, GradeUpsertAction.CREATED);
    }

    public static GradeUpsertOutcome updated(Grade grade) {
        return new GradeUpsertOutcome(grade, GradeUpsertAction.UPDATED);
    }

    public Grade getGrade() {
        return grade;
    }

    public GradeUpsertAction getAction() {
        return action;
    }
}

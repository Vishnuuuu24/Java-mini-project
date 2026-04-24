package com.example.stmgt.dto.service;

import com.example.stmgt.dto.GradeUpsertRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GradeApplicationService {

    private final GradeUpsertPolicy gradeUpsertPolicy;

    public GradeApplicationService(GradeUpsertPolicy gradeUpsertPolicy) {
        this.gradeUpsertPolicy = gradeUpsertPolicy;
    }

    @Transactional
    public GradeUpsertOutcome upsertGrade(GradeUpsertRequestDto requestDto) {
        return gradeUpsertPolicy.upsert(requestDto);
    }
}

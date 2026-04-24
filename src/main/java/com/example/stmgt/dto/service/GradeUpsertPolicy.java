package com.example.stmgt.dto.service;

import com.example.stmgt.dto.GradeUpsertRequestDto;

public interface GradeUpsertPolicy {

    GradeUpsertOutcome upsert(GradeUpsertRequestDto request);
}

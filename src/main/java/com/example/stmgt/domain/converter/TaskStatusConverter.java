package com.example.stmgt.domain.converter;

import com.example.stmgt.domain.enums.TaskStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TaskStatusConverter implements AttributeConverter<TaskStatus, String> {

    @Override
    public String convertToDatabaseColumn(TaskStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TaskStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TaskStatus.fromValue(dbData);
    }
}

package com.example.stmgt.domain.converter;

import com.example.stmgt.domain.enums.TaskPriority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TaskPriorityConverter implements AttributeConverter<TaskPriority, String> {

    @Override
    public String convertToDatabaseColumn(TaskPriority attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TaskPriority convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TaskPriority.fromValue(dbData);
    }
}

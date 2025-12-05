package com.abc.question_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombinationKey {
    private Long fieldId;
    private Long topicId;
    private Long levelId;
    private Long questionTypeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CombinationKey that = (CombinationKey) o;
        return Objects.equals(fieldId, that.fieldId) &&
               Objects.equals(topicId, that.topicId) &&
               Objects.equals(levelId, that.levelId) &&
               Objects.equals(questionTypeId, that.questionTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldId, topicId, levelId, questionTypeId);
    }

    @Override
    public String toString() {
        return String.format("Field:%d-Topic:%d-Level:%d-Type:%d", 
            fieldId, topicId, levelId, questionTypeId);
    }
}

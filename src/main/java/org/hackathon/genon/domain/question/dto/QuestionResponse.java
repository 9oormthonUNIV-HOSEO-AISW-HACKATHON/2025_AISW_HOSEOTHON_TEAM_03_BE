package org.hackathon.genon.domain.question.dto;

import java.util.List;

public record QuestionResponse(
        Long id,
        String category,
        String content,
        String explanation,
        List<QuestionOption> options
) {}
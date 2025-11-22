package org.hackathon.genon.domain.question.dto;

import java.util.List;

public record QuestionResponseDto(
        Long id,
        String category,
        String content,
        String explanation,
        List<QuestionOptionDto> options
) {}
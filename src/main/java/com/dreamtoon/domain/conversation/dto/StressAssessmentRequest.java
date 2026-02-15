package com.dreamtoon.domain.conversation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StressAssessmentRequest {

    @Min(0)
    @Max(100)
    private Integer workStress;

    @Min(0)
    @Max(100)
    private Integer relationshipStress;

    @Min(0)
    @Max(100)
    private Integer healthStress;

    @Min(0)
    @Max(100)
    private Integer financialStress;

    @Min(0)
    @Max(100)
    private Integer sleepQuality;

    private List<String> stressFactors;
}

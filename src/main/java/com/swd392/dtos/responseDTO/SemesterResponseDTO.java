package com.swd392.dtos.responseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SemesterResponseDTO {

    private Integer semesterId;
    private String semesterCode;
    private String semesterName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createdAt;

    // Whether a feeding period exists for this semester
    private Boolean hasFeedingPeriod;
    private Integer feedingPeriodId;
}

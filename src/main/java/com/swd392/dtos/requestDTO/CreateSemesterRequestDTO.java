package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSemesterRequestDTO {

    @NotBlank(message = "Semester code is required")
    @Pattern(regexp = "^(SP|SU|FA)\\d{2}$",
             message = "Semester code must follow format: SP26, SU26, FA26")
    private String semesterCode;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}

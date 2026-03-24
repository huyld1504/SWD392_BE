package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeedingRequestDTO {

    @NotBlank(message = "Semester code is required")
    @Pattern(regexp = "^(SP|SU|FA)\\d{2}$",
             message = "Semester code must follow format: SP26, SU26, FA26")
    private String semesterCode;

    @Positive(message = "Grant amount must be positive")
    private BigDecimal grantAmount = new BigDecimal("100");
}

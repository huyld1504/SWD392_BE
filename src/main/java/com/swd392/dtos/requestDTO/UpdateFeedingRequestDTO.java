package com.swd392.dtos.requestDTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeedingRequestDTO {

  @NotNull(message = "Scheduled date is required")
  @Future(message = "Scheduled date must be in the future")
  private LocalDateTime scheduledAt;
}

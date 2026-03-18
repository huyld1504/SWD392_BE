package com.swd392.dtos.responseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedingPeriodResponseDTO {

  // ===== Common fields (luôn có) =====
  private Integer periodId;
  private String periodName;
  private BigDecimal grantAmount;
  private String status;
  private String triggerSource;
  private UserInfoDTO createdBy;
  private LocalDateTime scheduledAt;
  private LocalDateTime executedAt;
  private LocalDateTime createdAt;
  private Integer totalUsers;

  // ===== Trigger/Schedule result (chỉ có khi trigger hoặc schedule) =====
  private Integer totalUsersProcessed;
  private Integer totalUsersSkipped;

  // ===== Summary (chỉ có khi gọi GET /{periodId}) =====
  private BigDecimal totalCoinsGranted;
  private BigDecimal totalCoinsEarned;

  // ===== Detail (chỉ có khi gọi GET /{periodId}) =====
  private List<UserFeedingDetailDTO> users;
}

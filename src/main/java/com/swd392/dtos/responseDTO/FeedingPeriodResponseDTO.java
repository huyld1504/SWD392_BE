package com.swd392.dtos.responseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedingPeriodResponseDTO {

    // ===== Semester info =====
    private Integer periodId;
    private String semesterCode;
    private String semesterName;
    private LocalDate startDate;
    private LocalDate endDate;

    // ===== Feeding config =====
    private BigDecimal grantAmount;
    private String status;

    // ===== Audit =====
    private UserInfoDTO createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== Stats (for list & detail view) =====
    private Integer totalUsersFed;
    private BigDecimal totalCoinsFed;

    // ===== Dashboard stats (only for detail view) =====
    private FeedingStatsDTO stats;

    // ===== Users list (only for detail view) =====
    private List<UserFeedingDetailDTO> users;

    // ===== Trigger result (only after manual trigger) =====
    private Integer usersProcessedNow;
}

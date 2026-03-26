package com.swd392.dtos.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopStudentDTO {

    private Integer rank;
    private UserInfoDTO student;
    private BigDecimal totalDonationReceived;       // tổng coin được donate
    private Long donationCount;                     // số lần được donate
    private Long approvedArticleCount;              // số bài viết được duyệt trong kỳ
}

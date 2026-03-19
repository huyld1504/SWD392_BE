package com.swd392.services.interfaces;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.DonationRequestDTO;
import com.swd392.dtos.responseDTO.DonationResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface DonationService {

  /**
   * Donate BLUE coins to an article's author.
   */
  DonationResponseDTO donate(String senderEmail, DonationRequestDTO request);

  /**
   * Get all donations for a specific article (paginated, filterable by date
   * range).
   */
  PaginationResponseDTO<List<DonationResponseDTO>> getDonationsByArticle(
      Integer articleId, LocalDateTime fromDate, LocalDateTime toDate, int page, int size);

  /**
   * Get all donations sent by the currently authenticated user (paginated,
   * filterable by date range).
   */
  PaginationResponseDTO<List<DonationResponseDTO>> getMyDonations(
      String email, LocalDateTime fromDate, LocalDateTime toDate, int page, int size);
}

package com.swd392.mapper;

import com.swd392.dtos.responseDTO.ArticleInfoDTO;
import com.swd392.dtos.responseDTO.DonationResponseDTO;
import com.swd392.dtos.responseDTO.UserInfoDTO;
import com.swd392.entities.Article;
import com.swd392.entities.Donation;
import com.swd392.entities.User;
import org.springframework.stereotype.Component;

@Component
public class DonationMapper {

  public DonationResponseDTO toDTO(Donation donation) {
    User sender = donation.getSender();
    Article article = donation.getArticle();
    User receiver = article.getAuthor();

    UserInfoDTO senderDTO = new UserInfoDTO(
        sender.getUserId(),
        sender.getFullName(),
        sender.getEmail(),
        sender.getAvatarUrl());

    UserInfoDTO receiverDTO = new UserInfoDTO(
        receiver.getUserId(),
        receiver.getFullName(),
        receiver.getEmail(),
        receiver.getAvatarUrl());

    ArticleInfoDTO articleDTO = new ArticleInfoDTO(
        article.getArticleId(),
        article.getTitle(),
        article.getStatus().name());

    return new DonationResponseDTO(
        donation.getDonationId(),
        senderDTO,
        receiverDTO,
        articleDTO,
        donation.getAmount(),
        donation.getCurrency().name(),
        donation.getMessage(),
        donation.getCreatedAt());
  }
}

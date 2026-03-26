package com.swd392.repositories;

import com.swd392.entities.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Integer>, JpaSpecificationExecutor<Donation> {

  /**
   * Top donation receivers - ALL TIME.
   */
  @Query("SELECT a.author.userId, a.author.fullName, a.author.email, a.author.avatarUrl, " +
      "SUM(d.amount), COUNT(d) " +
      "FROM Donation d JOIN d.article a " +
      "GROUP BY a.author.userId, a.author.fullName, a.author.email, a.author.avatarUrl " +
      "ORDER BY SUM(d.amount) DESC")
  List<Object[]> findTopDonationReceivers();

  /**
   * Top donation receivers - filtered by date range (year/month or period).
   */
  @Query("SELECT a.author.userId, a.author.fullName, a.author.email, a.author.avatarUrl, " +
      "SUM(d.amount), COUNT(d) " +
      "FROM Donation d JOIN d.article a " +
      "WHERE d.createdAt >= :fromDate AND d.createdAt <= :toDate " +
      "GROUP BY a.author.userId, a.author.fullName, a.author.email, a.author.avatarUrl " +
      "ORDER BY SUM(d.amount) DESC")
  List<Object[]> findTopDonationReceiversByDateRange(
      @Param("fromDate") LocalDateTime fromDate,
      @Param("toDate") LocalDateTime toDate);

  /**
   * Top donation receivers by semester date range with limit (for leaderboard).
   */
  @Query("SELECT a.author.userId, a.author.fullName, a.author.email, a.author.avatarUrl, " +
      "SUM(d.amount), COUNT(d) " +
      "FROM Donation d JOIN d.article a " +
      "WHERE d.createdAt >= :fromDate AND d.createdAt <= :toDate " +
      "GROUP BY a.author.userId, a.author.fullName, a.author.email, a.author.avatarUrl " +
      "ORDER BY SUM(d.amount) DESC")
  List<Object[]> findTopDonationReceiversByDateRange(
      @Param("fromDate") LocalDateTime fromDate,
      @Param("toDate") LocalDateTime toDate,
      org.springframework.data.domain.Pageable pageable);
}

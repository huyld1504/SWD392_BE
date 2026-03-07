package com.swd392.scheduler;

import com.swd392.entities.FeedingPeriod;
import com.swd392.repositories.FeedingPeriodRepository;
import com.swd392.services.interfaces.FeedingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedingScheduler {

  private final FeedingService feedingService;
  private final FeedingPeriodRepository feedingPeriodRepository;

  /**
   * Scheduler 1: Daily Check (mỗi 24h, chạy lúc 02:00 sáng)
   * Quét các PENDING feeding period có scheduledAt <= hôm nay → chạy.
   */
  @Scheduled(cron = "0 0 2 * * *") // 02:00 mỗi ngày
  public void dailyCheckPendingFeedings() {
    log.info("\n  ═══════════════════════════════════════════");
    log.info("  ║ SCHEDULER ─ Daily Check Pending Feedings ║");
    log.info("  ═══════════════════════════════════════════");

    List<FeedingPeriod> pendingPeriods = feedingPeriodRepository
        .findByStatusAndScheduledAtLessThanEqual(
            FeedingPeriod.PeriodStatus.PENDING,
            LocalDateTime.now());

    if (pendingPeriods.isEmpty()) {
      log.info("  No pending feeding periods to execute today.");
      return;
    }

    for (FeedingPeriod period : pendingPeriods) {
      try {
        log.info("  Executing pending period: id={}, name={}, scheduledAt={}",
            period.getPeriodId(), period.getPeriodName(), period.getScheduledAt());

        feedingService.executePendingPeriod(period.getPeriodId());

        log.info("  ✓ Period {} completed successfully.", period.getPeriodId());
      } catch (Exception e) {
        log.error("  ✗ Period {} failed: {}", period.getPeriodId(), e.getMessage());
      }
    }
  }

  /**
   * Scheduler 2: Monthly Auto (ngày 1 mỗi tháng, lúc 03:00 sáng)
   * Tự động tạo + chạy feeding cho tháng đó nếu chưa có.
   */
  @Scheduled(cron = "0 0 3 1 * *") // 03:00 ngày 1 mỗi tháng
  public void monthlyAutoFeeding() {
    log.info("\n  ═══════════════════════════════════════════");
    log.info("  ║ SCHEDULER ─ Monthly Auto Feeding         ║");
    log.info("  ═══════════════════════════════════════════");

    try {
      feedingService.executeFeedingReset("AUTO_SCHEDULE", null);
      log.info("  ✓ Monthly auto feeding completed successfully.");
    } catch (Exception e) {
      log.warn("  ✗ Monthly auto feeding skipped/failed: {}", e.getMessage());
    }
  }
}

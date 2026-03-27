package com.swd392.scheduler;

import com.swd392.entities.FeedingPeriod;
import com.swd392.entities.Semester;
import com.swd392.repositories.FeedingPeriodRepository;
import com.swd392.repositories.SemesterRepository;
import com.swd392.services.interfaces.FeedingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedingScheduler {

    private final FeedingService feedingService;
    private final FeedingPeriodRepository feedingPeriodRepository;
    private final SemesterRepository semesterRepository;

    /**
     * JOB 1: Daily Feeding (02:00 AM every day)
     * Find all ACTIVE feeding periods whose semester covers today,
     * then feed any unfed users.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyFeeding() {
        log.info("\n  ═══════════════════════════════════════════");
        log.info("  ║ SCHEDULER ─ Daily Feeding                ║");
        log.info("  ═══════════════════════════════════════════");

        try {
            int totalFed = feedingService.executeDailyFeeding();
            log.info("  ✓ Daily feeding completed. Total users fed: {}", totalFed);
        } catch (Exception e) {
            log.error("  ✗ Daily feeding failed: {}", e.getMessage());
        }
    }

    /**
     * JOB 2: Auto-Complete Expired Periods (00:00 AM every day)
     * Find ACTIVE feeding periods whose semester endDate < today → set COMPLETED.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void autoCompletePeriods() {
        log.info("\n  ═══════════════════════════════════════════");
        log.info("  ║ SCHEDULER ─ Auto-Complete Expired Periods ║");
        log.info("  ═══════════════════════════════════════════");

        LocalDate today = LocalDate.now();
        List<FeedingPeriod> activePeriods = feedingPeriodRepository
            .findByStatus(FeedingPeriod.PeriodStatus.ACTIVE);

        int completed = 0;
        for (FeedingPeriod period : activePeriods) {
            Semester semester = period.getSemester();
            if (today.isAfter(semester.getEndDate())) {
                period.setStatus(FeedingPeriod.PeriodStatus.COMPLETED);
                semester.setStatus(Semester.SemesterStatus.COMPLETED);
                feedingPeriodRepository.save(period);
                semesterRepository.save(semester);
                completed++;

                log.info("  ✓ Auto-completed period {} ({}) – ended {}",
                    period.getPeriodId(), semester.getSemesterCode(), semester.getEndDate());
            }
        }

        if (completed == 0) {
            log.info("  No expired periods to complete.");
        } else {
            log.info("  Auto-completed {} period(s).", completed);
        }
    }

    /**
     * JOB 3: Auto-Activate Pending Periods (00:00 AM every day)
     * Find PENDING feeding periods whose semester startDate <= today → set ACTIVE.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void autoActivatePendingPeriods() {
        log.info("\n  ═══════════════════════════════════════════");
        log.info("  ║ SCHEDULER ─ Auto-Activate Pending Periods ║");
        log.info("  ═══════════════════════════════════════════");

        LocalDate today = LocalDate.now();
        List<FeedingPeriod> pendingPeriods = feedingPeriodRepository
            .findByStatus(FeedingPeriod.PeriodStatus.PENDING);

        int activated = 0;
        for (FeedingPeriod period : pendingPeriods) {
            Semester semester = period.getSemester();
            if (!today.isBefore(semester.getStartDate())) {
                period.setStatus(FeedingPeriod.PeriodStatus.ACTIVE);
                feedingPeriodRepository.save(period);
                activated++;

                log.info("  ✓ Auto-activated period {} ({}) – started {}",
                    period.getPeriodId(), semester.getSemesterCode(), semester.getStartDate());
            }
        }

        if (activated == 0) {
            log.info("  No pending periods to activate.");
        } else {
            log.info("  Auto-activated {} period(s).", activated);
        }
    }
}

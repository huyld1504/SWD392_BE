package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.requestDTO.CreateSemesterRequestDTO;
import com.swd392.dtos.responseDTO.SemesterResponseDTO;
import com.swd392.dtos.responseDTO.TopStudentDTO;
import com.swd392.dtos.responseDTO.UserInfoDTO;
import com.swd392.entities.FeedingPeriod;
import com.swd392.entities.Semester;
import com.swd392.exceptions.AppException;
import com.swd392.repositories.ArticleRepository;
import com.swd392.repositories.DonationRepository;
import com.swd392.repositories.FeedingPeriodRepository;
import com.swd392.repositories.SemesterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/semesters")
@RequiredArgsConstructor
@Tag(name = "Semester", description = "Semester management, statistics, and leaderboard APIs")
public class SemesterController {

    private final SemesterRepository semesterRepository;
    private final FeedingPeriodRepository feedingPeriodRepository;
    private final DonationRepository donationRepository;
    private final ArticleRepository articleRepository;

    // ==================== CREATE SEMESTER ====================

    @Operation(summary = "Create a semester (Admin)", description = "Create a new semester. Dates are auto-calculated from the code: "
            + "SP26 (Spring, Jan-Apr), SU26 (Summer, May-Aug), FA26 (Fall, Sep-Dec).", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Semester created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Semester already exists", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SemesterResponseDTO>> createSemester(
            @Valid @RequestBody CreateSemesterRequestDTO request) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ createSemester\n  │ Code : {}", request.getSemesterCode());

        String code = request.getSemesterCode().toUpperCase();

        // Check duplicate
        if (semesterRepository.existsBySemesterCode(code)) {
            throw new AppException("Semester already exists: " + code, HttpStatus.CONFLICT);
        }

        // Create with user-provided dates
        Semester semester = Semester.fromCodeAndDates(
                code, request.getStartDate(), request.getEndDate());
        semesterRepository.save(semester);

        log.info("\n  └─ CONTROLLER ─ createSemester\n    Created: {} ({} → {})",
                semester.getSemesterCode(), semester.getStartDate(), semester.getEndDate());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SemesterResponseDTO>builder()
                        .success(true)
                        .message("Semester " + semester.getSemesterName() + " created successfully")
                        .data(mapToDTO(semester))
                        .requestId(RequestContext.getRequestId())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    // ==================== GET ALL SEMESTERS ====================

    @Operation(summary = "Get all semesters (Admin)", description = "Retrieve all semesters ordered by start date descending.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SemesterResponseDTO>>> getAllSemesters() {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ getAllSemesters");

        List<SemesterResponseDTO> semesters = semesterRepository.findAllByOrderByStartDateDesc()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.<List<SemesterResponseDTO>>builder()
                        .success(true)
                        .message("Semesters retrieved successfully")
                        .data(semesters)
                        .requestId(RequestContext.getRequestId())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    // ==================== GET SEMESTER BY CODE ====================

    @Operation(summary = "Get semester detail (Admin)", description = "Get detailed info of a specific semester.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/{semesterCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SemesterResponseDTO>> getSemester(
            @Parameter(description = "Semester code", example = "SP26") @PathVariable String semesterCode) {

        RequestContext.setCurrentLayer("CONTROLLER");

        Semester semester = semesterRepository.findBySemesterCode(semesterCode.toUpperCase())
                .orElseThrow(() -> new AppException("Semester not found: " + semesterCode, HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(
                ApiResponse.<SemesterResponseDTO>builder()
                        .success(true)
                        .message("Semester retrieved successfully")
                        .data(mapToDTO(semester))
                        .requestId(RequestContext.getRequestId())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    // ==================== DELETE SEMESTER ====================

    @Operation(summary = "Delete a semester (Admin)", description = "Delete a semester. Only allowed if no feeding period has been created for it.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @DeleteMapping("/{semesterCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSemester(
            @Parameter(description = "Semester code", example = "SP26") @PathVariable String semesterCode) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ deleteSemester\n  │ Code : {}", semesterCode);

        Semester semester = semesterRepository.findBySemesterCode(semesterCode.toUpperCase())
                .orElseThrow(() -> new AppException("Semester not found: " + semesterCode, HttpStatus.NOT_FOUND));

        // Cannot delete if a feeding period exists
        if (feedingPeriodRepository.existsBySemesterSemesterId(semester.getSemesterId())) {
            throw new AppException(
                    "Cannot delete semester " + semesterCode + ": a feeding period exists for it. "
                            + "Cancel or complete the feeding period first.",
                    HttpStatus.BAD_REQUEST);
        }

        semesterRepository.delete(semester);

        log.info("\n  └─ CONTROLLER ─ deleteSemester\n    Deleted: {}", semesterCode);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Semester " + semesterCode + " deleted successfully")
                        .requestId(RequestContext.getRequestId())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    // ==================== TOP 5 STUDENTS LEADERBOARD ====================

    @Operation(summary = "Get top 5 students by donation received (Admin/Lecture)", description = "Returns the top 5 students who received the most donation coins in a semester. "
            + "Includes total coins received, donation count, and approved article count.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/{semesterCode}/leaderboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURE')")
    public ResponseEntity<ApiResponse<List<TopStudentDTO>>> getTopStudents(
            @Parameter(description = "Semester code (e.g., SP26)", required = true, example = "SP26") @PathVariable String semesterCode) {

        RequestContext.setCurrentLayer("CONTROLLER");
        log.info("\n  ┌─ CONTROLLER ─ getTopStudents\n  │ Semester : {}", semesterCode);

        // 1. Find semester
        Semester semester = semesterRepository.findBySemesterCode(semesterCode.toUpperCase())
                .orElseThrow(() -> new AppException(
                        "Semester not found: " + semesterCode, HttpStatus.NOT_FOUND));

        // 2. Convert semester dates to LocalDateTime for queries
        LocalDateTime fromDate = semester.getStartDate().atStartOfDay();
        LocalDateTime toDate = semester.getEndDate().atTime(LocalTime.MAX);

        // 3. Get top 5 donation receivers in this semester
        List<Object[]> topDonations = donationRepository.findTopDonationReceiversByDateRange(
                fromDate, toDate, PageRequest.of(0, 5));

        // 4. Build response with article count for each student
        List<TopStudentDTO> leaderboard = new ArrayList<>();
        int rank = 1;

        for (Object[] row : topDonations) {
            Long userId = (Long) row[0];
            String fullName = (String) row[1];
            String email = (String) row[2];
            String avatarUrl = (String) row[3];
            BigDecimal totalDonation = (BigDecimal) row[4];
            Long donationCount = (Long) row[5];

            // Count approved articles for this student in the semester
            long approvedArticles = articleRepository.countApprovedByAuthorAndDateRange(
                    userId, fromDate, toDate);

            leaderboard.add(TopStudentDTO.builder()
                    .rank(rank++)
                    .student(new UserInfoDTO(userId, fullName, email, avatarUrl))
                    .totalDonationReceived(totalDonation)
                    .donationCount(donationCount)
                    .approvedArticleCount(approvedArticles)
                    .build());
        }

        log.info("\n  └─ CONTROLLER ─ getTopStudents\n    Results : {} students", leaderboard.size());

        return ResponseEntity.ok(
                ApiResponse.<List<TopStudentDTO>>builder()
                        .success(true)
                        .message("Top students for semester " + semester.getSemesterName())
                        .data(leaderboard)
                        .requestId(RequestContext.getRequestId())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    // ==================== HELPER ====================

    private SemesterResponseDTO mapToDTO(Semester semester) {
        Optional<FeedingPeriod> feedingPeriod = feedingPeriodRepository
                .findBySemesterSemesterId(semester.getSemesterId());

        return SemesterResponseDTO.builder()
                .semesterId(semester.getSemesterId())
                .semesterCode(semester.getSemesterCode())
                .semesterName(semester.getSemesterName())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .status(semester.getStatus().name())
                .createdAt(semester.getCreatedAt())
                .hasFeedingPeriod(feedingPeriod.isPresent())
                .feedingPeriodId(feedingPeriod.map(FeedingPeriod::getPeriodId).orElse(null))
                .build();
    }
}

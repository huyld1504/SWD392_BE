package com.swd392.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "semesters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "semester_id")
    private Integer semesterId;

    @Column(name = "semester_code", unique = true, nullable = false, length = 10)
    private String semesterCode;        // "SP26", "SU26", "FA26"

    @Column(name = "semester_name", nullable = false, length = 50)
    private String semesterName;        // "Spring 2026"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SemesterStatus status = SemesterStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum SemesterStatus {
        ACTIVE,
        COMPLETED
    }

    // ===== Helper: Parse semester code → auto-calculate name and set dates =====

    /**
     * Parse semesterCode (e.g., "SP26") to auto-fill semesterName, but use provided startDate and endDate.
     */
    public static Semester fromCodeAndDates(String code, LocalDate startDate, LocalDate endDate) {
        if (code == null || code.length() != 4) {
            throw new IllegalArgumentException("Invalid semester code format. Expected: SP26, SU26, FA26");
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        String season = code.substring(0, 2).toUpperCase();
        int yearSuffix = Integer.parseInt(code.substring(2));
        int year = 2000 + yearSuffix;

        Semester semester = new Semester();
        semester.setSemesterCode(code.toUpperCase());
        semester.setStartDate(startDate);
        semester.setEndDate(endDate);

        switch (season) {
            case "SP" -> semester.setSemesterName("Spring " + year);
            case "SU" -> semester.setSemesterName("Summer " + year);
            case "FA" -> semester.setSemesterName("Fall " + year);
            default -> throw new IllegalArgumentException(
                "Invalid season prefix: " + season + ". Must be SP, SU, or FA");
        }

        return semester;
    }
}

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

    // ===== Helper: Parse semester code → auto-calculate fields =====

    /**
     * Parse semesterCode (e.g., "SP26") and auto-fill semesterName, startDate, endDate.
     */
    public static Semester fromCode(String code) {
        if (code == null || code.length() != 4) {
            throw new IllegalArgumentException("Invalid semester code format. Expected: SP26, SU26, FA26");
        }

        String season = code.substring(0, 2).toUpperCase();
        int yearSuffix = Integer.parseInt(code.substring(2));
        int year = 2000 + yearSuffix;

        Semester semester = new Semester();
        semester.setSemesterCode(code.toUpperCase());

        switch (season) {
            case "SP" -> {
                semester.setSemesterName("Spring " + year);
                semester.setStartDate(LocalDate.of(year, 1, 1));
                semester.setEndDate(LocalDate.of(year, 4, 30));
            }
            case "SU" -> {
                semester.setSemesterName("Summer " + year);
                semester.setStartDate(LocalDate.of(year, 5, 1));
                semester.setEndDate(LocalDate.of(year, 8, 31));
            }
            case "FA" -> {
                semester.setSemesterName("Fall " + year);
                semester.setStartDate(LocalDate.of(year, 9, 1));
                semester.setEndDate(LocalDate.of(year, 12, 31));
            }
            default -> throw new IllegalArgumentException(
                "Invalid season prefix: " + season + ". Must be SP, SU, or FA");
        }

        return semester;
    }
}

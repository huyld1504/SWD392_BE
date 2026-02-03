package com.swd392.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "monthly_stats",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"wallet_id", "month", "year"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Integer id;

    /* ================= RELATIONSHIP ================= */

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    /* ================= BUSINESS FIELDS ================= */

    @Column(length = 7, nullable = false)
    private String month;   // format: MM-YYYY or YYYY-MM (recommend YYYY-MM)

    @Column(length = 7, nullable = false)
    private String year;    // example: 2026

    @Column(name = "total_earned", precision = 12, scale = 2)
    private BigDecimal totalEarned = BigDecimal.ZERO;

    @Column(name = "total_spent", precision = 12, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;
}


package com.swd392.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feeding_periods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedingPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "period_id")
    private Integer periodId;

    // ===== Semester reference =====
    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    // ===== Feeding config =====
    @Column(name = "grant_amount", precision = 12, scale = 2)
    private BigDecimal grantAmount;

    // ===== Running stats (updated each daily feeding run) =====
    @Column(name = "total_coins_fed", precision = 12, scale = 2)
    private BigDecimal totalCoinsFed = BigDecimal.ZERO;

    @Column(name = "total_users_fed")
    private Integer totalUsersFed = 0;

    // ===== Status =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodStatus status = PeriodStatus.ACTIVE;

    // ===== Audit =====
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== Relationships =====
    @OneToMany(mappedBy = "feedingPeriod", cascade = CascadeType.ALL)
    private List<UserFeeding> userFeedings = new ArrayList<>();

    public enum PeriodStatus {
        ACTIVE,       // Kỳ đang hoạt động, scheduler feed hàng ngày
        COMPLETED,    // Kỳ đã kết thúc (auto hoặc manual)
        CANCELLED     // Bị hủy bởi admin
    }
}

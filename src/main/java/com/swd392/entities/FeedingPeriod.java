package com.swd392.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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

    @Column(name = "period_name", length = 50)
    private String periodName;

    @Column(name = "grant_amount", precision = 12, scale = 2)
    private BigDecimal grantAmount;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodStatus status = PeriodStatus.PENDING;

    @Column(name = "trigger_source", length = 30)
    private String triggerSource;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "feedingPeriod", cascade = CascadeType.ALL)
    private List<UserFeeding> userFeedings = new ArrayList<>();

    public enum PeriodStatus {
        PENDING, // Đã tạo, chưa chạy
        EXECUTING, // Đang xử lý
        COMPLETED, // Hoàn thành
        FAILED // Lỗi giữa chừng
    }
}

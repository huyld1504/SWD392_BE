package com.swd392.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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

    @OneToMany(mappedBy = "feedingPeriod", cascade = CascadeType.ALL)
    private List<UserFeeding> userFeedings = new ArrayList<>();
}

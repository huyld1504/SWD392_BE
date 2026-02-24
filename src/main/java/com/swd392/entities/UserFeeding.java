package com.swd392.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "user_feedings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFeeding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feeding_id")
    private Integer feedingId;

    @ManyToOne
    @JoinColumn(name = "period_id", nullable = false)
    private FeedingPeriod feedingPeriod;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "snapshot_earned_balance", precision = 12, scale = 2)
    private BigDecimal snapshotEarnedBalance;

    @Column(name = "amount_received", precision = 12, scale = 2)
    private BigDecimal amountReceived;
}

package com.swd392.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_feedings",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"period_id", "user_id"},
           name = "uk_period_user"))
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

    @Column(name = "amount_received", precision = 12, scale = 2)
    private BigDecimal amountReceived;

    @Column(name = "fed_at")
    private LocalDateTime fedAt;
}

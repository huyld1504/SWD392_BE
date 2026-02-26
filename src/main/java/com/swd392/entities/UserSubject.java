package com.swd392.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserSubject.UserSubjectId.class)
public class UserSubject {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @CreationTimestamp
    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    // Composite Key Class
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSubjectId implements Serializable {
        private Long user;
        private Integer subject;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserSubjectId that = (UserSubjectId) o;
            return Objects.equals(user, that.user) && Objects.equals(subject, that.subject);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, subject);
        }
    }
}

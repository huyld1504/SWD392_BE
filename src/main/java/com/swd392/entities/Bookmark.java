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
@Table(name = "bookmarks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Bookmark.BookmarkId.class)
public class Bookmark {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Composite Key Class
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookmarkId implements Serializable {
        private Long user;
        private Integer article;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BookmarkId that = (BookmarkId) o;
            return Objects.equals(user, that.user) && Objects.equals(article, that.article);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, article);
        }
    }
}


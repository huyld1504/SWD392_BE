package com.swd392.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "article_diagrams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDiagram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagram_id")
    private Integer diagramId;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(length = 500)
    private String caption;

    @Column(name = "sort_order")
    private Integer sortOrder;
}

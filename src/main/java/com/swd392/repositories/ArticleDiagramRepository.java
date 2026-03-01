package com.swd392.repositories;

import com.swd392.entities.Article;
import com.swd392.entities.ArticleDiagram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleDiagramRepository extends JpaRepository<ArticleDiagram, Integer> {

  List<ArticleDiagram> findByArticle(Article article);

  void deleteByArticle(Article article);
}

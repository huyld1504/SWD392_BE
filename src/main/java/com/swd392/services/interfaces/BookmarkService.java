package com.swd392.services.interfaces;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.responseDTO.ArticleResponseDTO;

import java.util.List;

public interface BookmarkService {

    void addBookmark(Integer articleId);

    PaginationResponseDTO<List<ArticleResponseDTO>> getBookmarks(
            String keyword,
            int page,
            int size
    );

    void deleteBookmark(Integer articleId);

    void deleteMultiple(List<Integer> articleIds);

    void deleteAll();
}
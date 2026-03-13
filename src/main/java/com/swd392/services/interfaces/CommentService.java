package com.swd392.services.interfaces;

import com.swd392.dtos.common.PaginationResponseDTO;
import com.swd392.dtos.requestDTO.CommentRequestDTO;
import com.swd392.dtos.responseDTO.CommentResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {

    CommentResponseDTO create(CommentRequestDTO request);


    void delete(Integer commentId);

    PaginationResponseDTO<List<CommentResponseDTO>> getByArticle(
            Integer articleId,
            Pageable pageable
    );

    CommentResponseDTO pinComment(Integer commentId);
}
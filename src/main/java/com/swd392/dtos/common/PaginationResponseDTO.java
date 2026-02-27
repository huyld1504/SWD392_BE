package com.swd392.dtos.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationResponseDTO<T> {
  private long totalItems;
  private int totalPages;
  private int currentPage;  
  private int pageSize;
  private T data;
}

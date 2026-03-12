package com.swd392.dtos.requestDTO;

import lombok.Data;

import java.util.List;

@Data
public class BookmarkDeleteRequestDTO {

    private List<Integer> articleIds;

}
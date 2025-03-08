package com.kaboot.community.board.mapper;

import com.kaboot.community.board.dto.PostRequest;
import com.kaboot.community.board.entity.Board;

public class BoardMapper {

    public static Board toBoardFromPostRequest(PostRequest postRequest, Long memberId) {
        return Board.builder()
                .title(postRequest.title())
                .content(postRequest.content())
                .memberId(memberId)
                .viewCount(0)
                .imageOriginalName(postRequest.imageOriginalName())
                .imgUrl(postRequest.imageUrl())
                .build();
    }
}

package com.kaboot.community.domain.board.mapper;

import com.kaboot.community.domain.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.member.entity.Member;

public class BoardMapper {
    public static Board toBoardFromPostRequest(PostOrModifyRequest postRequest, Member member) {
        return Board.builder()
                .title(postRequest.title())
                .content(postRequest.content())
                .member(member)
                .viewCount(0)
                .imageOriginalName(postRequest.imageOriginalName())
                .imgUrl(postRequest.imageUrl())
                .build();
    }
}

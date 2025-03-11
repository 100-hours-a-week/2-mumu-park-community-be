package com.kaboot.community.board.mapper;

import com.kaboot.community.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.board.entity.Board;
import com.kaboot.community.member.entity.Member;

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

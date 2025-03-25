package com.kaboot.community.domain.board.dto.request;

import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.member.entity.Member;
import jakarta.validation.constraints.NotEmpty;

public record PostOrModifyRequest(
        @NotEmpty
        String title,
        @NotEmpty
        String content,
        String imageOriginalName,
        String imageUrl
) {

        public Board toEntity(Member member) {
                return Board.builder()
                        .member(member)
                        .title(title)
                        .content(content)
                        .imageOriginalName(imageOriginalName)
                        .imgUrl(imageUrl)
                        .viewCount(0)
                        .build();
        }
}

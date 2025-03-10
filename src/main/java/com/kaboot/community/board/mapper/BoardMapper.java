package com.kaboot.community.board.mapper;

import com.kaboot.community.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.board.dto.response.BoardDetailResponse;
import com.kaboot.community.board.entity.Board;
import com.kaboot.community.member.entity.Member;

import java.util.List;

public class BoardMapper {

    public static Board toBoardFromPostRequest(PostOrModifyRequest postRequest, Long memberId) {
        return Board.builder()
                .title(postRequest.title())
                .content(postRequest.content())
                .memberId(memberId)
                .viewCount(0)
                .imageOriginalName(postRequest.imageOriginalName())
                .imgUrl(postRequest.imageUrl())
                .build();
    }

    public static BoardDetailResponse toBoardDetailDto(Board board, Member author, List<BoardDetailResponse.Comments> comments, Integer likesCount) {
        return BoardDetailResponse.builder()
                .boardDetail(
                        new BoardDetailResponse.BoardDetail(
                                board.getId(),
                                board.getTitle(),
                                board.getContent(),
                                board.getImageOriginalName(),
                                board.getImgUrl(),
                                author.getProfileImgUrl(),
                                board.getCreatedAt(),
                                likesCount,
                                comments.size(),
                                board.getViewCount()
                        )

                )
                .comments(comments)
                .build();
    }
}

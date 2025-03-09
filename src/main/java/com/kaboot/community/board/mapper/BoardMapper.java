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
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .imgFileName(board.getImageOriginalName())
                .contentImg(board.getImgUrl())
                .authorProfileImg(author.getProfileImgUrl())
                .createdAt(board.getCreatedAt())
                .likeCnt(likesCount)
                .commentCnt(comments.size())
                .viewCnt(board.getViewCount())
                .comments(comments)
                .build();
    }
}

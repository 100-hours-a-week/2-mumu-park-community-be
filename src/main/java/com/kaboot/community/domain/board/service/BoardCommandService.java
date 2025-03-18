package com.kaboot.community.domain.board.service;

import com.kaboot.community.domain.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.domain.board.dto.request.LikeRequest;
import com.kaboot.community.domain.board.dto.request.PostOrModifyRequest;

public interface BoardCommandService {

    void postBoard(String username, PostOrModifyRequest postRequest);
    void modifyBoard(String username, PostOrModifyRequest modifyRequest, Long boardId);
    void deleteBoard(String username,  Long boardId);
    void toggleLike(String username, Long boardId, LikeRequest likeRequest);
    void postComment(String username, Long boardId, CommentPostOrModifyRequest commentPostRequest);
    void modifyComment(String username, Long commentId, CommentPostOrModifyRequest commentModifyRequest);
    void deleteComment(String username, Long commentId);
}

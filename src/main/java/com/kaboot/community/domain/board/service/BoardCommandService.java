package com.kaboot.community.domain.board.service;

import com.kaboot.community.domain.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.domain.board.dto.request.LikeRequest;
import com.kaboot.community.domain.board.dto.request.PostOrModifyRequest;

public interface BoardCommandService {
    void postBoard(String authUsername, PostOrModifyRequest postRequest);
    void modifyBoard(String authUsername, PostOrModifyRequest modifyRequest, Long boardId);
    void deleteBoard(String authUsername,  Long boardId);
    void toggleLike(String authUsername, Long boardId, LikeRequest likeRequest);
    void postComment(String authUsername, Long boardId, CommentPostOrModifyRequest commentPostRequest);
    void modifyComment(String authUsername, Long commentId, CommentPostOrModifyRequest commentModifyRequest);
    void deleteComment(String authUsername, Long commentId);
}

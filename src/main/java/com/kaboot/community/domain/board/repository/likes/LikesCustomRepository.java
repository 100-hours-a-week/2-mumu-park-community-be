package com.kaboot.community.domain.board.repository.likes;

import com.kaboot.community.domain.board.dto.response.BoardLikeResponse;

public interface LikesCustomRepository {

  BoardLikeResponse isBoardLikedByUser(Long memberId, Long boardId);

}

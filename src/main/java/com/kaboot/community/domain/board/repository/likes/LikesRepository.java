package com.kaboot.community.domain.board.repository.likes;

import com.kaboot.community.domain.board.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByBoardIdAndMemberId(Long boardId, Long memberId);
}

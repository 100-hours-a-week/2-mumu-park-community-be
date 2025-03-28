package com.kaboot.community.domain.board.repository.likes;

import com.kaboot.community.domain.board.entity.Likes;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, Long>, LikesCustomRepository {

  Optional<Likes> findByBoardIdAndMemberId(Long boardId, Long memberId);
}

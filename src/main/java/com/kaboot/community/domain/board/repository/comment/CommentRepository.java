package com.kaboot.community.domain.board.repository.comment;

import com.kaboot.community.domain.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}

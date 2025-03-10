package com.kaboot.community.board.repository.comment;

import com.kaboot.community.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}

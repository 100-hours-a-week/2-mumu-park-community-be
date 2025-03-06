package com.kaboot.community.board.repository;

import com.kaboot.community.board.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, Long> {
}

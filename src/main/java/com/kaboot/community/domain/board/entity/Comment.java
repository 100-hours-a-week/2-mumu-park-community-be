package com.kaboot.community.domain.board.entity;

import com.kaboot.community.domain.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.common.entity.BaseEntity;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("deleted_at is NULL")
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne()
    @JoinColumn(name = "board_id")
    private Board board;

    @Column(columnDefinition = "TEXT")
    private String content;

    public void validateSameMember(Long accessMemberId) {
        if (!Objects.equals(member.getId(), accessMemberId)) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }
    }

    public void updateComment(CommentPostOrModifyRequest modifyRequest) {
        this.content = modifyRequest.content();
    }

    public void delete(LocalDateTime now) {
        this.deletedAt = now;
    }
}

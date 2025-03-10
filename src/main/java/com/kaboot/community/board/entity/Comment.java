package com.kaboot.community.board.entity;

import com.kaboot.community.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.common.entity.BaseEntity;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
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

    private Long memberId;

    private Long boardId;

    @Column(columnDefinition = "TEXT")
    private String content;

    public void validateSameMember(Long accessMemberId) {
        if (!Objects.equals(memberId, accessMemberId)) {
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

package com.kaboot.community.board.entity;

import com.kaboot.community.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long boardId;

    @Column(columnDefinition = "TEXT")
    private String content;

    public boolean isSameMember(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public void updateComment(CommentPostOrModifyRequest modifyRequest) {
        this.content = modifyRequest.content();
    }
}

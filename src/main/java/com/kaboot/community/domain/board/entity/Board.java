package com.kaboot.community.domain.board.entity;

import com.kaboot.community.domain.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.common.entity.BaseEntity;
import com.kaboot.community.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("deleted_at is NULL")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 300)
    private String imageOriginalName;

    @Column(length = 2083)
    private String imgUrl;

    private Integer viewCount;

    public void increaseViewCount() {
        this.viewCount++;
    }

    public boolean canAccess(String accessUsername) {
        return member.isMe(accessUsername);
    }

    public void update(PostOrModifyRequest modifyRequest) {
        this.title = modifyRequest.title();
        this.content = modifyRequest.content();
        this.imageOriginalName = modifyRequest.imageOriginalName();
        this.imgUrl = modifyRequest.imageUrl();
    }
}

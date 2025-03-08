package com.kaboot.community.board.entity;

import com.kaboot.community.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Board extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 300)
    private String imageOriginalName;

    @Column(length = 2083)
    private String imgUrl;

    private Integer viewCount;

    public void upViewCount() {
        this.viewCount++;
    }
}

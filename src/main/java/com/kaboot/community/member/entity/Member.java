package com.kaboot.community.member.entity;


import com.kaboot.community.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(length = 40)
    private String nickname;

    @Column(length = 2083)
    private String profileImgUrl;
}

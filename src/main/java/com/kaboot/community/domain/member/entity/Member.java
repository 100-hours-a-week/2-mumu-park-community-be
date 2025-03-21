package com.kaboot.community.domain.member.entity;


import com.kaboot.community.common.entity.BaseEntity;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("deleted_at is NULL")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private RoleType role;

    @Builder
    private Member(String username, String password, String nickname, String profileImgUrl, RoleType role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.role = role;
    }

    public void update(ModifyRequest modifyRequest) {
        this.nickname = modifyRequest.nickname();
        this.profileImgUrl = modifyRequest.profileImg();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void withdrawal(LocalDateTime now) {
        this.deletedAt = now;
    }
}

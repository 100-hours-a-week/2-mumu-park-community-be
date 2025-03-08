package com.kaboot.community.member.entity;


import com.kaboot.community.common.entity.BaseEntity;
import com.kaboot.community.member.dto.request.ModifyRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

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

    public void update(ModifyRequest modifyRequest) {
        this.nickname = modifyRequest.nickname();
        this.profileImgUrl = modifyRequest.profileImg();
    }

    public boolean isSamePassword(String checkPassword) {
        return Objects.equals(password, checkPassword);
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}

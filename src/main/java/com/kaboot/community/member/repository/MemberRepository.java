package com.kaboot.community.member.repository;

import com.kaboot.community.member.dto.MemberInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepository {
    private final JdbcTemplate jdbcTemplate;

    public MemberInfo findByMemberById(Long id) {
        String sql = "SELECT username, nickname FROM member WHERE id = " + id;

        return jdbcTemplate.queryForObject(sql, memberRowMapper());
    }

    private RowMapper<MemberInfo> memberRowMapper() {
        return (rs, rowNum) -> new MemberInfo(
                rs.getString("username"),
                rs.getString("nickname")
        );
    }
}

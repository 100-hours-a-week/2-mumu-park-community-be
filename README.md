# 💁 커뮤니티 게시판 서비스

스프링 부트를 활용한 게시판 커뮤니티 서비스입니다

## 📚 프로젝트 개요

본 프로젝트는 Spring Boot 기반의 RESTful API 서비스로, 회원 관리 및 게시판 기능을 구현한 웹 애플리케이션입니다. </br></br>
<strong>JWT(Json Web Token)</strong>를 활용해 토큰 기반 인증 시스템을 구축하였으며, <strong>테스트 코드</strong>를 통해 서비스의 안정성과 신뢰성을 높였습니다.


## ⚙️ 요구 스펙

- **언어**: Java 21
- **프레임워크**: Spring Boot 3.3.3
- **데이터베이스**: MySQL 9.0.1, Redis
- **ORM**: JPA, QueryDSL
- **인증**: Spring Security, JWT (AccessToken, RefreshToken)
- **빌드 도구**: Gradle

## 🗂️ 주요 기능

### 회원(Member) 기능

- **인증 관련**
    - 회원가입 및 로그인
        - JWT 토큰 기반 인증 (AccessToken, RefreshToken)
    - 토큰 재발급
- **회원 정보 관리**
    - 회원 정보 조회 및 수정
    - 비밀번호 변경
    - 회원 탈퇴

### 게시판(Board) 기능

- **게시글 관리**
    - 게시글 생성, 수정, 삭제
    - 전체 게시글 목록 조회
    - 게시글 상세 조회
    - 게시글 좋아요 토글 기능
    - 댓글 생성, 수정, 삭제

## 🏷️ ERD

<img width="800" alt="image" src="https://github.com/user-attachments/assets/d3add21a-8b26-42fb-8039-98152a8b6f44" />



## 🌟 프로젝트 구조

```
├── common
│   ├── dto
│   │   └── ApiResponse
│   ├── entity
│   │   └── BaseEntity
│   ├── enums
│   │   └── CustomResponseStatus
│   └── exception
│       ├── CustomException
│       ├── CustomExceptionHandler
│       └── JwtExceptionHandler
├── config
│   ├── jwt
│   ├── querydsl
│   │   └── QueryDSLConfig
│   ├── redis
│   │   └── RedisConfig
│   └── security
│       └── SecurityConfig
├── domain
│   ├── board
│   │   ├── controller
│   │   ├── dto
│   │   ├── entity
│   │   │   ├── Board
│   │   │   ├── Comment
│   │   │   └── Likes
│   │   ├── mapper
│   │   ├── repository
│   │   └── service
│   └── member
│       ├── controller
│       │   ├── AuthController
│       │   └── MemberController
│       ├── dto
│       ├── entity
│       │   └── Member
│       ├── repository
│       │   ├── MemberJdbcRepository
│       │   └── MemberRepository
│       └── service
│           ├── auth
│           │   ├── AuthService
│           │   └── AuthServiceImpl
│           ├── member
│           │   ├── MemberCommandService
│           │   └── MemberQueryService
│           └── password
│               ├── BCryptPasswordEncoder
│               └── PasswordEncoder
├── util
│   ├── jwt
│   └── redis
└── test
```

## 🏃설치 및 실행 방법

### 사전 요구사항

- Java 21
- MySQL 9.0.1
- Redis

### 설치 및 실행

1. 프로젝트 클론

```bash
git clone https://github.com/100-hours-a-week/2-mumu-park-community-be.git
cd community-board
```

2. MySQL 데이터베이스 설정

```sql
CREATE DATABASE community_board;
```

3. `application.properties` 또는 `application.yml` 파일 설정

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/community_board
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: create or update ..
  redis:
    host: localhost
    port: 6379
```

4. 애플리케이션 빌드 및 실행

```bash
./gradlew build
./gradlew bootRun
```

## ❗️ 테스트 실행

모든 테스트는 다음 명령어로 실행할 수 있습니다:

```bash
./gradlew test
```

## 📖 고민을 통한 학습

### 설계에 관하여

- [🔗 DTO의 변환 위치는 어디가 좋을까? (`Service` VS `Controller`)]()
- [🔗 `Service` 간의 의존성에 대하여]()

### 테스트에 관하여

- [🔗 정적 메서드덕에 어려워진 테스트](https://phantom-cantaloupe-293.notion.site/1c5d69fad2e68051b7b2f6d011800ae0?pvs=4)
- [🔗 `@PreAuthorize` 에 대한 단위테스트를 작성하며 느낀 테스트의 중요성](https://phantom-cantaloupe-293.notion.site/PreAuthorize-1c5d69fad2e6804d821cd9424e877cbc?pvs=4)

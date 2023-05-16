*2023.04 ~ (진행 중)*

참여 인원: 개인 프로젝트

## 소개

다양한 웹툰 서비스를 참고하여 웹툰 서비스에서 사용되는 기능들을 구현하였습니다.

### 기능

- 회원가입/로그인/로그아웃
- 작가 신청/작가 조회
- 웹툰 등록/수정, 에피소드 등록/수정
- 에피소드 목록 조회/에피소드 조회/에피소드 상세 조회
- 팔로우 추가/삭제, 별점 등록, 좋아요 추가/삭제
- 웹툰 목록 조회
    - 장르별 웹툰 목록 조회
    - 최근 본 웹툰 목록 조회
    - 새로 올라온 에피소드가 있는 웹툰 목록 조회
    - 실시간 인기 웹툰 조회



- 추가할 기능
  - 댓글 생성, 수정, 삭제, 조회
  - 대댓글 조회
  - 댓글 좋아요
  - 댓글 신고
  - 댓글 숨기기
  - 작품, 작가 검색
  - 알람

## 목표

- 가독성 있는 코드 작성
- 확장성 있는 코드 작성
- 여러 웹 서비스에 존재하는 다양한 기능 스스로 구현해보기
- 효율적인 성능

## Github

---

[https://github.com/rhdtn311/webtoon-service](https://github.com/rhdtn311/webtoon-service)

## 사용 기술 및 도구

---

- Java 17, Spring Boot 2.7.1, Spring Data JPA 2.7.1, QueryDsl 5.0m Lombok, Gradle, JBCrypt 0.4
- MySQL 8.0
- AWS S3
- Github

<br>

## 프로젝트 이슈 
- **조회 쿼리 개선**
  -  [조회 성능 개선기 - 1](https://kongding0311.tistory.com/49)
  -  [조회 성능 개선기 - 2](https://kongding0311.tistory.com/50)


-  **반복적인 로그인(인증/인가) 로직을 인터셉터로 분리**
    -  [로그인 로직 인터셉터로 분리](https://drive.google.com/file/d/1QuTMUQUZTkm7Jtl1TPYMpxe_DnmZrohR/view?usp=share_link)


-  **S3 저장소와 DB간의 데이터 동기화**
    -  [S3 저장소와 DB간의 데이터 동기화](https://kongding0311.tistory.com/47)
-  **비동기 처리로 응답 속도 개선**
    -  [비동기 처리로 응답 속도 개선](https://kongding0311.tistory.com/47#:~:text=%EC%9D%B4%EB%B2%A4%ED%8A%B8%EA%B0%80%20%EB%B0%9C%EC%83%9D%ED%95%98%EC%A7%80%20%EC%95%8A%EB%8A%94%EB%8B%A4.-,%EB%AC%B8%EC%A0%9C%202,-%EC%82%AC%EC%9A%A9%EC%9E%90%20%EC%9E%85%EC%9E%A5%EC%97%90%EC%84%9C%20%EC%83%9D%EA%B0%81%ED%95%B4%EB%B3%B4%EB%A9%B4)
  -  **Record + @ModelAttribute로 중첩 프로퍼티 바인딩 되지 않는 문제 해결**
      -  [Record + @ModelAttribute로 중첩 프로퍼티 바인딩 되지 않는 문제 해결](https://kongding0311.tistory.com/46)
  -  **확장성을 고려한 코드 작성**
        -  [확장성을 고려한 코드 작성](https://drive.google.com/file/d/1Gd1TI-r8T0mWHzXTIiP1fyGn79pqSXDe/view)

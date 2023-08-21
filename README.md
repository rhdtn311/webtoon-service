# Webtoon Service
**개인 프로젝트**
*2023-04 ~ (진행 중)*

<br>

# 🔧 사용 기술
<img  src="https://img.shields.io/badge/Java 17-FC4C02?style=for-the-badge&logo=JAVA&logoColor=white"> <img  src="https://img.shields.io/badge/SpringBoot 2.7.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img  src="https://img.shields.io/badge/Spring Data JPA 2.7.1-6DB33F?style=for-the-badge&logo=SPRING&logoColor=white">
<img  src="https://img.shields.io/badge/Querydsl 5.0.0-0769AD?style=for-the-badge&logo=&logoColor=white">
<img  src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=Gradle&logoColor=white">

<img  src="https://img.shields.io/badge/MySQL 8.0-4479A1?style=for-the-badge&logo=MySQL&logoColor=white">

<img  src="https://img.shields.io/badge/Github-181717?style=for-the-badge&logo=Github&logoColor=white"> <img  src="https://img.shields.io/badge/Github Actions-2088FF?style=for-the-badge&logo=Github Actions&logoColor=white">

<img  src="https://img.shields.io/badge/AWS EC2-FF9900?style=for-the-badge&logo=Amazon EC2&logoColor=white"> <img  src="https://img.shields.io/badge/AWS S3-569A31?style=for-the-badge&logo=Amazon S3&logoColor=white">

<br>

# 📋 개요
네이버 웹툰, 다음 웹툰 등 다양한 웹툰 서비스를 참고하여 자주 사용되는 여러 API를 직접 구현했습니다.

<br>

# 🎯 목표
-   성능을 고려한 기능 구현
-   가독성 있는 코드 작성
-   확장성 있는 코드 작성
-   여러 웹 서비스에 존재하는 다양한 기능 스스로 구현

<br>

# 🧪 주요 기능
-   회원가입/로그인/로그아웃
-   작가 신청/작가 조회
-   웹툰 등록/수정, 에피소드 등록/수정
-   에피소드 목록 조회/에피소드 조회/에피소드 상세 조회
-   팔로우 추가/삭제, 별점 등록, 좋아요 추가/삭제
-   웹툰 목록 조회
    -   장르별 웹툰 목록 조회
    -   최근 본 웹툰 목록 조회
    -   새로 올라온 에피소드가 있는 웹툰 목록 조회
    -   실시간 인기 웹툰 조회

<br>

# 🎡 서버 아키텍처
![image](https://github.com/rhdtn311/webtoon-service/assets/68289543/ada2bdd4-06fd-411d-a02f-c18f307bfc97)

<br>

# 💡 이슈 및 트러블슈팅

## 1. 조회 성능 개선 - 1
**요약: 주요 테이블 500만개 데이터 기준 0.86초 -> 0.02초로 조회 성능 개선**

<details>
<summary> <b> 확인 </b> </summary>
<div markdown="1">       

'장르별 웹툰 목록 조회' 기능은 다음과 같은 요구사항을 가집니다.
1. 사용자가 요구하는 장르에 속하는 웹툰 목록을 필터링한다.
2. 웹툰이 3일 이내 생성되었다면 가장 위에 출력한다.
3. 조회수가 높은 순서대로 나열한다.
   3-1. 조회수는 웹툰의 가장 최근 에피소드의 조회수만 계산한다.

위 요구사항을 바탕으로 다음과 같은 조회 쿼리를 도출했습니다.

```sql
SELECT c.id, c.name, a.author_name, t.image_url, c.created_at > "..."
FROM comic c
         LEFT JOIN episode e ON e.comic_id = c.id
    AND e.episode_number =
        (SELECT MAX(e2.episode_number)
         FROM episode e2
         WHERE e2.comic_id = c.id)
         LEFT JOIN view v ON e.id = v.episode_id
         JOIN author a ON a.id = c.author_id
         LEFT JOIN thumbnail t ON c.id = t.comic_id AND t.thumbnail_type = "MAIN"
WHERE c.genre = "ACTION"
GROUP BY c.id, t.image_url
ORDER BY (IF(DATE_SUB(NOW(), INTERVAL 3 DAY) <= c.created_at, 1, 2)), COUNT(v.id) DESC;
```

웹툰의 조회수를 계산하기 위해 특정 웹툰의 최근 에피소드를 찾고, 조회수 테이블에서 해당 에피소드의 id와 일치하는 로우 수를 count 해줘야 합니다.

<br>

### 문제

이때 주요 테이블인 조회수 테이블에 데이터가 500만개 있을 때 위 쿼리를 실행했더니 약 0.86초의 시간이 소요되었습니다.

![](https://blog.kakaocdn.net/dn/VFAbc/btseR9oliRh/4GTfRkGSKkvMRkDKtk7GgK/img.png)

'장르별 웹툰 목록 조회' 기능은 메인 페이지에 사용자가 접속할 때마다 호출되는 API이기 때문에 호출 수가 빈번하다고 판단했고 매 API를 호출할 때마다 단순 쿼리 수행 시간만 0.87초 걸리는 것은 성능상 좋지
않다고 판단했습니다. 따라서 쿼리 튜닝을 진행했습니다.

### 해결

우선 쿼리 실행계획을 분석했습니다.
![](https://blog.kakaocdn.net/dn/dIMoMK/btseNsQzd63/xohVquqVygzJgz721hvCu1/img.png)

이때 `select_type`에 `DEPENDENT SUBQUERY`가 존재했고, 이를 검색해본 결과 **상관 서브쿼리**라는 것을 알게되었습니다. 상관 서브쿼리는 상위 쿼리 결과에 의존하여, 상위 쿼리의 로우 하나
당 서브쿼리를 한 번씩 실행하는 쿼리로, 위 쿼리에서는 상위 쿼리의 모든 로우에 대해 `c.id`를 서브쿼리의 조건절로 사용했기 때문에 조회된 모든 로우 수만큼 서브쿼리가 실행되어 조회 성능이 떨어지게 된 것입니다.


결론적으로 `in`절을 사용하여 쿼리를 다음과 같이 개선하였습니다.

```sql
SELECT c.id, c.name, a.author_name, t.image_url, c.created_at > "..."
FROM comic c
         LEFT  JOIN episode e ON e.comic_id = c.id
    AND (e.comic_id, e.episode_number) in (SELECT e2.comic_id, max(e2.episode_number)
                                           FROM episode e2
                                           GROUP BY e2.comic_id);
LEFT JOIN  view v ON e.id = v.episode_id 
JOIN author a ON a.id = c.author_id 
LEFT JOIN thumbnail t ON c.id = t.comic_id AND t.thumbnail_type = "MAIN" 
WHERE c.genre = "ACTION" 
GROUP  BY c.id, t.image_url 
ORDER  BY (IF(DATE_SUB(NOW(), INTERVAL  3  DAY) <= c.created_at, 1, 2)), COUNT(v.id) DESC;
```

위 쿼리는 이전 쿼리와 달리 상위 쿼리의 결과에 상관없이 서브쿼리 자체로 실행 가능하기 때문에 딱 한 번만 실행됩니다.

![](https://blog.kakaocdn.net/dn/bptHr2/btseQECApF4/DgZsjnoRC4EK4SnVdqBFck/img.png)

같은 조건으로 조회 쿼리를 실행한 결과 0.07초로 개선되었습니다.

### 추가

![](https://blog.kakaocdn.net/dn/cegDe0/btseRW3Pzel/QHyfK85hRuMGooHutHa21K/img.png)

추가적으로 이를 더 개선하고자 실행계획을 확인하였고 `in`절을 검사할 때 인덱스를 적절히 사용하지 못하고 있는 것을 확인했습니다. 따라서 `(e.comic_id, e.episode_number)` 복합인덱스를
생성하였습니다.

```sql
create index comic_id_AND_episode_number on episode (comic_id, episode_number);
```
![](https://blog.kakaocdn.net/dn/cavqz8/btseQkqPOC8/xj3L2fUEFdhltCtqYuQlXK/img.png)

다시 실행계획을 분석해본 결과 커버링 인덱스를 사용하고 있는 것을 확인하였습니다.
![](https://blog.kakaocdn.net/dn/bi9bLV/btseTgtXqz9/12nmm7wWWI1ANbd7Sc4h21/img.png)

쿼리 성능 또한 0.02초로 개선되었습니다.

</div>
</details>

<br>

## 2. 조회 성능 개선 - 2
**요약: 주요 테이블 500만개 데이터 기준 5초 -> 0.001초로 조회 성능 개선**
<details>
<summary> <b> 확인 </b></summary>
<div markdown="1">
'실시간 인기 웹툰 목록 조회' 기능은 다음과 같은 요구사항을 가집니다.
1. 하루 24시간을 0시~2시, 2시~4시, ... 22시~24시와 같이 2시간 간격으로 나눕니다.
2. 현재 시간과 가장 가까운 이전의 2시간 간격 동안 조회수가 가장 높은 웹툰 순서대로 정렬합니다.

만약, 현재 시간이 오전 3시라면 0시~2시 동안 조회수가 가장 높은 웹툰 순서대로 정렬합니다.

위 요구사항을 바탕으로 다음과 같은 조회 쿼리를 도출했습니다.
```sql
SELECT comic.id, comic.name, author.author_name, thumbnail.image_url, COUNT(view.id) 
FROM  view  
INNER  JOIN episode ON view.episode_id = episode.id 
INNER  JOIN comic ON episode.comic_id = comic.id 
INNER  JOIN author ON comic.author_id = author.id 
INNER  JOIN thumbnail ON comic.id = thumbnail.comic_id 
	AND thumbnail.thumbnail_type = "SMALL" 
WHERE view.last_access_time BETWEEN 'yyyy-MM-dd HH:mm:SS' AND 'yyyy-MM-dd HH:mm:SS'  
GROUP  BY comic.id, thumbnail.image_url 
ORDER  BY  COUNT(view.id) 
DESC LIMIT 10;
```
2시간동안 웹툰, 에피소드, 조회 테이블을 조인하여 웹툰 id를 기준으로 group by 후 count하여 웹툰 별 조회수를 계산합니다. 그리고 조회수 순으로 정렬하여 상위 10개만 조회합니다.

### 문제
![](https://blog.kakaocdn.net/dn/lnjd7/btsf59gIdMl/6sCH6J1DMPRAYeWWdFAbjk/img.png)

이때 주요 테이블인 조회 테이블에 데이터가 500만개 있을 때 위 쿼리를 실행했더니 약 5초의 시간이 소요되었습니다.

![](https://blog.kakaocdn.net/dn/dwu2s0/btsf2pd47pO/STVbvikM3b0Ky6D0t7blG1/img.png)

실행 계획을 살펴보니, 약 500만개의 데이터가 있는 조회 테이블을 FULL TABLE SCAN 하기 때문이었습니다. 조회 조건인 `last_access_time` 컬럼에 인덱스를 추가하여 조회 성능을 높일 수도 있었지만 조회 테이블 특성 상 데이터의 삽입, 수정이 많은 테이블이기 때문에 인덱스를 추가하는 것은 오히려 성능 상 좋지 않을 것이라 판단했습니다.

### 해결
고려했던 해결 방법은 두 가지입니다.
1. 캐시 적용
2. 스케줄러를 사용하여 특정 시간마다 실시간 인기 웹툰 목록 계산

저는 2번 방법을 선택했습니다. 이유는 과거의 실시간 인기 웹툰 이력을 관리하기 편하고 캐시를 사용하더라도 처음 API를 호출하는 사람한테는 많은 시간이 소요될 것이라 판단했기 때문입니다.

따라서 다음과 같이 기능 구현 과정을 변경했습니다.
1. '실시간 인기 웹툰 목록'을 저장하는 테이블을 생성한다.
2. 2시간에 한 번 실시간 인기 웹툰 목록을 계산하여 테이블에 10개의 데이터를 삽입한다.
3. 실시간 인기 웹툰 목록 조회 API를 호출하면 해당 테이블에서 데이터를 조회한다.

위처럼 구현 방식을 변경하면 다음과 같은 장점이 있습니다.

1. 2시간당 10개, 하루에 120개의 데이터만 삽입되기 때문에 조회 테이블보다 데이터의 수가 현저히 적습니다. -> 조회 속도 증가
2. 데이터 변경이 적은 테이블이기 때문에 인덱스를 적용할 수 있습니다. -> 조회 속도 증가
3. 과거에 인기 있었던 웹툰 또한 빠르게 조회할 수도 있습니다.

<br>

![](https://blog.kakaocdn.net/dn/bSL5aI/btsgaMyEXI0/Cp0DVHkuqoJLOKHYVnDtok/img.png)

'실시간 인기 웹툰 목록' 테이블을 생성했습니다.
![](https://blog.kakaocdn.net/dn/nMP3a/btsgbIwdqqo/3tbNzm9RcEvNWGkeGdJNQ0/img.png)

스프링 스케줄러를 사용하여 2시간에 한 번씩 실시간 인기 웹툰 목록 테이블에 2시간동안 조회수가 가장 높은 10개의 웹툰 목록을 삽입하도록 했습니다.

### 결과

약 10년간 실시간 인기 웹툰 목록 데이터가 쌓였을 때의 데이터인 45만개의 데이터를 실시간 인기 웹툰 테이블에 넣고 실시간 인기 웹툰 목록을 조회하였습니다.

```sql
SELECT comic.id,
       realtime_comic_ranking.ranks,
       comic.name,
       author.author_name,
       thumbnail.image_url,
       realtime_comic_ranking.views
FROM realtime_comic_ranking
         INNER JOIN comic ON realtime_comic_ranking.comic_id = comic.id
         INNER JOIN author ON comic.author_id = author.id
         LEFT OUTER JOIN thumbnail ON comic.id = thumbnail.comic_id AND
                                      thumbnail.thumbnail_type = 'SMALL'
WHERE realtime_comic_ranking.record_date = '2023-05-16'
  AND realtime_comic_ranking.record_time = 'HOUR_00_02'
ORDER BY `ranks` ASC;
```
![](https://blog.kakaocdn.net/dn/cAwHRM/btsf59HUb3K/Eo7b75eYIYqKKD4HZXDBRk/img.png)

결과는 약 0.14초의 시간이 소요되었습니다.
```sql
ALTER  TABLE `web_comics`.`realtime_comic_ranking` 
ADD INDEX `record_date_AND_record_time_index` (`record_date` ASC, `record_time` ASC) VISIBLE;;
```
추가적으로 조건절에 사용되는 컬럼에 복합 인덱스를 추가했습니다.

![](https://blog.kakaocdn.net/dn/NzDqx/btsgchZr3Lo/WzTJMnNO36BOGHxkKkPO81/img.png)

결과적으로 0.00n초로 조회 시간이 개선되었습니다.
</div>
</details>
<br>

## 3. 여러 단일 insert 쿼리 -> bulk insert로 한 번에 처리하기
**요약: 여러 사용자가 조회 시 보내는 insert, update 쿼리를 인메모리에 모아 한 번에 insert 하여 DB 접근을 최소화**
<details>
<summary> <b> 확인 </b></summary>
<div markdown="1">

### 문제

![image](https://github.com/rhdtn311/webtoon-service/assets/68289543/b886e4da-5228-4c79-9e68-514860461f54)

사용자가 에피소드를 조회하면 조회 데이터가 갱신되어야 합니다. 따라서 다음과 같은 과정을 거칩니다.

1. 해당 에피소드를 이전에 조회한 적 있는지 조회
2. 에피소드에 대한 사용자의 조회 데이터를 insert/update <br>
   2-1. 조회한 적 있다면 기존 조회 데이터를 현재 시간으로 update <br>
   2-2. 조회한 적 없다면 새로운 조회 데이터를 조회 테이블에 insert

이때, 만약 10명의 사용자가 특정 웹툰의 에피소드를 조회한다면 10개의 insert/update 쿼리가 나가고, 에피소드를 조회한 사용자가 1000명이라면 1000개의 insert/update 쿼리가 나갑니다.
이렇게 DB 접근이 많아 질 수록 사용되는 비용도 그만큼 증가하게 됩니다. 따라서 insert/update 쿼리를 줄이고자 했습니다.

### 해결
![](https://www.notion.so/image/https%3A%2F%2Fs3-us-west-2.amazonaws.com%2Fsecure.notion-static.com%2Fe8c6b5ab-3528-474d-ad47-047e0fe319f8%2Fhs.svg?id=2cbc26ea-8dc6-4e9f-b33a-0d73300e3767&table=block&spaceId=92733449-5700-47a6-a223-50f1b43b5171&userId=660ca7a2-7a30-495f-b85f-55faf8b7a8d8&cache=v2)

1.  사용자가 에피소드를 조회한다.
2.  사용자가 해당 에피소드를 이전에 조회한 적 있는지 확인한다.
3.  에피소드 조회 엔티티를 생성하여 메모리에 저장한다.
4.  메모리에 일정 개수만큼 조회 데이터가 쌓였다면 한 번에 bulk insert한다.

위와 같이 사용자의 에피소드에 대한 조회 정보를 바로 DB에 보내는 것이 아니라 메모리에 쌓아두고, 일정 개수가 쌓이면 DB에 한 번에 insert/update하는 것입니다.

![](https://www.notion.so/image/https%3A%2F%2Fs3-us-west-2.amazonaws.com%2Fsecure.notion-static.com%2Fcd060f3b-0bd2-4af5-a285-9c8fc6499f55%2Fhoh.svg?id=22ea0d48-01e8-46cd-b952-ae9c13af38fd&table=block&spaceId=92733449-5700-47a6-a223-50f1b43b5171&userId=660ca7a2-7a30-495f-b85f-55faf8b7a8d8&cache=v2)

메모리에 저장할 때 사용할 Map 자료구조의 형태는 위와 같습니다. HashMap 내부에 ConcurrentHashMap이 있는 형태인데(insert, update는 각각 Map 자료구조입니다.), insert Map은 사용자가 이전에 조회한적 없는 에피소드이기 때문에 새로 생성해야 하는 조회 데이터를 담고있고, update Map은 사용자가 이전에 조회한적 있는 에피소드이기 때문에 수정해줘야 하는 조회 데이터를 담고 있습니다. 이렇게 insert와 update Map으로 나눈 이유는 bulk insert와 bulk update의 SQL문이 조금 다르기 때문입니다.

### 결과
우선 메모리에 데이터가 5000개 쌓였을 때 DB에 반영하도록 로직을 수정했습니다. 따라서 1초 동안 5000명의 서로다른 사용자가 에피소드를 조회하는 요청을 보냈습니다.

![](https://www.notion.so/image/https%3A%2F%2Fs3-us-west-2.amazonaws.com%2Fsecure.notion-static.com%2F4ad0bfb0-b6fa-480b-bbb2-fbeeccc28384%2FUntitled.png?id=7cb423a5-8b81-40eb-a084-f61f76f1e2d2&table=block&spaceId=92733449-5700-47a6-a223-50f1b43b5171&width=2000&userId=660ca7a2-7a30-495f-b85f-55faf8b7a8d8&cache=v2)

기존에는 5000개의 insert 쿼리가 나갔지만, 1번의 insert 쿼리만 나가는 것을 확인할 수 있었습니다.

![image](https://github.com/rhdtn311/webtoon-service/assets/68289543/7dbd85f2-2f3e-49f2-9f6c-269639363619)

데이터도 정상적으로 5000개가 삽입된 것을 확인할 수 있었습니다.
</div>
</details>
<br>

## 4. Spring Events로 낭비되는 S3 저장소 관리하기
**요약: S3 저장소에 사용하지 않는 이미지가 업로드되는 경우 해당 이미지를 삭제하는 이벤트를 호출하여 저장공간 효율화**

<details>
<summary> <b> 확인 </b></summary>
<div markdown="1">

### 문제

![](https://blog.kakaocdn.net/dn/deiTX5/btsbQ2Mf9dj/kP9eCVKM1JYXIki0UJac91/img.jpg)

웹툰 저장 API를 호출하면 흐름은 위와 같습니다.
여기서 2, 3번 과정을 보면 썸네일 이미지를 S3에 업로드하고 URL을 DB에 저장합니다. 이 때, 2번 과정 이후에 예외가 발생하여 트랜잭션이 롤백되면 DB에 웹툰에 대한 정보는 저장되지 않지만, S3에는 여전히
썸네일 이미지가 저장되어 있습니다.
즉, 사용하지 않는 이미지가 저장 공간을 차지하고 있는 것입니다.

### 해결

스프링에서 제공하는 `@TransactionalEventListener`을 사용하여 문제를 해결하였습니다. 해당 어노테이션에는 `phase`라는 옵션이 존재하는데, 이 옵션 값을 설정하면 트랜잭션이 롤백되었을 때
이벤트를 호출할 수 있습니다.

```java
@Transactional
public void createComic(ComicCreateRequest comicCreateRequest,String loginId){

        // 웹툰 저장 API 로직 (생략)

        applicationEventPublisher.publishEvent(new FileDeleteEvent(thumbnailImageUrl));
        }
```

위와 같이 웹툰 저장 API에서 `FileDeleteEvent`를 호출합니다.

```java
@Component  
@RequiredArgsConstructor  
public  class  FileEventListener {

    private final FileStorage fileStorage;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void deleteFile(FileDeleteEvent fileDeleteEvent) {
        fileStorage.delete(fileDeleteEvent.getKey(), ImageFileType.COMIC_THUMBNAIL);
    }
}
```
그리고 웹툰 저장 API 로직이 수행될 때마다 이벤트를 호출하는게 아니라 `TransactionPhase.AFTER_ROLLBACK`으로 설정하여 트랜잭션이 롤백되는 경우에만 S3에 저장된 이미지를 삭제하는 이벤트가 호출됩니다.
추가적으로 해당 이벤트에 `@Async` 어노테이션을 적용함으로써 비동기로 호출되도록 하였습니다. 따라서 사용자는 웹툰 저장에 실패하더라도 S3에 저장된 이미지를 삭제하는 시간까지 기다릴 필요가 없게됩니다.
</div>
</details>
<br>

## 5. 일관성 있는 예외 응답
**요약: 예외 발생 시 일관성 있는 형식의 ErrorResponse를 생성하여 반환**

<details>
<summary> <b> 확인 </b></summary>
<div markdown="1">

### 문제

서버는 클라이언트의 요청을 받아 비즈니스 로직을 수행합니다. 그러나 요청에는 검증되지 않은 잘못된 값이 들어올 수 있으며, 이로 인해 비즈니스 로직 수행 중 문제가 발생할 수 있습니다. 이를 방지하기 위해
컨트롤러에서 입력값을 제대로 검증하는 것이 중요합니다. 검증되지 않은 값으로 DB에 접근하면 예상치 못한 예외가 발생할 수 있으며, 이를 미리 방지함으로써 DB 접근을 최소화할 수 있습니다.

또한 예외 처리를 통해 일관된 형식으로 응답을 제공함으로써 클라이언트는 로직 수정 없이도 예외 상황을 처리할 수 있습니다.

### 해결
**일관된 응답 형식**

![image](https://github.com/rhdtn311/webtoon-service/assets/68289543/926d3b78-0d64-4142-b3f3-a7850033c019)

응답 형식은 위와 같은 형식을 갖습니다.
-   `code` : 발생한 에러에 대한 정보를 나타내는 코드
-   `message` : 에러에 대한 세부 메세지
-   `inputErrors` : 입력값 검증 예외 시 세부 정보
    -   `message` : 검증 에러에 대한 메세지
    -   `field` : 검증 에러가 발생한 필드

예외 처리는 `ControllerAdvice`를 사용하여 하나의 클래스에서 핸들링 하였습니다.
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 비즈니스 예외 발생 시
    @ExceptionHandler(value = BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity
                .status(HttpStatus.valueOf(errorCode.getStatus()))
                .body(ErrorResponse.basic(errorCode));
    }

    // @RequestBody + @Valid로 바인딩 에러 발생 시 
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.input(exception.getFieldErrors()));
    }

    // @ModelAttribute + @Valid로 바인딩 에러 발생 시 
    @ExceptionHandler(value = BindException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(BindException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.input(exception.getFieldErrors()));
    }

    // URL Parameter + @Validated로 바인딩 에러 발생 시
    @ExceptionHandler(value = ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(ConstraintViolationException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.input(exception.getConstraintViolations()));
    }

    // URL Parameter 바인딩 시 타입이 일치 하지 않을 경우
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.input(exception.getErrorCode(), exception.getParameter().getParameterName()));
    }

    // Query Parameter에 값이 전달되지 않은 경우
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.input(exception.getMessage(), exception.getParameterName()));
    }

    // URL은 존재하지만 대응되는 HTTP 메소드가 존재하지 않는 경우 
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.basic(METHOD_NOT_ALLOWED));
    }

    // 나머지 예외 발생 시
    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.basic(INTERNAL_SERVER_ERROR));
    }
}
```

### 결과

1. 입력값 검증 예외 발생 시

![image](https://github.com/rhdtn311/webtoon-service/assets/68289543/b83ef787-7de8-4c5c-80f3-b9f73a6c864b)
2. 비즈니스 예외 발생 시

![image](https://github.com/rhdtn311/webtoon-service/assets/68289543/26866324-6d19-4ead-988f-e1a30811e7d9)
</div>
</details>
<br>

## 6. Spring Rest Docs + Swagger 통합 문서화
**요약: Spring Rest Docs의 문서 신뢰성, Swagger의 가독성 좋은 UI의 장점을 살려 API 문서화**
<details>
<summary> <b> 확인 </b></summary>
<div markdown="1">

### 문제

API 문서화를 위해 다음 두 가지 선택지가 존재했습니다.
- Swagger
- Spring Rest Docs

Swagger는 어노테이션을 기반으로 간단하게 적용할 수 있고 문서화된 UI가 가독성이 좋다는 장점이 있지만 비즈니스 로직과 문서화 코드가 섞이게 되고, 테스트 코드를 작성하지 않아도 문서화가 가능하여 문서의
정확성이 떨어질 수 있다는 단점이 있습니다.

Spring Rest Docs는 테스트 코드를 작성해야 하기 때문에 API 문서가 신뢰성이 있고, 비즈니스 코드와 별개로 문서화 코드를 작성하기 때문에 비즈니스 로직과 문서화 코드가 섞이지 않는다는 장점이 있지만,
최종 문서를 개발자가 직접 작성해줘야 하고 개인적으로 문서 UI의 가독성이 떨어진다고 생각했습니다.

### 해결
Swagger와 Spring Rest Docs의 장점만 적용하여 Spring Rest Docs 기반의 코드로 문서화를 하고 해당 문서를 Swagger UI로 확인할 수 있도록 하였습니다.

### 결과
```java
@Test
@DisplayName("회원가입 시 이메일 중복으로 실패한다.")
void signUpDuplicatedEmailFail()throws Exception{

        // 테스트 코드 생략

        // docs
        resultActions.andDo(
        document("이메일 중복으로 회원가입 실패",
        ResourceSnippetParameters.builder()
        .tag(SIGNUP_TAG)
        .requestSchema(Schema.schema(SIGNUP_REQ_SCHEMA))
        .responseSchema(Schema.schema(COMMON_EX_OBJ_SCHEMA))
        ,
        preprocessRequest(prettyPrint()),
        preprocessResponse(prettyPrint()),
        requestFields(
        fieldWithPath(SIGNUP_LOGIN_ID_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_LOGIN_ID_REQ_DESCRIPTION),
        fieldWithPath(SIGNUP_LOGIN_ID_ID_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_LOGIN_ID_ID_VALUE_REQ_DESCRIPTION),
        fieldWithPath(SIGNUP_NAME_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_NAME_REQ_DESCRIPTION),
        fieldWithPath(SIGNUP_EMAIL_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_EMAIL_REQ_DESCRIPTION),
        fieldWithPath(SIGNUP_EMAIL_ADDRESS_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_EMAIL_ADDRESS_REQ_DESCRIPTION),
        fieldWithPath(SIGNUP_NICKNAME_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_NICKNAME_REQ_DESCRIPTION),
        fieldWithPath(SIGNUP_PASSWORD_REQ_FIELD).type(JsonFieldType.OBJECT).description(SIGNUP_PASSWORD_REQ_DESCRIPTION),
        fieldWithPath(SIGNUP_PASSWORD_VALUE_REQ_FIELD).type(JsonFieldType.STRING).description(SIGNUP_PASSWORD_VALUE_REQ_DESCRIPTION)
        ),
        responseFields(
        fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
        fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
        fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
        )
        )
        );
        }
```
API 문서화를 위한 테스트 코드를 작성합니다.
![](https://blog.kakaocdn.net/dn/b3pTZs/btso12ewOYU/8EdbHVRZ3dAiGojgoX5IA0/img.png)
Swagger UI로 API 문서가 생성됩니다.
</div>
</details>

<br>

## 7. ArgumentResolver를 커스텀하여 세션 의존성 제거

**요약: `ArgumentResolver`를 커스텀하여 세션에 의존적인 파라미터 바인딩 코드를 제거하여 변경에 유연한 코드로 리팩토링**
<details>
<summary> <b> 확인 </b></summary>
<div markdown="1">

### 문제

현재는 세션 로그인 방식을 사용하고 있습니다.
따라서 로그인된 사용자에 대한 정보를 가져오기 위해서는 세션에 저장된 사용자의 정보를 가져와야 했습니다.

세션에 저장된 사용자의 정보를 가져오기 위해 `@SesseionAttribute` 어노테이션을 사용했습니다.

```java
@LoginCheck(authority = UserAuthority.USER)
@PostMapping
public ResponseEntity<Void> createAuthor(
@RequestBody @Valid AuthorCreateRequest authorCreateRequest,
@SessionAttribute(value = UserSessionUtil.LOGIN_MEMBER_ID, required = false) UserAuthDTO userAuth,
        HttpServletRequest httpServletRequest
        ){
        Long savedAuthId=authorService.createAuthor(authorCreateRequest,userAuth.loginId());

        return ResponseEntity.created(URI.create(httpServletRequest.getRequestURI()+"/"+savedAuthId)).build();
        }
```

현재까지 문제는 없지만 나중에 세션 로그인 방식이 아닌 JWT 로그인 방식이나 혹은 또 다른 로그인 방식으로 변경하게 될 수도 있습니다.
이런 경우 `@SessionAttribute`를 사용했던 코드를 전부 수정해줘야 할 수도 있다고 판단했습니다.

### 해결

`ArgumentResolver`를 커스텀하여 로그인 방식에 따른 `UserAuthDTO`를 바인딩하도록 합니다.

```java

@Component
public class UserAuthSessionArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(UserAuthDTO.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.SESSION_EXPIRATION);
        }

        return UserSessionUtil.getLoginUserAuth(session);
    }
}
```

현재는 세션 로그인 방식을 사용하고 있기 때문에 `UserAuthDTO` 타입으로 파라미터를 받는다면
사용자 요청에 해당하는 세션에 저장되어 있는 `UserAuthDTO` 객체를 반환하도록 하였습니다.

### 결과

```java
@LoginCheck(authority = UserAuthority.USER)
@PostMapping
public ResponseEntity<Void> createAuthor(
@RequestBody @Valid AuthorCreateRequest authorCreateRequest,
        UserAuthDTO userAuth,
        HttpServletRequest httpServletRequest
        ){
        Long savedAuthId=authorService.createAuthor(authorCreateRequest,userAuth.loginId());

        return ResponseEntity.created(URI.create(httpServletRequest.getRequestURI()+"/"+savedAuthId)).build();
        }
```

이제 로그인한 사용자에 대한 정보(`UserAuthDTO`)를 받을 때 `@SessionAttribute`를 사용하지 않습니다.
즉, 세션에 대한 의존성이 사라지게 되었습니다.

만약 로그인 방식이 JWT 방식으로 바뀐다면

```java

@RequiredArgsConstructor
@Component
public class UserAuthJwtArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenManager jwtTokenManager;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(UserAuthDTO.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        try {
            String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
            String token = getJwtTokenByHeader(authorizationHeader);
            return jwtTokenManager.getUserAuthFromToken(token);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("인증 헤더가 존재하지 않습니다.");
        }
    }

    private String getJwtTokenByHeader(String authorizationHeader) {
        return authorizationHeader.split(" ")[1];
    }
}
```

위와 같이 `ArgumentResolver`만 변경해주면 컨트롤러의 파라미터에 대한 코드 변경 없이
사용자 인증 정보를 동일한 코드로 받을 수 있습니다.
</div>
</details>

<br>

## 8. 객체지향적으로 중복 검증 로직 제거

**요약: `@Embedded`를 활용하여 사용자의 로그인 ID를 `String` 대신 `LoginId`라는 객체로 관리함으로써
여러 곳에 나뉘어 있던 검증 로직을 하나의 객체에서 공통 처리하도록 리팩토링**
<details>
<summary> <b> 확인 </b></summary>
<div markdown="1">

### 문제

사용자인 `User`는 로그인ID를 가지고있습니다. 현재는 이 값들을 `String` 타입으로 표현하고 있지만,
이렇게되면 다음과 같이 동일한 검증 로직이 중복되는 문제가 발생합니다.

현재 로그인 ID를 검증하는 기능은 여러 API에서 이루어집니다.

```java
// 1. 회원가입 API
@PostMapping("/signup")
public ResponseEntity<Void> signup(
// SignupRequest 내부에서 loginId에 대한 검증이 이루어짐
@RequestBody @Valid SignupRequest signupRequest,
        HttpServletRequest httpServletRequest
        ){
        Long savedUserId=userService.signup(signupRequest);

        return ResponseEntity
        .created(URI.create(httpServletRequest.getRequestURI()+"/"+savedUserId))
        .build();
        }

public record SignupRequest(
        @NotBlank
        @Length(min = 5, max = 20)
        String loginId,
        // ...
) {
}

    // 2. 회원가입 시 로그인 ID의 중복을 검증하는 API
    @PostMapping("/signup/check-duplicate-id/{loginId}")
    public ResponseEntity<Void> checkDuplicateId(
            // 마찬가지로 같은 조건의 검증이 이루어짐
            @PathVariable @NotBlank @Length(min = 5, max = 20) String loginId
    ) {
        userService.validateDuplicateLoginId(loginId);

        return ResponseEntity.noContent().build();
    }

    // 3. 로그인 API
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletRequest httpServletRequest
    ) {
        UserAuthDTO userAuth = userService.login(loginRequest);

        HttpSession session = httpServletRequest.getSession();
        UserSessionUtil.setLoginUserAuth(session, userAuth);

        return ResponseEntity.noContent().build();
    }

public record LoginRequest(
        @NotBlank
        @Length(min = 5, max = 20)
        String loginId,
        // ...
) {
}
```

위 코드에서 보이듯이, 회원가입 API, 로그인 API에서 각각 같은 검증 코드가 중복적으로 작성됩니다.
현재는 3개의 API지만, 만약 10개의 API에서 로그인 ID를 요청으로 받는다면 10개의 중복 검증 코드가 생기고,
로그인 ID에 대한 검증 조건이 바뀌게 된다면 또 10개의 API에서 코드를 수정해줘야 합니다.
또한 실수로 한 두개의 API에서는 다른 검증 조건으로 코드를 작성할 수도 있다고 생각했습니다.

### 해결

로그인 ID, 이메일, 패스워드를 `String`이 아닌 객체로 관리하여 검증 로직을 해당 객체 내에서 공통처리 합니다.

```java

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class LoginId {

    private static final int MIN_ID_LENGTH = 5;
    private static final int MAX_ID_LENGTH = 20;
    private static final String INVALID_ID_LENGTH_MESSAGE = "로그인 ID 길이 검증에 실패했습니다.";

    @NotBlank
    @Length(min = MIN_ID_LENGTH, max = MAX_ID_LENGTH)
    @Column(name = "login_id", unique = true, length = 15, nullable = false)
    private String idValue;

    public LoginId(String idValue) {
        Assert.isTrue(validatedLoginIdLength(idValue), INVALID_ID_LENGTH_MESSAGE);

        this.idValue = idValue;
    }

    private boolean validatedLoginIdLength(String idValue) {
        return idValue.length() >= MIN_ID_LENGTH && idValue.length() <= MAX_ID_LENGTH;
    }
}
```

`LoginId` 클래스를 생성합니다. 이 클래스는 로그인 ID에 대한 상태와 행위를 가지고 있습니다.

- `@Embeddable` : `User` 클래스 내부에서 엔티티의 값으로 사용되기 때문에 해당 어노테이션을 적용합니다.
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` : 기본 생성자는 사용할 일이 없기 때문에 접근을 최대한 막습니다.
- `@NotBlank`, `@Length` : 입력값 검증 어노테이션을 적용합니다. 이렇게 `LoginId`와 관련된 입력값 검증 로직을 해당 클래스에서 처리함으로써 `String`으로 관리할 때 여러 컨트롤러에서
  중복되던 로직이 하나의 클래스에서 관리됩니다.
- `validatedLoginLength()` : `LoginId` 객체가 입력값을 바인딩 하지 않고 코드를 통해 생성되는 경우 값 검증을 하기 위한 메소드입니다.

### 결과

```java
// 1. 회원가입 API
@PostMapping("/login")
public ResponseEntity<Void> login(
@RequestBody @Valid LoginRequest loginRequest,
        HttpServletRequest httpServletRequest
        ){...}

// 2. 회원가입 시 로그인 ID의 중복을 검증하는 API
@PostMapping("/signup")
public ResponseEntity<Void> signup(
@RequestBody @Valid SignupRequest signupRequest,
        HttpServletRequest httpServletRequest
        ){...}

public record SignupRequest(
        @Valid
        LoginId loginId
)

        // 3. 로그인 API
        @PostMapping("/signup/check-duplicate-id/{loginId}")
        public ResponseEntity<Void> checkDuplicateId(
                @PathVariable @Valid LoginId loginId
        ) { ...}
```

이전과 달리 이제 `LoginId`를 요청으로 받는 API 마다 검증 어노테이션을 적용해 주는게 아니라 `LoginId` 내부에 검증 로직이 작성되어 있으니
`@Valid` 어노테이션만 작성해주면 됩니다.

### 배운점

종합적으로 `String` 대신 `LoginId`와 같이 객체로 관리하면서 다음과 같은 이점이 있습니다.

1. 중복 로직 제거
    1. `LoginId`와 관련된 로직이 `LoginId` 클래스 내부에 정의됩니다.
2. 명확한 의미 전달
    1. 애매한 변수나 파라미터 명이 아닌 클래스 명으로 어떤 책임을 가지는지 명확하게 알 수 있습니다.
3. `User` 객체의 책임 분산 및 `LoginId` 객체의 응집도 증가
    1. 만약 로그인 ID의 길이를 반환하는 기능이 있다고 했을때, 로그인 ID를 `String`으로 관리한다면 `User`나 서비스 레이어에서 해당 기능을 구현할 수 있습니다. `User`에서
       구현한다면 `User`가 가지는 책임이 많아지고, 서비스 레이어에서 구현한다면 로그인 ID의 길이를 반환하는 기능이 여러 서비스 레이어에서 필요한 경우, 서비스 레이어마다 중복적으로 코드를 작성해줘야
       합니다. 이를 `LoginId` 객체 내부에서 관리함으로써   `User` 객체의 책임을 분산하고, `LoginId` 객체의 응집도를 증가시킬 수 있습니다.

마지막으로 조영호님의 <오브젝트 2장: 객체지향 프로그래밍>에 다음과 같은 구절이 있습니다.

> 금액을 구현하기 위해 `Long` 타입을 쓸 수 있지만, `Money`라는 객체를 만들어서 사용할 수도 있다. 기본 타입 대신 객체를 만들어서 사용하면 저장하는 값이 금액과 관련돼 있다는 의미를 전달할 수 있다.
> 또한 금액과 관련된 로직이 여러 곳에 중복되어 구현되는 것을 막을 수 있다. 따라서 의미를 좀 더 명시적이고 분명하게 표현할 수 있다면 객체를 사용해서 해당 개념을 구현하라 그 개념이 비록 하나의 인스턴스 변수만
> 포함하더라도 개념을 명시적으로 표현하는 것이 전체적인 설계의 명확성과 유연성을 높이는 첫걸음이다.
>

즉, 객체를 사용하여 명시적으로 개념을 표현하거나 중복 구현을 제거함으로써 설계의 명확성과 유연성을 높이는게 좋은 설계라 생각합니다.
</div>
</details>

<br>

## 9. 불필요한 객체 생성 막기

**요약: 이메일 검증 요청마다 항상 새로운 `Pattern` 인스턴스를 생성하는 비효율적인 메소드를 `Pattern` 인스턴스를 캐싱하여 재사용 하도록 효율화**
<details>
<summary> <b> 확인 </b></summary>
<div markdown="1">

### 문제

현재 이메일을 검증하는 기능은  `ConstraintValidator`를 구현한 `EmailFormatValidator`에서 수행하고 있습니다.
그리고 이메일 검증 메소드는 다음과 같이 구현되어 있습니다.

```java

@Component
public class EmailFormatValidator implements ConstraintValidator<EmailValid, Email> {
    // ...
    private static boolean validateEmailAddress(Email email) {
        return !Pattern.matches(RegexConst.EMAIL_VALID_REGEX, email.getAddress());
    }
}
```

정규식으로 올바른 이메일인지 검증하기 위해서 `Pattern.matches()` 메소드를 사용하고 있는데, 해당 메소드의 코드는 내부적으로 다음과 같습니다.

```java
public static boolean matches(String regex,CharSequence input){
        Pattern p=Pattern.compile(regex);
        Matcher m=p.matcher(input);
        return m.matches();
        }
```

즉, 이메일을 검증할 때마다 새로운 `Pattern` 객체를 생성하고 한 번 사용한 뒤 GC의 대상이 됩니다.

이처럼 불필요한 객체의 생성을 막기 위해 다음과 같이 해결할 수 있습니다.

### 해결

#### 1. 직접 `Pattern` 인스턴스 캐싱

검증하려는 정규표현식에 대한 정보를 가지고 있는 `Pattern` 인스턴스를 미리 만들어 놓고 검증 요청이 올 때마다 해당 인스턴스를 재사용합니다.

```java

@Component
public class EmailFormatValidator implements ConstraintValidator<EmailValid, Email> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(RegexConst.EMAIL_VALID_REGEX);

    // ...

    private static boolean validateEmailAddress(Email email) {
        return EMAIL_PATTERN.matcher(email.getAddress())
                .matches();
    }
}
```

<br>

#### 2. `@Pattern` 어노테이션 사용

`javax.validation.constraints.Pattern` 어노테이션을 사용하여 검증합니다.

```java

@Embeddable
public class Email {

    private static final String INVALID_EMAIL_MESSAGE = "이메일 형식이 일치하지 않습니다.";

    @Pattern(regexp = RegexConst.EMAIL_VALID_REGEX, message = INVALID_EMAIL_MESSAGE) // 추가
    @Length(max = 320)
    @NotBlank
    @Column(name = "email", unique = true, length = 320, nullable = false)
    private String address;
}
```

`@Pattern` 어노테이션을 적용하면, 요청이 왔을 때 `PatternValidator`에서 검증 로직을 수행합니다.

```java
public class PatternValidator implements ConstraintValidator<Pattern, CharSequence> {

    private java.util.regex.Pattern pattern;

    // ...

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }

        if (constraintValidatorContext instanceof HibernateConstraintValidatorContext) {
            constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class).addMessageParameter("regexp", escapedRegexp);
        }

        Matcher m = pattern.matcher(value);
        return m.matches();
    }
}
```

`PatternValidator`는 스프링 빈으로 등록되어 있기 때문에 싱글톤 객체이고, 거기에 `pattern` 필드값으로 `@Pattern`의 `regexp`에 입력한 값을 가지고 있습니다.
그리고 `isValid()` 메소드를 보면 해당 `pattern` 인스턴스를 사용하는 것을 알 수 있습니다.

즉, `@Pattern` 어노테이션을 통해 정규식을 검증할 때마다 애플리케이션 실행 시 미리 만들어 놨던 `Pattern` 인스턴스를 재사용합니다.
따라서 저는 간편하게 적용 가능하고 일관성 있도록 `@Pattern` 어노테이션을 적용하여 검증 요청 때마다 새로운 `Pattern` 인스턴스를 생성하는 문제를 해결했습니다.
</div>
</details>

<br>
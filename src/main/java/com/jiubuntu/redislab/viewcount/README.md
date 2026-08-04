# INCR + SET 자료구조 를 사용한 정합성 지키기(상품 조회 수) 

`GET`으로 현재 값을 읽고 애플리케이션에서 +1 한 뒤 `SET`으로 되돌려 쓰는 방식 대신, Redis의 `INCR`로 값을 원자적으로 증가시켜 조회수 카운터의 정합성을 지키는 패턴이다. <br>여기에 Redis Set(`SADD`)으로 이미 조회한 유저를 관리해, 같은 유저가 여러 번 조회해도 조회수가 계속 오르지 않도록 한다.

## 개념

- `GET` → `+1` → `SET` 방식은 두 요청이 동시에 들어오면 서로의 증가분을 덮어써 값이 유실될 수 있다. (Lost Update / Race Condition)
- `INCR`은 Redis 서버 내부에서 값을 읽고 증가시키는 과정이 하나의 원자적(atomic) 연산으로 처리되기 때문에, 동시에 수천 개의 요청이 들어와도 값이 유실되지 않는다.
- Set은 중복 값을 허용하지 않는 자료구조이고 `SADD`는 이미 존재하는 값을 추가하면 `0`을, 새로 추가되면 `1`을 원자적으로 반환하기 때문에 반환 값으로 INCR 실행유무를 판단할 수 있다.
- `SADD`로 새로운 유저임이 확인됐을 때만 `INCR`을 호출해, 같은 유저의 재조회는 조회수에 반영되지 않는다.

## 동작 흐름

### 조회수 증가 (Write)
1. `SADD article:{articleId}:viewers {userId}` : 조회 이력 Set에 유저를 추가한다. 반환값이 `1`이면 처음 조회하는 유저, `0`이면 이미 조회한 유저다.
2. 새로운 유저일 때만 `INCR article:{articleId}:views`로 조회수를 원자적으로 1 증가시킨다. 이미 조회한 유저라면 조회수는 그대로 두고 현재 값만 조회해서 반환한다.
3. 조회수가 특정 단위(예: 100)에 도달할 때만 메인 DB에 동기화한다고 가정한다. 매 요청마다 DB에 쓰지 않아 DB 부하를 줄인다. 

### 조회수 조회 (Read)
- `GET article:{articleId}:views` — 조회수를 조회한다. 값이 없으면 0으로 취급한다.

## 이 패턴의 장점
### 정합성 + 성능

- `GET → +1 → SET`을 애플리케이션에서 직접 구현하면 동시성 문제를 막기 위해 별도의 락(lock)이 필요하다. `INCR` 하나로 이 문제를 완전히 해결하면서도, 락 대기 없이 매우 빠르게 처리된다. 
- 여기에 `SADD`의 원자적 반환값을 조건으로 사용하면, 별도의 조회 로직 없이도 중복 조회를 걸러낼 수 있다.
- Write-Back 패턴을 사용하여 매 요청마다 DB에 쓰지 않아 부하가 크게 감소한다.

## 이 예제의 구조

```
viewcount
├── controller/ArticleViewCountController.java   # 조회수 증가(SADD+INCR) + 조회수 조회
└── dto/ArticleViewResponse.java                 # 조회수 응답 DTO
```

이 예제도 Redis 자체가 원본 데이터이므로 별도의 `repository` 계층 없이 `RedisService`(`addToSet`/`increment`/`getCounter`)만으로 흐름을 표현한다.

- `POST /articles/{articleId}/view` → `SADD` → (새 유저면) `INCR` (원자적 증가 + 조건부 DB 동기화)
- `GET /articles/{articleId}/stats` → `GET` (조회수)




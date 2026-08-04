# Redis Lab

Redis 실무 패턴을 하나씩 학습하고, 개념 정리와 동작하는 Spring Boot 예제 코드로 검증하는 저장소.

각 패턴은 독립된 패키지로 분리되어 있고, 패키지 안의 `README.md`에 개념/동작 흐름/API 테스트 방법이 정리되어 있다.

## 학습한 패턴

| 패턴                        | 설명                                          | 코드 위치 |
|---------------------------|---------------------------------------------| --- |
| Cache-Aside               | 캐시를 먼저 조회하고 없으면 DB 조회 후 캐싱, 쓰기 시 캐시 무효화     | [`cacheaside/`](src/main/java/com/jiubuntu/redislab/cacheaside) |
| 고정 크기 큐(Fixed-size Queue) | Redis List(`LPUSH`+`LTRIM`)로 최근 본 상품 목록을 구현 | [`recentview/`](src/main/java/com/jiubuntu/redislab/recentview) |
| INCR + SET 자료구조를 사용한 정합성 지키기(상품 조회수) | `SADD`로 유저별 중복 조회를 걸러내고, 새 유저일 때만 `INCR`로 조회수를 원자적으로 증가 | [`viewcount/`](src/main/java/com/jiubuntu/redislab/viewcount) |
| TTL 기반 임시 인증번호 | 해싱한 전화번호를 key로 인증번호를 TTL과 함께 저장, 검증 성공 시에만 즉시 삭제 | [`authcode/`](src/main/java/com/jiubuntu/redislab/authcode) |

## 프로젝트 구조

```
redis-lab
└── src/main/java/com/jiubuntu/redislab
    ├── RedisLabApplication.java      # 애플리케이션 진입점
    │
    ├── common                        # 여러 패턴에서 공통으로 쓰는 설정/유틸
    │   ├── config/                   # Redis, OpenAPI(Swagger) 설정
    │   └── redis/RedisService.java   # Redis 공용 서비스 (get/set/delete 등)
    │
    ├── cacheaside                    # Cache-Aside 패턴 예제 
    │   ├── controller/                   # 캐시 조회/무효화 로직
    │   ├── dto/                          # 요청/응답 DTO
    │   └── repository/                   # DB를 대신하는 인메모리 저장소
    │
    ├── recentview                    # Fixed-size Queue 패턴 예제 
    │   ├── controller/                   # 최근 본 목록 갱신/조회 로직
    │   └── dto/                          # 응답 DTO
    │
    ├── viewcount                     # INCR + SET 정합성 패턴 예제 
    │   ├── controller/                   # 조회수 증가/조회 로직
    │   └── dto/                          # 응답 DTO
    │
    └── authcode                      # TTL 기반 임시 인증번호 패턴 예제
        ├── controller/                   # 인증번호 발송/검증 로직
        └── dto/                          # 요청/응답 DTO
```

## 실행 방법

- Redis는 별도로 설치/기동할 필요 없음. `docker-compose.yml`을 통해 앱 실행 시 자동으로 컨테이너가 뜨고, 앱 종료 시 함께 내려간다.
- 실행 후 아래 주소에서 Swagger를 통해 API를 바로 테스트할 수 있다.

```
http://localhost:8080/swagger-ui/index.html
```

## 기술 스택

- Java 21
- Spring Boot 4.1.0 (Spring Web MVC, Spring Data Redis)
- Redis (Docker Compose로 자동 기동)
- springdoc-openapi (Swagger UI)

<br>
학습 출처 : 2026년! 백엔드 개발자를 위한 Redis 실전 가이드: 기초부터 실무 패턴까지 - 코딩하는기술사 (인프런 강의)

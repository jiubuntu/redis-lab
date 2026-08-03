# Fixed-size Queue (최근 본 상품 목록)

Redis List(`LPUSH` + `LTRIM`)를 고정 크기 큐처럼 활용해, 유저별 최근 본 상품 목록을 최신순으로 N개만 유지하는 패턴이다.

## 개념 및 동작흐름

- Redis List는 저장 순서를 유지하는 자료구조라 최신순 정렬이 자연스럽게 표현된다.
- `LPUSH`로 맨 앞에 최신 항목을 넣고, `LTRIM`으로 리스트 길이를 항상 N개로 잘라내면 별도의 만료/정리 배치 없이도 크기가 고정된 큐가 된다.
- 동일한 상품을 다시 본 경우 "가장 최근"으로 끌어올려야 하므로, `LPUSH` 전에 `LREM`으로 기존 항목을 먼저 제거해 중복을 막는다.


## 이 패턴의 장점

### 압도적인 성능과 단순한 로직

- DB에서 수많은 조회 이력 데이터를 저장하고 저장된 데이터들을 최신순으로 나열하는 작업은 많은 비용이 든다.<br>Redis List 를 사용하면 이 문제를 빠르고 단순하게 해결할 수 있다.





## 이 예제의 구조

```
recentview
├── controller/RecentViewController.java   # 조회 시 목록 갱신 + 목록 조회
└── dto/RecentViewResponse.java            # 최근 본 목록 응답 DTO
```

Redis List 자체가 최근 본 상품의 원본 데이터이기 때문에, 별도의 `repository` 계층 없이 `RedisService`(List 연산)만으로 흐름을 표현한다.

- `POST /products/{productId}/view` → `LREM` → `LPUSH` → `LTRIM` (고정 크기 큐 갱신)
- `GET /users/{userId}/recent-views` → `LRANGE`로 리스트 조회



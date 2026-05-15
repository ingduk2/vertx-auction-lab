# vertx-auction-lab

Spring Boot 4 + Vert.x 5로 만드는 실시간 경매 시스템 학습 프로젝트

> Vert.x의 핵심 개념인 Verticle, EventBus, WebSocket을 직접 구현하며 익히는 것이 목표

---

## 🛠 기술 스택

| 항목 | 버전 |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.x |
| Vert.x | 5.0.x |
| Build | Gradle Kotlin DSL |
| DB | H2 (인메모리, 개발용) |
| ORM | Spring Data JPA |

---

## 🏗 아키텍처 개요

```
브라우저
  │
  │  WebSocket (:9090)
  ▼
WebSocketVerticle          ← 연결 관리, 세션 매핑
  │
  │  EventBus
  ▼
AuctionVerticle            ← 경매 핵심 로직 (입찰, 최고가, 타이머)
  │
  │  EventBus
  ▼
ResultVerticle             ← 결과 처리 및 브로드캐스트
  │
  │  Worker Verticle (블로킹 격리)
  ▼
Spring Data JPA            ← 경매 이력 영속성

Spring REST (:8080)        ← 관리 API (경매 생성, 조회 등)
```

**포트 분리 전략**
- `:8080` — Spring (REST 관리 API)
- `:9090` — Vert.x HttpServer (WebSocket 실시간 통신)

---

## 📚 커리큘럼

### Chapter 0 — 환경 세팅 & 프로젝트 구조
> Spring Boot 4 + JDK 25 + Vert.x 5 공존시키기

- 0-1. Spring Initializr 세팅 & `build.gradle.kts` Vert.x 의존성 추가
- 0-2. 프로젝트 패키지 구조 설계 & README 작성

---

### Chapter 1 — Verticle & Event Loop 이해
> Vert.x가 왜 빠른지, 어떻게 동작하는지부터

- 1-1. Event Loop 모델 & Thread 구조 — Netty 위에서 어떻게 도는가
- 1-2. Verticle이란 — Actor 모델과의 비교
- 1-3. Spring Bean에서 Verticle 배포하기 & 포트 분리 전략 — `VertxConfig` 구현
- 1-4. 절대 하면 안 되는 것 — Event Loop blocking 패턴

---

### Chapter 2 — EventBus 설계
> Verticle끼리 어떻게 말 걸고, 안 깨지게 하나

- 2-1. EventBus 주소 체계 설계 — `auction.bid.request` / `auction.result.broadcast`
- 2-2. Request-Reply vs Publish-Subscribe — 언제 뭘 쓰나
- 2-3. 메시지 직렬화 — `JsonObject` & `MessageCodec` 등록
- 2-4. 에러 핸들링 & 타임아웃 패턴

---

### Chapter 3 — WebSocket Verticle
> 브라우저와 연결하는 관문

- 3-1. `HttpServerVerticle` 구현 — Vert.x HttpServer + WebSocket 업그레이드
- 3-2. 세션 관리 — `connectionId` / `userId` 매핑 & 연결 끊김 처리
- 3-3. WebSocket → EventBus 브릿지 — 입찰 요청 수신
- 3-4. EventBus → WebSocket 브로드캐스트 — 결과 전체 전송

---

### Chapter 4 — 경매 비즈니스 로직 Verticle
> 입찰, 최고가 갱신, 타이머 마감

- 4-1. `AuctionVerticle` 설계 — 상태를 어디에 들고 있을 것인가
- 4-2. 입찰 처리 로직 — 최고가 갱신 & 동시 입찰 처리
- 4-3. 경매 타이머 — Vert.x `periodic timer`로 마감 처리
- 4-4. Spring Service 호출 패턴 — Worker Verticle로 블로킹 격리
- 4-5. Verticle 핸들러 분리 리팩토링 — AuctionVerticle, WebSocketVerticle
- 4-6. CompletableFuture로 EventBus 응답 대기 — WebMVC 유지
- 4-7. WebFlux로 교체 — Mono로 EventBus 응답 대기

---

### Chapter 5 — 영속성 & 비동기 DB
> Event Loop 막지 않고 JPA 쓰기

- 5-1. Event Loop에서 JPA 부르면 왜 위험한가 — 블로킹 문제
- 5-2. Worker Verticle 패턴으로 JPA 격리
- 5-3. 경매 이력 저장 설계 & 엔티티 구성
- 5-4. WebFlux 환경에서 H2 콘솔 설정 — 별도 서버로 띄우기
- 5-5. Vert.x Reactive SQL Client vs JPA — 왜 이 프로젝트는 JPA를 선택했는가

---

### Chapter 6 — 완성 & 테스트
> 여러 탭 띄워서 실제로 경매 해보기

- 6-1. HTML 클라이언트 — WebSocket 연결 & 실시간 입찰 UI
- 6-2. 시나리오 테스트 — 동시 입찰, 타임아웃, 연결 끊김
- 6-3. k6로 부하 살짝 걸어보기

---

## 📁 패키지 구조

```
src/main/java/com/auction/lab/
├── config/
│   ├── VertxConfig.java          ← Vertx 인스턴스 빈 등록 & Verticle 배포
│   └── JpaConfig.java
├── verticle/
│   ├── WebSocketVerticle.java    ← Chapter 3
│   ├── AuctionVerticle.java      ← Chapter 4
│   └── ResultVerticle.java
├── auction/
│   ├── AuctionService.java       ← Spring Service (Worker Verticle에서 호출)
│   ├── AuctionRepository.java
│   └── Auction.java              ← JPA Entity
└── VertxAuctionLabApplication.java
```

---

## 🚀 실행 방법

```bash
./gradlew bootRun
```

- Spring REST API: `http://localhost:8080`
- WebSocket: `ws://localhost:9090/auction`

---

## 📝 학습 포인트

- **Vert.x + Spring 공존** — 각자 잘하는 걸 맡긴다
- **EventBus = 경량 내부 MQ** — Kafka 없이 Verticle 간 비동기 통신
- **Worker Verticle** — 블로킹 코드(JPA)를 Event Loop에서 격리하는 패턴
- **WebSocket 브로드캐스트** — 입찰 결과를 모든 참여자에게 실시간 전달
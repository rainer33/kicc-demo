# KICC 결제 데모 (Spring Boot + Gradle + Vue + MariaDB)

## 실행 명령어
프로젝트 루트에서 실행:
```bash
./bootrun docker
```

프론트엔드(dev) 실행:
```bash
./run
```

백엔드만 직접 실행:
```bash
cd backend
./gradlew bootRun
```

도커 없이(회사 환경) 실행:
```bash
./bootrun nodocker
```

## 사내 망(외부 차단) 실행 가이드
- 권장 모드: `./bootrun nodocker`
- 결제 Mock 유지: `.env.nodocker`에서 `KICC_USE_MOCK_APPROVE=true`
- DB는 사내망 DB로 연결:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
- Redis는 선택(없어도 fallback 동작)이나 운영 안정성 측면에서 권장
- 외부 차단 시 `https://pg.kicc.co.kr` 실접속/실승인 테스트는 불가
- 최초 빌드 의존성 대응 필요:
  - `gradle`, `npm` 의존성을 사내 Nexus/Artifactory로 연결하거나
  - 사전에 캐시/산출물(JAR, node_modules 등) 반입

## 빠른 실행
루트에서:
```bash
./bootrun docker
```
새 터미널에서:
```bash
./run
```

- `bootrun docker`는 `.env.docker`를 로드한 뒤 backend의 `./gradlew bootRun` 실행
- `bootrun nodocker`는 `.env.nodocker`를 로드한 뒤 backend의 `./gradlew bootRun` 실행
- `docker` 모드에서는 bootRun 시 `backend/compose.yaml`로 MariaDB + Redis 자동 기동
- `bootrun`은 `ADMIN_TOKEN` 미설정 시 개발용 토큰을 자동 생성해 출력

## 기본 계정/설정
- DB 사용자: `root`
- DB 비밀번호: `als@2586`
- DB 이름: `kicc_demo`
- Redis 사용자: `redis`
- Redis 비밀번호: `redis2586!`
- 관리자 토큰: `ADMIN_TOKEN` 필수(운영은 16자 이상 고정값 권장)

## 구현된 기능 (실승인 없이 테스트 가능)
- 결제 준비/승인/상태조회
- 키오스크 연동 데모(세션 생성/결제요청/단말 승인/단말 실패)
- 전체취소, 부분환불, 환불이력 조회
- 주문 상태머신 (`PAYMENT_PENDING/PAID/PARTIALLY_REFUNDED/REFUNDED/CANCELED/FAILED`)
- 멱등키 처리 (`Idempotency-Key`)
- Redis 선점(NX) + 결과 캐시 기반 중복체크
- 콜백 서명 검증 옵션 (`X-KICC-SIGNATURE`)
- 관리자 API (결제/주문/감사로그/수동 보정)
- 자동 보정 스케줄러 (오래된 READY 결제 자동 실패 처리 등)
- Actuator (`/actuator/health`, `/actuator/metrics`)

## 주요 API
### 결제 API
- `POST /api/payments/ready`
- `POST /api/payments/{orderId}/mock-approve` (헤더 `X-Admin-Token` 필요)
- `POST /api/payments/{orderId}/mock-cancel` (헤더 `X-Admin-Token` 필요)
- `POST /api/payments/{orderId}/mock-refund` (헤더 `X-Admin-Token` 필요)
- `GET /api/payments/{orderId}`
- `GET /api/payments/{orderId}/refund-history`
- `POST /api/payments/kicc/callback`

### 관리자 API (헤더 `X-Admin-Token` 필요)
- `GET /api/admin/payments`
- `GET /api/admin/orders`
- `GET /api/admin/audit-logs`
- `POST /api/admin/reconcile-now`

### 키오스크 데모 API
- `POST /api/kiosk/sessions`
- `POST /api/kiosk/sessions/{sessionId}/request-payment`
- `POST /api/kiosk/sessions/{sessionId}/terminal-approve`
- `POST /api/kiosk/sessions/{sessionId}/terminal-fail`
- `GET /api/kiosk/sessions/{sessionId}`
- `GET /api/kiosk/sessions`

## 환경변수
- `ADMIN_TOKEN`
- `ALLOW_GENERATED_ADMIN_TOKEN`
- `KICC_MERCHANT_ID`
- `KICC_MERCHANT_KEY`
- `KICC_PAY_URL`
- `KICC_USE_MOCK_APPROVE`
- `KICC_CALLBACK_SIGNATURE_REQUIRED`
  - 기본값: `true`
- `BACKEND_URL`
- `APP_TEST_HISTORY_SEED_ENABLED`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_USERNAME`
- `REDIS_PASSWORD`

루트 경로의 `.env.docker`, `.env.nodocker` 파일을 모드에 맞게 `bootrun`이 자동 로드합니다.

## 실연동 시 교체 포인트
- `KiccPayloadFactory`: KICC 실제 필드/서명 규격 반영
- `PaymentController#callback`: 실제 콜백 필드/서명 규격 반영
- 승인/취소/환불의 실제 PG API 호출 로직 추가

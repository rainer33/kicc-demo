# KICC 결제 데모 (Spring Boot + Gradle + Vue + MariaDB)

## 빠른 실행
루트에서:
```bash
bootrun
```
새 터미널에서:
```bash
run
```

- `bootrun`은 backend의 `./gradlew bootRun` 실행
- bootRun 시 `backend/compose.yaml`로 MariaDB 자동 기동
- `bootrun`은 `ADMIN_TOKEN` 미설정 시 개발용 토큰을 자동 생성해 출력

## 기본 계정/설정
- DB 사용자: `root`
- DB 비밀번호: `als@2586`
- DB 이름: `kicc_demo`
- 관리자 토큰: `ADMIN_TOKEN` 필수 (16자 이상)

## 구현된 기능 (실승인 없이 테스트 가능)
- 결제 준비/승인/상태조회
- 전체취소, 부분환불, 환불이력 조회
- 주문 상태머신 (`PAYMENT_PENDING/PAID/PARTIALLY_REFUNDED/REFUNDED/CANCELED/FAILED`)
- 멱등키 처리 (`Idempotency-Key`)
- 콜백 서명 검증 옵션 (`X-KICC-SIGNATURE`)
- 관리자 API (결제/주문/감사로그/수동 보정)
- 자동 보정 스케줄러 (오래된 READY 결제 자동 실패 처리 등)
- Actuator (`/actuator/health`, `/actuator/metrics`)

## 주요 API
### 결제 API
- `POST /api/payments/ready`
- `POST /api/payments/{orderId}/mock-approve`
- `POST /api/payments/{orderId}/mock-cancel`
- `POST /api/payments/{orderId}/mock-refund`
- `GET /api/payments/{orderId}`
- `GET /api/payments/{orderId}/refund-history`
- `POST /api/payments/kicc/callback`

### 관리자 API (헤더 `X-Admin-Token` 필요)
- `GET /api/admin/payments`
- `GET /api/admin/orders`
- `GET /api/admin/audit-logs`
- `POST /api/admin/reconcile-now`

## 환경변수
- `ADMIN_TOKEN`
- `KICC_MERCHANT_ID`
- `KICC_MERCHANT_KEY`
- `KICC_PAY_URL`
- `KICC_USE_MOCK_APPROVE`
- `KICC_CALLBACK_SIGNATURE_REQUIRED`
  - 기본값: `true`

## 실연동 시 교체 포인트
- `KiccPayloadFactory`: KICC 실제 필드/서명 규격 반영
- `PaymentController#callback`: 실제 콜백 필드/서명 규격 반영
- 승인/취소/환불의 실제 PG API 호출 로직 추가

# 결제 데모 실프로젝트 적용 가이드

## 1) 적용 대상 범위
아래 기능을 실프로젝트에 이식합니다.
- Redis 기반 중복결제 방지(NX 락 + 결과 캐시)
- 테스트 이력관리(목록/상세/등록 API + 화면)
- 키오스크 결제 데모(세션 생성/결제요청/단말 승인·실패)
- 개발환경 `ADMIN_TOKEN` 자동 생성(운영에서는 비활성화 권장)

## 2) 권장 이식 방법
- 안전한 방법: 기능 커밋만 `git cherry-pick`으로 순차 반영
- 반영 순서(의존성 기준): Redis -> 테스트 이력 -> 토큰 편의 -> 키오스크

## 3) 체리픽 대상 커밋
- `817226f` Redis 컨테이너 및 ACL 설정 추가
- `103b46d` Spring Redis 의존성과 인증 연결 설정 추가
- `3df6fe8` 결제 ready Redis 중복체크 및 멱등 처리 강화
- `00bf0ae` README에 Redis 실행 및 중복체크 설정 반영
- `f8eff93` 테스트 이력관리 백엔드 도메인과 관리자 API 추가
- `5498dbd` 테스트 이력 목록·상세 화면 및 라우팅 구조 추가
- `276032e` 멱등성 테스트 키 충돌 방지를 위한 회귀 테스트 보정
- `909c0fc` 개발환경 ADMIN_TOKEN 자동 생성 및 부팅 허용 추가
- `d830897` 키오스크 결제 세션 도메인과 연동 API 데모 추가
- `5647470` 키오스크 데모 화면과 라우팅 및 문서 안내 추가

## 4) 필수 환경변수
- DB: `MARIADB_ROOT_PASSWORD`, `MARIADB_DATABASE`
- 관리자: `ADMIN_TOKEN` (운영 필수, 16자 이상)
- KICC: `KICC_MERCHANT_ID`, `KICC_MERCHANT_KEY`, `KICC_PAY_URL`, `KICC_USE_MOCK_APPROVE`, `KICC_CALLBACK_SIGNATURE_REQUIRED`
- Redis: `REDIS_HOST`, `REDIS_PORT`, `REDIS_USERNAME`, `REDIS_PASSWORD`
- 개발 편의: `ALLOW_GENERATED_ADMIN_TOKEN`

## 5) 운영 반영 시 주의사항
- `KICC_USE_MOCK_APPROVE=false`
- `KICC_CALLBACK_SIGNATURE_REQUIRED=true` 유지
- `ALLOW_GENERATED_ADMIN_TOKEN=false`
- `ADMIN_TOKEN`은 시크릿 매니저/배포변수로 주입
- DB/Redis root 계정 하드코딩 금지, 운영 전용 계정 사용

## 6) 반영 후 검증 체크리스트
- 백엔드: `./gradlew test`
- 프론트: `npm run build`
- API 점검:
  - `/api/payments/ready` + 동일 `Idempotency-Key` 재요청
  - `/api/admin/test-histories`
  - `/api/kiosk/sessions` -> `/request-payment` -> `/terminal-approve`

## 7) 문제 발생 시 롤백
- 체리픽 커밋 단위로 `git revert <commit>` 수행
- 기능 단위 롤백 권장(예: 키오스크만 먼저 롤백)

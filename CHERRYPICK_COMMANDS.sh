#!/usr/bin/env bash
set -euo pipefail

# 사용법:
# 1) 실프로젝트 저장소 루트에서 실행
# 2) DEMO_REMOTE를 데모 저장소 URL로 설정
# 3) DEMO_BRANCH를 데모 브랜치로 설정(기본: main)
#
# 예)
# DEMO_REMOTE=https://github.com/rainer33/kicc-demo.git DEMO_BRANCH=main ./CHERRYPICK_COMMANDS.sh

DEMO_REMOTE="${DEMO_REMOTE:-https://github.com/rainer33/kicc-demo.git}"
DEMO_BRANCH="${DEMO_BRANCH:-main}"

echo "[1/4] remote 준비"
if ! git remote get-url kicc-demo >/dev/null 2>&1; then
  git remote add kicc-demo "$DEMO_REMOTE"
fi

echo "[2/4] 최신 커밋 fetch"
git fetch kicc-demo "$DEMO_BRANCH"

echo "[3/4] 기능 커밋 cherry-pick"
# Redis
git cherry-pick 817226f 103b46d 3df6fe8 00bf0ae
# 테스트 이력관리
git cherry-pick f8eff93 5498dbd 276032e
# 개발편의(운영에서 비활성화 권장)
git cherry-pick 909c0fc
# 키오스크 데모
git cherry-pick d830897 5647470

echo "[4/4] 적용 완료. 충돌 없으면 빌드/테스트를 실행하세요."
echo "  backend: ./gradlew test"
echo "  frontend: npm run build"

#!/bin/bash
# ============================================================
# deploy.sh: 로컬에서 실행 - 이미지 빌드 후 GCE에 배포
# 사전 준비: gcloud CLI 설치 및 로그인 (gcloud auth login)
# ============================================================
set -e

# ── 설정값 (환경에 맞게 수정) ──────────────────────────────
PROJECT_ID="dreamtoon-487717"
REGION="asia-northeast3"
REPO="dreamtoon"
IMAGE_NAME="dreamtoon-be"
GCE_INSTANCE="dreamtoon-backend"
GCE_ZONE="asia-northeast3-a"
# ────────────────────────────────────────────────────────────

IMAGE_TAG="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}/${IMAGE_NAME}:latest"

echo "▶ [1/5] Artifact Registry 저장소 확인/생성"
gcloud artifacts repositories describe ${REPO} \
    --location=${REGION} \
    --project=${PROJECT_ID} > /dev/null 2>&1 || \
gcloud artifacts repositories create ${REPO} \
    --repository-format=docker \
    --location=${REGION} \
    --project=${PROJECT_ID} \
    --description="Dreamtoon Docker images"

echo "▶ [2/5] Docker 이미지 빌드"
docker build -t ${IMAGE_TAG} .

echo "▶ [3/5] Artifact Registry 인증"
gcloud auth configure-docker ${REGION}-docker.pkg.dev --quiet

echo "▶ [4/5] 이미지 푸시"
docker push ${IMAGE_TAG}

echo "▶ [5/5] GCE 배포"
gcloud compute ssh ${GCE_INSTANCE} \
    --zone=${GCE_ZONE} \
    --project=${PROJECT_ID} \
    --command="
        cd /opt/dreamtoon &&
        gcloud auth configure-docker ${REGION}-docker.pkg.dev --quiet &&
        docker pull ${IMAGE_TAG} &&
        docker compose -f docker-compose.prod.yml up -d --force-recreate app &&
        docker image prune -f
    "

echo "✅ 배포 완료!"
echo "   앱 주소: http://\$(gcloud compute instances describe ${GCE_INSTANCE} --zone=${GCE_ZONE} --format='get(networkInterfaces[0].accessConfigs[0].natIP)'):8080"

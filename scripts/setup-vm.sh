#!/bin/bash
# ============================================================
# setup-vm.sh: GCE VM 최초 1회 실행 (SSH 접속 후 실행)
# gcloud compute ssh YOUR_INSTANCE --zone=asia-northeast3-a
# ============================================================
set -e

echo "▶ [1/4] Docker 설치"
sudo apt-get update -y
sudo apt-get install -y ca-certificates curl gnupg

sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
    sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker $USER

echo "▶ [2/4] gcloud CLI 설치 (apt, 시스템 전역)"
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | \
    sudo gpg --dearmor -o /usr/share/keyrings/cloud.google.gpg
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | \
    sudo tee /etc/apt/sources.list.d/google-cloud-sdk.list
sudo apt-get update -y
sudo apt-get install -y google-cloud-cli

echo "▶ [3/4] 앱 디렉토리 생성"
sudo mkdir -p /opt/dreamtoon
sudo chown $USER:$USER /opt/dreamtoon

echo "▶ [4/4] Cloud SQL Auth Proxy용 서비스 계정 권한 확인"
echo "   GCE 인스턴스에 다음 역할이 부여되어 있어야 합니다:"
echo "   - Cloud SQL 클라이언트 (roles/cloudsql.client)"
echo "   - Artifact Registry 읽기 (roles/artifactregistry.reader)"
echo "   - Storage 객체 관리자 (roles/storage.objectAdmin)"
echo ""
echo "✅ VM 초기 설정 완료!"
echo ""
echo "다음 단계:"
echo "1. 새 터미널 세션 시작 (docker 그룹 적용)"
echo "2. /opt/dreamtoon/.env.prod 파일 생성 (.env.prod.example 참고)"
echo "3. /opt/dreamtoon/docker-compose.prod.yml 업로드"
echo "   gcloud compute scp docker-compose.prod.yml YOUR_INSTANCE:/opt/dreamtoon/ --zone=asia-northeast3-a"
echo "4. 로컬에서 ./scripts/deploy.sh 실행"

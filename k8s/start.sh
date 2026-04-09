#!/bin/bash
set -e

CLUSTER_NAME="backend"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=========================================="
echo " K8s 클러스터 시작"
echo "=========================================="

# 1. kind 클러스터 생성 (없을 때만)
echo ""
echo "=== [1/8] kind 클러스터 확인/생성 ==="
if kind get clusters | grep -q "^${CLUSTER_NAME}$"; then
  echo "클러스터 '${CLUSTER_NAME}' 이미 존재 - 생성 건너뜀"
  kubectl config use-context "kind-${CLUSTER_NAME}"
else
  kind create cluster --config "$SCRIPT_DIR/common/kind-config.yaml" --name $CLUSTER_NAME
fi

# 2. host.docker.internal CoreDNS 패치
echo ""
echo "=== [2/8] CoreDNS host.docker.internal 패치 ==="
HOST_IP=$(docker run --rm --network kind alpine sh -c \
  "getent hosts host.docker.internal 2>/dev/null | awk '{print \$1}'" 2>/dev/null || true)

if [ -z "$HOST_IP" ]; then
  HOST_IP=$(docker network inspect kind --format='{{(index .IPAM.Config 0).Gateway}}')
fi
echo "Host IP: $HOST_IP"

kubectl get configmap coredns -n kube-system -o json | python3 -c "
import sys, json
host_ip = sys.argv[1]
cm = json.load(sys.stdin)
hosts_block = '    hosts {\n      ' + host_ip + ' host.docker.internal\n      fallthrough\n    }\n'
cm['data']['Corefile'] = cm['data']['Corefile'].replace('    ready', hosts_block + '    ready')
print(json.dumps(cm))
" "$HOST_IP" | kubectl apply -f -

kubectl rollout restart deployment coredns -n kube-system
kubectl rollout status deployment coredns -n kube-system --timeout=60s

# 3. NGINX Ingress Controller 설치
echo ""
echo "=== [3/8] NGINX Ingress Controller 설치 ==="
kubectl apply -f "$SCRIPT_DIR/nginx-ingress/deploy.yaml"
echo "Ingress Controller 준비 대기 중..."
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

# 4. 공통 리소스 적용
echo ""
echo "=== [4/8] 공통 리소스 적용 ==="
kubectl apply -f "$SCRIPT_DIR/common/namespace.yaml"
kubectl apply -f "$SCRIPT_DIR/common/secret.yaml"

# 5. Gradle 빌드
echo ""
echo "=== [5/8] Gradle 빌드 ==="
cd "$ROOT_DIR"
./gradlew :gateway:bootJar :user:bootJar :chapter:bootJar :problem:bootJar :flyway:bootJar

# 6. Docker 이미지 빌드 및 kind 로드
echo ""
echo "=== [6/8] Docker 이미지 빌드 및 kind 로드 ==="
docker build -t gateway:latest ./gateway
docker build -t user_manage:latest ./user
docker build -t chapter:latest ./chapter
docker build -t problem:latest ./problem
docker build -t flyway:latest ./flyway

kind load docker-image gateway:latest --name $CLUSTER_NAME
kind load docker-image user_manage:latest --name $CLUSTER_NAME
kind load docker-image chapter:latest --name $CLUSTER_NAME
kind load docker-image problem:latest --name $CLUSTER_NAME
kind load docker-image flyway:latest --name $CLUSTER_NAME

# 7. DB 마이그레이션
echo ""
echo "=== [7/8] DB 마이그레이션 (Flyway) ==="
kubectl apply -f "$SCRIPT_DIR/flyway/job.yaml"
echo "마이그레이션 완료 대기 중..."
kubectl wait --for=condition=complete job/flyway-migration -n backend --timeout=120s

# 8. 서비스 배포
echo ""
echo "=== [8/8] 서비스 배포 ==="
kubectl apply -f "$SCRIPT_DIR/gateway/"
kubectl apply -f "$SCRIPT_DIR/user/"
kubectl apply -f "$SCRIPT_DIR/chapter/"
kubectl apply -f "$SCRIPT_DIR/problem/"
kubectl apply -f "$SCRIPT_DIR/nginx-ingress/ingress.yaml"

echo ""
echo "=========================================="
echo " 완료!"
echo "=========================================="
echo ""
echo "/etc/hosts에 아래 항목이 없으면 추가하세요:"
echo "  127.0.0.1 api.local"
echo ""
echo "접근 URL: http://api.local"
kubectl get pods -n backend

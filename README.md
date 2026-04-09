# K8s 학습 프로젝트

# 로컬 Docker Compose → AWS EKS + CI/CD

## 프로젝트 개요

| 항목 | 내용 |
| --- | --- |
| 참여 인원 | 백엔드 엔지니어 1명, 클라우드 엔지니어 1명 |
| 시작 상태 | 백엔드 서버 개발 완료, Docker Compose 로컬 구동 완료 |
| 최종 목표 | AWS EKS 배포 + CI/CD 파이프라인 + 오토스케일링 운영 환경 구축 |
| 예상 기간 | 5~7주 |

---

## 기술 스택 요약

- **컨테이너**: Docker, Kubernetes (Minikube/kind → EKS)
- **패키징**: Helm
- **클라우드**: AWS (EKS, ECR, ALB, RDS, VPC, IAM)
- **IaC**: Terraform (또는 eksctl)
- **CI/CD**: GitHub Actions + ArgoCD
- **모니터링**: Prometheus + Grafana
- **부하테스트**: k6 또는 wrk

---

## Phase 1: 로컬 K8s 전환 (1~2주)

> **목표**: Docker Compose로 구동 중인 서비스를 로컬 K8s 클러스터에서 동일하게 구동한다.
> 

### 백엔드 엔지니어

- [ ]  Dockerfile 최적화
    - 멀티스테이지 빌드 적용
    - 불필요한 레이어 제거, 이미지 사이즈 축소
    - `.dockerignore` 정리
- [ ]  K8s manifest 작성
    - Deployment (replicas, resource limits/requests)
    - Service (ClusterIP)
    - ConfigMap (환경변수 분리)
    - Secret (DB 비밀번호 등 민감정보 분리)
- [ ]  health check 엔드포인트 구현
    - `GET /healthz` → liveness probe용
    - `GET /readyz` → readiness probe용
- [ ]  Deployment에 probe 설정 추가
    
    ```yaml
    livenessProbe:  httpGet:    path: /healthz    port: 8080  initialDelaySeconds: 10  periodSeconds: 5readinessProbe:  httpGet:    path: /readyz    port: 8080  initialDelaySeconds: 5  periodSeconds: 3
    ```
    

### 클라우드 엔지니어

- [ ]  로컬 K8s 클러스터 세팅 (Minikube 또는 kind)
- [ ]  Namespace 설계 및 생성 (예: `dev`, `staging`)
- [ ]  Ingress Controller 설치 (NGINX Ingress Controller)
- [ ]  Ingress 리소스 작성 및 라우팅 설정
- [ ]  DB를 K8s 내에서 구동
    - StatefulSet + PVC 구성
    - 또는 로컬 외부 DB 유지 후 ExternalName Service 연결

### 완료 기준

- [ ]  `kubectl apply -f` 로 전체 서비스가 정상 기동
- [ ]  Ingress를 통해 외부에서 API 호출 가능
- [ ]  `kubectl get pods` 에서 모든 Pod가 Running + Ready 상태
- [ ]  Pod 삭제 시 자동 재생성 확인

---

## Phase 2: Helm Chart 패키징 (1주)

> **목표**: Phase 1의 manifest를 Helm Chart로 패키징하여 환경별 배포가 가능하도록 한다.
> 

### 백엔드 엔지니어

- [ ]  `values.yaml` 설계
    - 파라미터화 대상: 이미지 태그, 레플리카 수, 리소스 제한, 환경변수
    - 예시:
        
        ```yaml
        image:  repository: my-backend  tag: latestreplicas: 2resources:  requests:    cpu: 100m    memory: 128Mi  limits:    cpu: 500m    memory: 512Mienv:  DB_HOST: localhost  DB_PORT: "5432"
        ```
        
- [ ]  환경별 values 파일 분리
    - `values-dev.yaml`
    - `values-staging.yaml`
    - `values-prod.yaml`

### 클라우드 엔지니어

- [ ]  Helm Chart 디렉토리 구조 생성
    
    ```
    chart/├── Chart.yaml├── values.yaml├── values-dev.yaml├── values-staging.yaml├── values-prod.yaml└── templates/    ├── deployment.yaml    ├── service.yaml    ├── configmap.yaml    ├── secret.yaml    ├── ingress.yaml    └── _helpers.tpl
    ```
    
- [ ]  `_helpers.tpl` 에 공통 라벨, 이름 규칙 정의
- [ ]  Helm 명령어 워크플로우 검증
    - `helm install` → 신규 배포
    - `helm upgrade` → 변경 배포
    - `helm rollback` → 롤백
    - `helm template` → manifest 렌더링 확인 (dry-run)

### 완료 기준

- [ ]  `helm install my-app ./chart -f values-dev.yaml` 로 한 번에 배포 가능
- [ ]  values 파일만 교체하여 환경 전환 확인
- [ ]  `helm upgrade` 후 변경사항 반영 확인
- [ ]  `helm rollback` 으로 이전 버전 복원 확인

---

## Phase 3: AWS 인프라 + EKS 구축 (2주)

> **목표**: AWS에 EKS 클러스터를 구축하고, 백엔드 서비스를 실제 클라우드에 배포한다.
> 

### 백엔드 엔지니어

- [ ]  ECR 레포지토리 생성 및 이미지 push
    
    ```bash
    aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.ap-northeast-2.amazonaws.comdocker tag my-backend:latest <account-id>.dkr.ecr.ap-northeast-2.amazonaws.com/my-backend:v1.0.0docker push <account-id>.dkr.ecr.ap-northeast-2.amazonaws.com/my-backend:v1.0.0
    ```
    
- [ ]  AWS 환경용 `values-prod.yaml` 작성
    - ECR 이미지 경로로 변경
    - RDS 엔드포인트 반영
    - 리소스 제한 조정
- [ ]  RDS 연동을 위한 앱 설정 변경
    - DB 커넥션 풀 설정
    - 타임아웃, 재시도 로직 점검

### 클라우드 엔지니어

- [ ]  Terraform으로 AWS 인프라 프로비저닝
    - VPC (Public/Private Subnet, NAT Gateway)
    - EKS 클러스터 + Node Group
    - RDS (Private Subnet 배치)
    - Security Group 설계
        - ALB → Node: 앱 포트 허용
        - Node → RDS: DB 포트 허용
- [ ]  IAM 설정
    - EKS 클러스터 역할
    - Node Group 역할
    - IRSA (ServiceAccount ↔ IAM Role) 설정
- [ ]  AWS Load Balancer Controller 설치
- [ ]  Ingress 리소스에 ALB 어노테이션 추가
    
    ```yaml
    annotations:  kubernetes.io/ingress.class: alb  alb.ingress.kubernetes.io/scheme: internet-facing  alb.ingress.kubernetes.io/target-type: ip
    ```
    
- [ ]  kubeconfig 설정 및 팀원과 클러스터 접근 공유

### 완료 기준

- [ ]  EKS 클러스터에 Helm으로 서비스 배포 완료
- [ ]  ALB DNS로 외부 API 호출 정상 동작
- [ ]  앱 → RDS 연동 정상 동작 (CRUD 확인)
- [ ]  `kubectl logs` 로 앱 로그 정상 확인

---

## Phase 4: CI/CD 파이프라인 (1~2주)

> **목표**: 코드 push만으로 빌드 → 이미지 push → EKS 자동 배포까지 완성한다.
> 

### 백엔드 엔지니어

- [ ]  GitHub Actions CI 워크플로우 작성
    
    ```yaml
    # .github/workflows/ci.yamlname: CIon:  push:    branches: [main, develop]  pull_request:    branches: [main]jobs:  test:    runs-on: ubuntu-latest    steps:      - uses: actions/checkout@v4      - name: Run tests        run: |          # 유닛 테스트 + 통합 테스트  build-and-push:    needs: test    runs-on: ubuntu-latest    steps:      - uses: actions/checkout@v4      - uses: aws-actions/configure-aws-credentials@v4      - uses: aws-actions/amazon-ecr-login@v2      - name: Build and push        run: |          docker build -t $ECR_REPO:${{ github.sha }} .          docker push $ECR_REPO:${{ github.sha }}      - name: Update Helm values        run: |          # values 파일의 image tag를 commit SHA로 업데이트          # GitOps 레포에 커밋
    ```
    
- [ ]  브랜치 전략 정의
    - `feature/*` → develop (PR 머지)
    - `develop` → staging 자동 배포
    - `main` → prod 자동 배포
- [ ]  테스트 자동화 (CI에서 실행될 테스트 코드 정비)

### 클라우드 엔지니어

- [ ]  ArgoCD 설치 (EKS 클러스터 내)
    
    ```bash
    kubectl create namespace argocdkubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
    ```
    
- [ ]  ArgoCD Application 리소스 생성
    - Git 레포 (Helm Chart) 연동
    - 자동 sync 정책 설정
    
    ```yaml
    apiVersion: argoproj.io/v1alpha1
    kind: Application
    metadata:
      name: my-backend
      namespace: argocd
    spec:
      source:
        repoURL: https://github.com/<org>/<gitops-repo>.git
        path: chart
        helm:
          valueFiles:
            - values-prod.yaml
      destination:
        server: https://kubernetes.default.svc
        namespace: production
      syncPolicy:
        automated:
          prune: true
          selfHeal: true
    ```
    
- [ ]  배포 전략 설정
    - Rolling Update (기본): maxSurge, maxUnavailable 설정
    - 또는 Blue/Green 전략 적용
- [ ]  ArgoCD 대시보드 접근 설정 (포트포워딩 또는 Ingress)

### 완료 기준

- [ ]  develop 브랜치에 push → staging 자동 배포
- [ ]  main 브랜치에 push → prod 자동 배포
- [ ]  ArgoCD 대시보드에서 sync 상태 확인 가능
- [ ]  잘못된 배포 시 `helm rollback` 또는 ArgoCD에서 롤백 가능

---

## Phase 5: 운영 안정화 (1주)

> **목표**: 모니터링, 오토스케일링, 장애 대응을 구축하여 운영 수준으로 끌어올린다.
> 

### 백엔드 엔지니어

- [ ]  HPA 설정
    
    ```yaml
    apiVersion: autoscaling/v2kind: HorizontalPodAutoscalermetadata:  name: my-backend-hpaspec:  scaleTargetRef:    apiVersion: apps/v1    kind: Deployment    name: my-backend  minReplicas: 2  maxReplicas: 10  metrics:    - type: Resource      resource:        name: cpu        target:          type: Utilization          averageUtilization: 70
    ```
    
- [ ]  부하 테스트 수행 (k6 또는 wrk)
    - HPA에 의해 Pod가 스케일아웃되는지 확인
    - 트래픽 줄이면 스케일인되는지 확인
- [ ]  Prometheus 메트릭 노출 (앱에 `/metrics` 엔드포인트 추가)

### 클라우드 엔지니어

- [ ]  Prometheus + Grafana 설치 (Helm chart: kube-prometheus-stack)
    
    ```bash
    helm repo add prometheus-community https://prometheus-community.github.io/helm-chartshelm install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace
    ```
    
- [ ]  Grafana 대시보드 구성
    - K8s 클러스터 리소스 대시보드
    - 앱 메트릭 대시보드
- [ ]  Karpenter 설정 (노드 오토스케일링)
    - Pod가 Pending 상태 시 자동 노드 추가
    - 트래픽 감소 시 노드 자동 축소
- [ ]  장애 시나리오 테스트
    - Pod 강제 삭제 → 자동 재생성 확인
    - 노드 drain → Pod 재배치 확인
    - 잘못된 이미지 배포 → 롤백 수행

### 완료 기준

- [ ]  부하 테스트 시 Pod 오토스케일링 정상 동작
- [ ]  Grafana에서 CPU, 메모리, 요청 수 실시간 모니터링 가능
- [ ]  장애 시나리오 3개 이상 테스트 및 복구 확인

---

## 트래픽 흐름 아키텍처

```
[Client]
   │
   ▼
[AWS ALB] ── AWS가 자동 관리
   │
   ▼
[K8s Ingress]
   │
   ▼
[K8s Service] ── Pod 간 로드밸런싱 (라운드로빈)
   │
   ├──▶ [Pod 1]
   ├──▶ [Pod 2]
   └──▶ [Pod 3]  ← HPA가 트래픽에 따라 자동 증감
          │
          ▼
       [AWS RDS]
```

## 스케일아웃 흐름

```
트래픽 증가
   → HPA: Pod 수 증가 (Pod 레벨)
   → Karpenter: EC2 노드 추가 (노드 레벨)
   → ALB: 새 Pod 자동 등록 (로드밸런서 레벨)

트래픽 감소
   → HPA: Pod 수 감소
   → Karpenter: 빈 노드 회수
```

---

## 참고 자료

- [Kubernetes 공식 문서](https://kubernetes.io/ko/docs/home/)
- [Helm 공식 문서](https://helm.sh/docs/)
- [AWS EKS 공식 가이드](https://docs.aws.amazon.com/eks/latest/userguide/)
- [ArgoCD 공식 문서](https://argo-cd.readthedocs.io/)
- [Terraform EKS 모듈](https://registry.terraform.io/modules/terraform-aws-modules/eks/aws/latest)
- [kube-prometheus-stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack)
- [Karpenter 공식 문서](https://karpenter.sh/docs/)

# allimio 배포 가이드 (Docker + minikube + GitHub Actions)

## 1. 구조

```
개발자 PC ── git push(main) ──▶ GitHub
                                  │  GitHub Actions
                                  │   ① Docker 이미지 빌드 (Java: Gradle bootWar / React: Vite build)
                                  │   ② GHCR(ghcr.io/eong98/...)에 업로드 (태그 = 커밋 SHA)
                                  │   ③ SSH로 서버 접속 → kubectl apply → 롤아웃 확인 (실패 시 자동 되돌림)
                                  ▼
서버 1.201.122.5 (Ubuntu 22.04)
  ├─ 호스트 nginx
  │    :80   → 유형숲 (별도 설정, 건드리지 않음)
  │    :5174 → 192.168.49.2:30174 (allimio-web NodePort)
  │    :9102 → 192.168.49.2:30902 (allimio-api NodePort)
  ├─ systemd allimio-mount : /home/ubuntu/deploy/allimio → minikube 노드 같은 경로 (업로드 파일)
  └─ minikube (docker driver, 노드 IP 192.168.49.2)
       └─ namespace allimio
            ├─ allimio-web (nginx + React dist)
            └─ allimio-api (Spring Boot, hostPath /home/ubuntu/deploy/allimio)

외부: Oracle 1.201.122.84:1521 / Redis 1.201.122.84:6379 / FastAPI(H200) 139.150.91.194:11200
```

| 저장소 | 이미지 | 매니페스트 |
|---|---|---|
| team2_jpa_v1 | ghcr.io/eong98/allimio-api | k8s/api.yaml (+ nginx-allimio.conf, allimio-mount.service) |
| team2_react_v1 | ghcr.io/eong98/allimio-web | k8s/web.yaml |

## 2. 서버 최초 설정 (1회)

### 2-1. 사전 확인
```bash
minikube status                              # host/kubelet/apiserver Running
which minikube                               # /usr/local/bin/minikube (다르면 allimio-mount.service 경로 수정)
kubectl get svc -A | grep -E '30902|30174'   # 아무것도 안 나와야 NodePort 사용 가능
kubectl describe node minikube | grep -A6 Allocatable   # memory가 2Gi 이상인지
```

### 2-2. 업로드 폴더 연결 (minikube mount 상시 실행)
```bash
sudo cp ~/allimio/team2_jpa_v1/k8s/allimio-mount.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now allimio-mount
systemctl status allimio-mount --no-pager | tail -5
minikube ssh -- ls /home/ubuntu/deploy/allimio    # attach profile home board 보이면 OK
```

### 2-3. 네임스페이스 / 시크릿
```bash
kubectl create namespace allimio
kubectl -n allimio create secret generic allimio-env --from-env-file=$HOME/allimio/allimio.env
# GHCR 이미지 받기용 (GitHub PAT classic, 권한 read:packages)
kubectl -n allimio create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io --docker-username=eong98 --docker-password='<PAT>'
```

### 2-4. GitHub Actions용 SSH 키
```bash
ssh-keygen -t ed25519 -f ~/.ssh/github_deploy -N "" -C github-actions
cat ~/.ssh/github_deploy.pub >> ~/.ssh/authorized_keys
cat ~/.ssh/github_deploy          # 출력 전체를 GitHub Secret SERVER_SSH_KEY에 등록
```

### 2-5. GitHub Secrets (두 저장소 모두)
Settings → Secrets and variables → Actions

| 이름 | 값 |
|---|---|
| SERVER_HOST | 1.201.122.5 |
| SERVER_USER | ubuntu |
| SERVER_SSH_KEY | `~/.ssh/github_deploy` 내용 전체 |

### 2-6. push → 첫 배포 확인 → 트래픽 전환
```bash
kubectl -n allimio get pods,svc      # allimio-api, allimio-web Running / NodePort 30902, 30174
curl -I http://192.168.49.2:30902/swagger-ui/index.html   # 200
curl -I http://192.168.49.2:30174                         # 200
```
확인되면 기존 Docker/정적 nginx → minikube로 전환:
```bash
sudo docker rm -f allimio-api                              # 9102 비우기
cd ~/allimio/team2_jpa_v1 && git pull
sudo cp k8s/nginx-allimio.conf /etc/nginx/sites-available/allimio
sudo nginx -t && sudo systemctl reload nginx               # test is successful 일 때만 reload
curl -I http://127.0.0.1:9102/swagger-ui/index.html && curl -I http://127.0.0.1:5174
```

## 3. 평소 배포
`main`에 push → 자동 배포. GitHub **Actions** 탭에서 확인.
- 실패하면 이전 버전 유지(롤아웃 실패 시 자동 undo).
- 다시 배포: Actions → 워크플로 → **Run workflow** / **Re-run jobs**.

## 4. 운영 명령어
```bash
# 상태
kubectl -n allimio get pods,svc -o wide
kubectl -n allimio describe pod <pod이름>          # Events: 이미지 pull 실패, OOM, probe 실패
kubectl top pod -n allimio                        # (minikube addons enable metrics-server 필요)

# 로그
kubectl -n allimio logs deploy/allimio-api -f --tail=100
kubectl -n allimio logs deploy/allimio-api --previous   # 재시작 직전 로그

# 재시작 / 이력 / 되돌리기
kubectl -n allimio rollout restart deployment/allimio-api
kubectl -n allimio rollout history deployment/allimio-api
kubectl -n allimio rollout undo deployment/allimio-api
kubectl -n allimio rollout undo deployment/allimio-api --to-revision=3

# 컨테이너 안 들어가기
kubectl -n allimio exec -it deploy/allimio-api -- bash
```

### 환경변수(시크릿) 변경
```bash
vi ~/allimio/allimio.env
kubectl -n allimio create secret generic allimio-env --from-env-file=$HOME/allimio/allimio.env \
  --dry-run=client -o yaml | kubectl apply -f -
kubectl -n allimio rollout restart deployment/allimio-api
```

### 서버 재부팅 후
minikube는 자동으로 켜지지 않음.
```bash
minikube start                         # 기존 클러스터 그대로 다시 켬 (설정/Pod 유지)
sudo systemctl restart allimio-mount   # 업로드 폴더 재연결
kubectl -n allimio rollout restart deployment/allimio-api
kubectl -n allimio get pods
```

### 업로드 파일 백업
`tar czf ~/allimio-storage-$(date +%F).tgz -C /home/ubuntu/deploy allimio`

## 5. 문제 해결

| 증상 | 확인 | 원인/조치 |
|---|---|---|
| `ImagePullBackOff` | `describe pod` | ghcr-secret PAT 만료/권한 → 시크릿 재생성 |
| `CrashLoopBackOff` | `logs --previous` | 환경변수 누락(Could not resolve placeholder), DB/Redis 접속 실패 |
| api Pod `ContainerCreating`에서 멈춤, Events에 hostPath | `systemctl status allimio-mount` | 업로드 폴더 mount 끊김 → `sudo systemctl restart allimio-mount` |
| `OOMKilled` | `describe pod` | api.yaml `limits.memory` 증가 (minikube 노드 메모리 한도 내) |
| 브라우저 502 Bad Gateway | `curl 192.168.49.2:30902` | Pod 미기동 또는 minikube 정지(`minikube status`) |
| 첨부 업로드 `413` | | nginx `client_max_body_size` 확인 |
| Actions `Permission denied (publickey)` | | SERVER_SSH_KEY / authorized_keys |
| Actions `ssh ... timed out` | | 가비아 방화벽 22번이 특정 IP만 허용 → GitHub 러너 접속 불가 |

## 6. 주의
- main에 push하면 누구든 배포가 일어남 → main은 확인된 코드만.
- 업로드 폴더가 서버 1대의 폴더이므로 replicas 1 유지.
- `minikube delete`는 클러스터(시크릿, 배포 이력 포함)를 전부 지움 → 2-3부터 다시 해야 함. 업로드 파일(서버 폴더)은 남음.
- `src/utils/Tool.ts`, `vite.config.ts`는 .gitignore에 있지만 이미 git이 추적 중인 파일이라 push하면 배포에 반영됨.

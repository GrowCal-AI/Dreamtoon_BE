# 생성된 웹툰 확인 방법

## 📍 이미지 위치

웹툰 이미지가 다운로드되었습니다:
```
/tmp/dreamtoon_webtoons/webtoons/
```

Finder가 자동으로 열렸으니 4개의 PNG 이미지를 확인할 수 있습니다!

---

## 🎨 확인 방법

### 1. **Finder로 확인 (이미 열림)** ✅
```bash
open /tmp/dreamtoon_webtoons/webtoons/
```

4개의 이미지 파일:
- `20260217_212144_c5d345a6.png` (2.9MB)
- `20260217_212203_f425f77e.png` (1.8MB)
- `20260217_212222_9d947e72.png` (2.2MB)
- `20260217_212244_3eb189fa.png` (2.6MB)

### 2. **API로 URL 확인**
```bash
curl -s -X GET "http://localhost:8080/api/v1/dreams/3" \
  -H "Authorization: Bearer <TOKEN>" | jq '.data.webtoonImages'
```

**응답:**
```json
[
  "https://dreamtoon-dev-bucket.s3.ap-northeast-2.amazonaws.com/webtoon/20260217_212144_c5d345a6.png",
  "https://dreamtoon-dev-bucket.s3.ap-northeast-2.amazonaws.com/webtoon/20260217_212203_f425f77e.png",
  "https://dreamtoon-dev-bucket.s3.ap-northeast-2.amazonaws.com/webtoon/20260217_212222_9d947e72.png",
  "https://dreamtoon-dev-bucket.s3.ap-northeast-2.amazonaws.com/webtoon/20260217_212244_3eb189fa.png"
]
```

### 3. **DB에서 확인**
```bash
docker exec dreamtoon-postgres psql -U postgres -d dreamtoon_dev -c \
  "SELECT id, title, processing_status, webtoon_images FROM dreams WHERE id=3;"
```

---

## 🖼️ 이미지 정보

| 컷 | 파일명 | 크기 | 생성 시간 |
|---|--------|------|-----------|
| 1 | 20260217_212144_c5d345a6.png | 2.9MB | 21:21:45 |
| 2 | 20260217_212203_f425f77e.png | 1.8MB | 21:22:03 |
| 3 | 20260217_212222_9d947e72.png | 2.2MB | 21:22:22 |
| 4 | 20260217_212244_3eb189fa.png | 2.6MB | 21:22:44 |

**총 크기:** 9.6MB

---

## 🌐 프로덕션 환경에서는?

실제 GCP Storage를 사용하면 이미지 URL이 공개 URL이 되어 브라우저에서 바로 볼 수 있습니다:

```
https://storage.googleapis.com/dreamtoon-prod-bucket/webtoon/xxx.png
```

---

## 💡 팁

### 이미지를 프로젝트에 복사하기
```bash
cp /tmp/dreamtoon_webtoons/webtoons/*.png ~/Desktop/
```

### Preview로 한 번에 보기
```bash
open -a Preview /tmp/dreamtoon_webtoons/webtoons/*.png
```

### 이미지 정보 확인
```bash
file /tmp/dreamtoon_webtoons/webtoons/*.png
```

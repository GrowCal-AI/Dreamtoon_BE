#!/bin/bash

# Blueprint v2.0 E2E 테스트 스크립트
# 서버가 실행 중이어야 합니다 (./gradlew bootRun)

BASE_URL="http://localhost:8080/api/v1"
DREAM_ID=""
ACCESS_TOKEN=""

echo "🚀 Blueprint v2.0 E2E 테스트 시작"
echo "=================================="
echo ""

# Step 0: 테스트 로그인 (JWT 토큰 발급)
echo "🔐 Step 0: 테스트 로그인"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/test-login?userId=3")
echo "$LOGIN_RESPONSE" | jq '.'

ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken')

if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
  echo "❌ 실패: 토큰을 받지 못했습니다"
  exit 1
fi

echo "✅ 성공: 토큰 발급 완료"
echo "Token: ${ACCESS_TOKEN:0:50}..."
echo ""
sleep 1

# Step 1: 꿈 기록 시작
echo "📝 Step 1: 꿈 기록 시작"
RESPONSE=$(curl -s -X POST "$BASE_URL/dreams" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "dreamContent": "어젯밤 꿈에서 하늘을 날고 있었어요. 구름 위를 자유롭게 날아다니는데 너무 행복했습니다."
  }')

echo "$RESPONSE" | jq '.'
DREAM_ID=$(echo "$RESPONSE" | jq -r '.data.dreamId')

if [ "$DREAM_ID" == "null" ] || [ -z "$DREAM_ID" ]; then
  echo "❌ 실패: dreamId를 받지 못했습니다"
  exit 1
fi

echo "✅ 성공: dreamId = $DREAM_ID"
echo ""
sleep 1

# Step 2: 감정 선택
echo "😊 Step 2: 감정 선택 (JOY)"
curl -s -X PATCH "$BASE_URL/dreams/$DREAM_ID/emotion" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "primaryEmotion": "JOY"
  }' | jq '.'

echo "✅ 감정 선택 완료"
echo ""
sleep 1

# Step 3: 상세 설명 입력 (AI 분석 시작)
echo "📖 Step 3: 상세 설명 입력 (AI 분석 시작)"
curl -s -X PATCH "$BASE_URL/dreams/$DREAM_ID/details" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "detailedDescription": "구름 위를 날면서 아래를 내려다보니 제가 사는 동네가 보였어요. 날개는 없었지만 자유롭게 날 수 있었고, 바람이 시원했습니다.",
    "realLifeContext": "요즘 회사 업무가 많아서 스트레스를 많이 받고 있어요. 자유롭고 싶다는 생각을 자주 합니다."
  }' | jq '.'

echo "✅ AI 분석 시작됨 (백그라운드 처리 중...)"
echo ""

# Step 4: 분석 결과 조회 (폴링)
echo "🔍 Step 4: 분석 결과 조회 (폴링)"
for i in {1..10}; do
  echo "  폴링 시도 $i/10..."
  ANALYSIS=$(curl -s -X GET "$BASE_URL/dreams/$DREAM_ID/analysis" \
    -H "Authorization: Bearer $ACCESS_TOKEN")
  STATUS=$(echo "$ANALYSIS" | jq -r '.data.status')
  
  if [ "$STATUS" == "ANALYSIS_COMPLETED" ]; then
    echo "$ANALYSIS" | jq '.'
    echo "✅ AI 분석 완료!"
    break
  elif [ "$STATUS" == "FAILED" ]; then
    echo "❌ AI 분석 실패"
    echo "$ANALYSIS" | jq '.'
    exit 1
  fi
  
  sleep 2
done
echo ""

# Step 5: 웹툰 생성 (장르 선택)
echo "🎨 Step 5: 웹툰 생성 (FANTASY)"
curl -s -X POST "$BASE_URL/dreams/$DREAM_ID/webtoon" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "selectedGenre": "FANTASY"
  }' | jq '.'

echo "✅ 웹툰 생성 시작됨 (백그라운드 처리 중...)"
echo ""

# Step 6: 최종 결과 확인 (폴링)
echo "🖼️  Step 6: 최종 결과 확인 (폴링)"
for i in {1..20}; do
  echo "  폴링 시도 $i/20..."
  DREAM=$(curl -s -X GET "$BASE_URL/dreams/$DREAM_ID" \
    -H "Authorization: Bearer $ACCESS_TOKEN")
  STATUS=$(echo "$DREAM" | jq -r '.data.processingStatus')
  
  if [ "$STATUS" == "COMPLETED" ]; then
    echo "$DREAM" | jq '.'
    echo "✅ 웹툰 생성 완료!"
    break
  elif [ "$STATUS" == "FAILED" ]; then
    echo "❌ 웹툰 생성 실패"
    echo "$DREAM" | jq '.'
    exit 1
  fi
  
  sleep 3
done
echo ""

# 추가 기능 테스트
echo "📚 추가 기능 테스트"
echo "===================="
echo ""

# 라이브러리에 추가
echo "📌 라이브러리에 추가"
curl -s -X POST "$BASE_URL/dreams/$DREAM_ID/library" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'
echo ""

# 즐겨찾기 토글
echo "⭐ 즐겨찾기 토글"
curl -s -X PATCH "$BASE_URL/dreams/$DREAM_ID/favorite" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'
echo ""

# 라이브러리 조회
echo "📖 라이브러리 조회"
curl -s -X GET "$BASE_URL/library" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'
echo ""

# 심리상담 챗봇
echo "💬 심리상담 챗봇"
curl -s -X POST "$BASE_URL/dreams/$DREAM_ID/chat" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "message": "이 꿈이 무슨 의미인가요?"
  }' | jq '.'
echo ""

echo "🎉 전체 테스트 완료!"
echo "====================="
echo "dreamId: $DREAM_ID"

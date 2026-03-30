"""
KOMORAN 사용 예제
=================

설치:
    cd rust/komoran-rs
    pip install .

사용:
    from komoran import Komoran
    komoran = Komoran()
"""

from komoran import Komoran

# 모델 로드 (인자 없으면 내장 모델 자동 사용)
komoran = Komoran()

# 1. 형태소 분석 (plain text)
print("=== analyze() ===")
print(komoran.analyze("감기는 자주 걸리는 병이다"))
# 출력: 감기/VV 는/ETM 자주/MAG 걸리/VV 는/ETM 병/NNG 이/VCP 다/EC

# 2. 토큰 리스트 (위치 정보 포함)
print("\n=== tokens() ===")
for token in komoran.tokens("대한민국은 민주공화국이다"):
    print(f"  {token.morph}/{token.pos} [{token.begin_index}:{token.end_index}]")

# 3. 명사 추출
print("\n=== nouns() ===")
print(komoran.nouns("오늘 날씨가 정말 좋은 대한민국입니다"))

# 4. (형태소, 품사) 쌍 리스트
print("\n=== pos() ===")
print(komoran.pos("나는 밥을 먹었다"))

# 5. 배치 분석 (멀티스레드)
print("\n=== analyze_batch() ===")
sentences = [
    "감기는 자주 걸리는 병이다",
    "대한민국은 민주공화국이다",
    "아버지가방에들어가신다",
]
results = komoran.analyze_batch(sentences)
for s, r in zip(sentences, results):
    print(f"  {s} → {r}")

# 6. 배치 토큰 분석 (멀티스레드)
print("\n=== tokens_batch() ===")
token_results = komoran.tokens_batch(sentences)
for s, tokens in zip(sentences, token_results):
    token_str = " ".join(f"{t.morph}/{t.pos}" for t in tokens)
    print(f"  {s} → {token_str}")

"""
KOMORAN - 한국어 형태소 분석기 (Rust/Python)

사용법:
    from komoran import Komoran

    komoran = Komoran()                    # 기본 내장 모델 사용
    komoran = Komoran("path/to/model")     # 커스텀 모델 경로 지정

    komoran.analyze("아버지가방에들어가신다")
    # '아버지/NNG 가방/NNG 에/JKB 들어가/VV 시/EP ㄴ다/EC'
"""

from komoran._core import Komoran, Token

__version__ = "4.0.0"
__all__ = ["Komoran", "Token"]

"""
KOMORAN Python 래퍼
- 내장 모델 자동 탐색
- 사용자 친화적 API
"""

import os
from pathlib import Path
from typing import Optional

from komoran.komoran_rs import PyKomoran, PyToken


def _find_default_model() -> str:
    """내장 모델 디렉토리를 자동 탐색합니다."""
    # 1) 패키지 내장 모델
    pkg_dir = Path(__file__).parent
    pkg_model = pkg_dir / "model"
    if pkg_model.is_dir():
        return str(pkg_model)

    # 2) 개발 환경: 프로젝트 루트의 model/
    dev_model = pkg_dir.parent.parent / "model"
    if dev_model.is_dir():
        return str(dev_model)

    raise FileNotFoundError(
        "KOMORAN 모델을 찾을 수 없습니다. "
        "Komoran('path/to/model') 형식으로 모델 경로를 직접 지정해주세요."
    )


class Token:
    """형태소 분석 토큰

    Attributes:
        morph: 형태소
        pos: 품사 태그
        begin_index: 원문 시작 위치 (글자 단위, inclusive)
        end_index: 원문 끝 위치 (글자 단위, exclusive)
    """

    __slots__ = ("morph", "pos", "begin_index", "end_index")

    def __init__(self, py_token: PyToken):
        self.morph = py_token.morph
        self.pos = py_token.pos
        self.begin_index = py_token.begin_index
        self.end_index = py_token.end_index

    def __repr__(self) -> str:
        return f"{self.morph}/{self.pos}"

    def __str__(self) -> str:
        return f"{self.morph}/{self.pos}"


class Komoran:
    """KOMORAN 한국어 형태소 분석기

    Args:
        model_path: 모델 디렉토리 경로. 생략하면 내장 모델을 사용합니다.

    Examples:
        >>> from komoran import Komoran
        >>> k = Komoran()
        >>> k.analyze("아버지가방에들어가신다")
        '아버지/NNG 가방/NNG 에/JKB 들어가/VV 시/EP ㄴ다/EC'
    """

    def __init__(self, model_path: Optional[str] = None):
        if model_path is None:
            model_path = _find_default_model()
        self._engine = PyKomoran(model_path)

    def analyze(self, sentence: str) -> str:
        """형태소 분석 결과를 '형태소/품사' 형식 문자열로 반환합니다.

        Args:
            sentence: 분석할 문장

        Returns:
            '형태소/품사' 형식의 문자열
        """
        return self._engine.analyze(sentence)

    def nouns(self, sentence: str) -> list[str]:
        """문장에서 명사(NNG, NNP)를 추출합니다.

        Args:
            sentence: 분석할 문장

        Returns:
            명사 리스트
        """
        return self._engine.nouns(sentence)

    def pos(self, sentence: str) -> list[tuple[str, str]]:
        """(형태소, 품사) 튜플의 리스트를 반환합니다.

        Args:
            sentence: 분석할 문장

        Returns:
            (형태소, 품사) 튜플 리스트
        """
        return self._engine.pos(sentence)

    def tokens(self, sentence: str) -> list[Token]:
        """위치 정보를 포함한 토큰 리스트를 반환합니다.

        Args:
            sentence: 분석할 문장

        Returns:
            Token 객체 리스트
        """
        return [Token(t) for t in self._engine.tokens(sentence)]

    def analyze_batch(self, sentences: list[str]) -> list[str]:
        """여러 문장을 멀티스레드로 병렬 분석합니다.

        Args:
            sentences: 분석할 문장 리스트

        Returns:
            분석 결과 문자열 리스트
        """
        return self._engine.analyze_batch(sentences)

    def tokens_batch(self, sentences: list[str]) -> list[list[Token]]:
        """여러 문장의 토큰을 멀티스레드로 병렬 추출합니다.

        Args:
            sentences: 분석할 문장 리스트

        Returns:
            Token 리스트의 리스트
        """
        return [
            [Token(t) for t in tokens]
            for tokens in self._engine.tokens_batch(sentences)
        ]

    def __repr__(self) -> str:
        return "Komoran()"

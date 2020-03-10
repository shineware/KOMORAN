package kr.co.shineware.nlp.komoran.benchmark;

import kr.co.shineware.nlp.komoran.corpus.model.Dictionary;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;

import java.util.List;

public interface DataStructureSpeedTestInterface {
    KoreanUnitParser koreanUnitParser = new KoreanUnitParser();
    void load(Dictionary dictionary);
    long doTestAndGetAverageElapsedTime(List<String> lines);
}

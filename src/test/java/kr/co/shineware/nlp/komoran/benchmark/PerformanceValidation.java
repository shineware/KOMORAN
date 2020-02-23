package kr.co.shineware.nlp.komoran.benchmark;

import kr.co.shineware.ds.aho_corasick.AhoCorasickDictionary;
import kr.co.shineware.ds.aho_corasick.FindContext;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.corpus.model.Dictionary;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.file.FileUtil;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class PerformanceValidation {

    private static final Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);
    private static final KoreanUnitParser koreanUnitParser = new KoreanUnitParser();

    @Test
    //from https://github.com/shineware/KOMORAN/issues/96
    public void analyzeSpeedTest() {

        int totalTestCount = 10;

        List<String> lines = FileUtil.load2List("stress.test", StandardCharsets.UTF_8);

        long totalElapsedTime = 0L;

        for (int i = 0; i < totalTestCount + 1; i++) {
            long elapsedTime = 0L;
            long beginTime, endTime;
            for (String line : lines) {
                beginTime = System.currentTimeMillis();
                komoran.analyze(line);
                endTime = System.currentTimeMillis();
                elapsedTime += (endTime - beginTime);
            }
            if (i == 0) {
                continue;
            }
            System.out.println("Step " + i + " : " + elapsedTime);
            totalElapsedTime += elapsedTime;
        }
        System.out.println("Average elapsed time : " + (totalElapsedTime / totalTestCount));
    }

    @Test
    public void dataStructureSpeedTest() {

        int totalTestCount = 10;

        Dictionary dictionary = loadDictionary();
        AhoCorasickDictionary<List<String>> ahoCorasickDictionary = loadAhoCorasick(dictionary);
        List<String> lines = FileUtil.load2List("stress.test", StandardCharsets.UTF_8);

        long totalElapsedTime = 0L;

        for (int i = 0; i < totalTestCount + 1; i++) {

            long beginTime, endTime;
            long elapsedTime = 0L;

            for (String line : lines) {
                line = koreanUnitParser.parse(line);
                beginTime = System.currentTimeMillis();
                FindContext<List<String>> findContext = ahoCorasickDictionary.newFindContext();
                for (int j = 0; j < line.length(); j++) {
                    Map<String, List<String>> retrievedResult = ahoCorasickDictionary.get(findContext, line.charAt(j));
                    if (retrievedResult != null) {
                        for (Map.Entry<String, List<String>> morphPosListEntry : retrievedResult.entrySet()) {
                            ;
                        }
                    }
                }
                endTime = System.currentTimeMillis();
                elapsedTime += (endTime - beginTime);
            }
            if (i == 0) {
                continue;
            }
            System.out.println("Step " + i + " : " + elapsedTime);
            totalElapsedTime += elapsedTime;
        }
        System.out.println("Average elapsed time : " + (totalElapsedTime / totalTestCount));

    }

    private Dictionary loadDictionary() {
        return new Dictionary("corpus_build/dic.word");
    }

    private AhoCorasickDictionary<List<String>> loadAhoCorasick(Dictionary dictionary) {
        AhoCorasickDictionary<List<String>> ahoCorasickDictionary = new AhoCorasickDictionary<>();
        for (Map.Entry<String, Map<String, Integer>> morphPosFreqMapEntry : dictionary.getDictionary().entrySet()) {
            String morph = morphPosFreqMapEntry.getKey();
            Set<String> posSet = morphPosFreqMapEntry.getValue().keySet();
            ahoCorasickDictionary.put(koreanUnitParser.parse(morph), new ArrayList<>(posSet));
        }
        ahoCorasickDictionary.buildFailLink();
        return ahoCorasickDictionary;
    }
}

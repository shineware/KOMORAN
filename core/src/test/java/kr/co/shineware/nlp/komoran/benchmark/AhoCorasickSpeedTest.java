package kr.co.shineware.nlp.komoran.benchmark;

import kr.co.shineware.ds.aho_corasick.AhoCorasickDictionary;
import kr.co.shineware.ds.aho_corasick.FindContext;
import kr.co.shineware.nlp.komoran.corpus.model.Dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AhoCorasickSpeedTest implements DataStructureSpeedTestInterface {
    private AhoCorasickDictionary<List<String>> ahoCorasickDictionary;

    @Override
    public void load(Dictionary dictionary) {
        this.ahoCorasickDictionary = new AhoCorasickDictionary<>();
        for (Map.Entry<String, Map<String, Integer>> morphPosFreqMapEntry : dictionary.getDictionary().entrySet()) {
            String morph = morphPosFreqMapEntry.getKey();
            Set<String> posSet = morphPosFreqMapEntry.getValue().keySet();
            ahoCorasickDictionary.put(koreanUnitParser.parse(morph), new ArrayList<>(posSet));
        }
        ahoCorasickDictionary.buildFailLink();
    }

    @Override
    public long doTestAndGetAverageElapsedTime(List<String> lines) {
        long totalElapsedTime = 0L;

        int totalTestCount = 10;

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
        return (totalElapsedTime / totalTestCount);
    }
}

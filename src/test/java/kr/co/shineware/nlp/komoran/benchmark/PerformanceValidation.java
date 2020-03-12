package kr.co.shineware.nlp.komoran.benchmark;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.corpus.model.Dictionary;
import kr.co.shineware.util.common.file.FileUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Ignore
public class PerformanceValidation {

    private static final Komoran komoran = new Komoran(DEFAULT_MODEL.STABLE);

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
    public void crossValidation() {
        List<String> lines = getTotalTrainingData("/Users/shinjunsoo/shineware/data/komoran_training_data", "refine.txt");
//        List<String> lines = FileUtil.load2List("/Users/shinjunsoo/shineware/data/komoran_training_data/현대문어_형태분석_말뭉치.refine.txt", StandardCharsets.UTF_8);
        CrossValidationTester crossValidationTester = new CrossValidationTester(lines, 10, false);
        crossValidationTester.doTest();

    }

    private List<String> getTotalTrainingData(String filePath, String filePostfix) {
        List<String> trainingDataSentences = new ArrayList<>();
        List<String> filenameList = FileUtil.getFileNames(filePath, filePostfix);
        for (String filename : filenameList) {
            trainingDataSentences.addAll(FileUtil.load2List(filename, StandardCharsets.UTF_8));
        }
        Collections.shuffle(trainingDataSentences);
        return trainingDataSentences;
    }

    @Test
    public void dataStructureSpeedTest() {
        AhoCorasickSpeedTest ahoCorasickSpeedTest = new AhoCorasickSpeedTest();
        ahoCorasickSpeedTest.load(new Dictionary("corpus_build/dic.word"));
        System.out.println(ahoCorasickSpeedTest.doTestAndGetAverageElapsedTime(FileUtil.load2List("stress.test", StandardCharsets.UTF_8)));
    }
}

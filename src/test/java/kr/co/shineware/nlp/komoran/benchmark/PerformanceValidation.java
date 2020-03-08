package kr.co.shineware.nlp.komoran.benchmark;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.corpus.builder.CorpusBuilder;
import kr.co.shineware.nlp.komoran.corpus.model.Dictionary;
import kr.co.shineware.nlp.komoran.model.Token;
import kr.co.shineware.nlp.komoran.modeler.builder.ModelBuilder;
import kr.co.shineware.util.common.file.FileUtil;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PerformanceValidation {

    private static final Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);

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

        int totalLineSize = lines.size();
        int testLineStartIndex = 0;
        int testLineEndIndex = totalLineSize / 10;

        for (int i = 0; i < 10; i++) {
            System.out.println(testLineStartIndex + " to " + testLineEndIndex);
            List<String> testDataSet = new ArrayList<>(lines.subList(testLineStartIndex, testLineEndIndex));
            List<String> trainingDataSet = new ArrayList<>(lines.subList(0, testLineStartIndex));
            trainingDataSet.addAll(new ArrayList<>(lines.subList(testLineEndIndex, totalLineSize)));

            validation(trainingDataSet, testDataSet);

            testLineStartIndex = testLineEndIndex;
            testLineEndIndex = Math.min(testLineEndIndex + (totalLineSize / 10), totalLineSize);
            break;
        }
    }

    private void validation(List<String> trainingDataSet, List<String> testDataSet) {

        buildModel(trainingDataSet);
        Komoran komoran = new Komoran("models_validation");

        int correctCount = 0;
        int incorrectCount = 0;

        for (String testData : testDataSet) {
            String word = testData.split("\t")[0];
            String answer = testData.split("\t")[1];
            List<Token> analyzeResultList = komoran.analyze(word).getTokenList();
            List<Token> answerResultList = convertAnswerToTokenList(answer);

            for (Token analyzeToken : analyzeResultList) {
                if (answerResultList.contains(analyzeToken)) {
                    correctCount++;
                } else {
                    incorrectCount++;
                }
            }
        }

        System.out.println("Accuracy : " + (correctCount / (double) (correctCount + incorrectCount)));

    }

    private List<Token> convertAnswerToTokenList(String answer) {

        List<Token> answerTokenList = new ArrayList<>();

        String[] morphPosPairs = answer.split(" ");
        for (String morphPosPair : morphPosPairs) {
            int separatorIndex = morphPosPair.lastIndexOf('/');
            String morph = morphPosPair.substring(0, separatorIndex);
            String pos = morphPosPair.substring(separatorIndex + 1);
            answerTokenList.add(new Token(morph, pos, 0, 0));
        }
        return answerTokenList;
    }

    private void buildModel(List<String> trainingDataSet) {
        CorpusBuilder corpusBuilder = new CorpusBuilder();
        corpusBuilder.setExclusiveIrrRule("resources/irrDic.remove.txt");
        corpusBuilder.buildFromLine(trainingDataSet);
        corpusBuilder.save("corpus_build_validation");

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.buildPath("corpus_build_validation");
        modelBuilder.save("models_validation");
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

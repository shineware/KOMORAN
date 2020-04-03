package kr.co.shineware.nlp.komoran.benchmark;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.corpus.builder.CorpusBuilder;
import kr.co.shineware.nlp.komoran.corpus.builder.FWDBuilder;
import kr.co.shineware.nlp.komoran.model.Token;
import kr.co.shineware.nlp.komoran.modeler.builder.ModelBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrossValidationTester {
    private List<String> trainingDataList;
    private int n;
    private boolean loggingIncorrectResults;
    private BufferedWriter bw = null;

    public CrossValidationTester(List<String> trainingDataList, int n, boolean loggingIncorrectResults) {
        this.trainingDataList = trainingDataList;
        this.n = n;
        this.loggingIncorrectResults = loggingIncorrectResults;
        if (this.loggingIncorrectResults) {
            try {
                bw = new BufferedWriter(new FileWriter("incorrect_log.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void doTest() {
        int totalLineSize = trainingDataList.size();
        int testLineStartIndex = 0;
        int testLineEndIndex = totalLineSize / n;
        double averageAccuracy = 0.0;

        for (int i = 0; i < n; i++) {
            System.out.println(testLineStartIndex + " to " + testLineEndIndex);
            List<String> testDataSet = new ArrayList<>(trainingDataList.subList(testLineStartIndex, testLineEndIndex));
            List<String> trainingDataSet = new ArrayList<>(trainingDataList.subList(0, testLineStartIndex));
            trainingDataSet.addAll(new ArrayList<>(trainingDataList.subList(testLineEndIndex, totalLineSize)));

            double accuracy = validation(trainingDataSet, testDataSet);
            System.out.println("Accuracy : " + accuracy);
            averageAccuracy += accuracy;
            testLineStartIndex = testLineEndIndex;
            testLineEndIndex = Math.min(testLineEndIndex + (totalLineSize / n), totalLineSize);
        }
        if (bw != null) {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Average accuracy : " + (averageAccuracy / n));
    }

    private double validation(List<String> trainingDataSet, List<String> testDataSet) {

        buildModel(trainingDataSet);

        FWDBuilder fwdBuilder = new FWDBuilder();
        fwdBuilder.buildFromLine(trainingDataSet);
        fwdBuilder.save("fwd_validation.dic", 5);

        Komoran komoran = new Komoran("models_validation");
        komoran.setFWDic("fwd_validation.dic");

        int correctCount = 0;
        int incorrectCount = 0;
        boolean isIncorrect = false;

        for (String testData : testDataSet) {
            isIncorrect = false;
            if (testData.trim().length() == 0) {
                continue;
            }
            String word = testData.split("\t")[0];
            String answer = testData.split("\t")[1];
            List<Token> analyzeResultList = komoran.analyze(word).getTokenList();
            List<Token> answerResultList = convertAnswerToTokenList(answer);

            for (Token analyzeToken : analyzeResultList) {
                if (answerResultList.contains(analyzeToken)) {
                    correctCount++;
                } else {
                    incorrectCount++;
                    isIncorrect = true;
                }
            }

            if (loggingIncorrectResults && isIncorrect) {
                try {
                    bw.write("problem\t" + testData);
                    bw.newLine();
                    bw.write("answer\t" + answerResultList);
                    bw.newLine();
                    bw.write("predict\t" + analyzeResultList);
                    bw.newLine();
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return (correctCount / (double) (correctCount + incorrectCount));
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
}

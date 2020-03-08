/*******************************************************************************
 * KOMORAN 3.0 - Korean Morphology Analyzer
 *
 * Copyright 2015 Shineware http://www.shineware.co.kr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package kr.co.shineware.nlp.komoran.corpus.builder;

import kr.co.shineware.nlp.komoran.constant.FILENAME;
import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.corpus.model.Dictionary;
import kr.co.shineware.nlp.komoran.corpus.model.Grammar;
import kr.co.shineware.nlp.komoran.corpus.parser.CorpusParser;
import kr.co.shineware.nlp.komoran.corpus.parser.IrregularParser;
import kr.co.shineware.nlp.komoran.corpus.parser.model.ProblemAnswerPair;
import kr.co.shineware.nlp.komoran.exception.FileFormatException;
import kr.co.shineware.nlp.komoran.interfaces.UnitParser;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.file.FileUtil;
import kr.co.shineware.util.common.model.Pair;
import kr.co.shineware.util.common.string.StringUtil;

import java.io.*;
import java.lang.Character.UnicodeBlock;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 코퍼스로부터 모델 생성 시 필요한 데이터 생성 </br> 생성되는 데이터는 아래와 같음</br> - 단어 사전(word
 * dictionary)</br> - 문법(grammar)</br> - 기분석 사전(full word-phrase
 * dictionary)</br> - 불규칙 사전(irregular dictionary)</br>
 *
 * @author Junsoo Shin <jsshin@shineware.co.kr>
 * @version 2.2
 * @since 2.1
 */
public class CorpusBuilder {

    private UnitParser unitParser;
    private CorpusParser corpusParser;
    private IrregularParser irrParser;

    private Dictionary wordDic;
    private Dictionary irrDic;
    private Grammar grammar;

    private Set<String> irrExclusiveSet;

    public CorpusBuilder() {

        unitParser = new KoreanUnitParser();
        corpusParser = new CorpusParser();
        irrParser = new IrregularParser();

        wordDic = new Dictionary();
        irrDic = new Dictionary();
        grammar = new Grammar();

        irrExclusiveSet = new HashSet<>();
    }

    /**
     * 빌드한 코퍼스를 savePathName 디렉토리에 저장합니다. </p>
     * savePathName 디렉토리에는 dic.irregular, dic.word, grammar.in 파일이 저장됩니다.
     *
     * @param savePathName 빌드 데이터가 저장될 디렉토리
     */
    public void save(String savePathName) {
        File savePath = new File(savePathName);
        if (savePath.exists() && !savePath.isDirectory()) {
            System.err.println("CorpusBuilder.save error!");
            System.err
                    .println("savePathName is exists, but it's not a directory.");
            System.err.println("please check path name to save");
            System.exit(1);
        }
        savePath.mkdirs();
        wordDic.save(savePathName + File.separator + FILENAME.WORD_DIC);
        irrDic.save(savePathName + File.separator + FILENAME.IRREGULAR_DIC);
        grammar.save(savePathName + File.separator + FILENAME.GRAMMAR);
        savePath = null;
    }

    @Deprecated
    /**
     * 빌드된 코퍼스를 로드합니다. loadPath 디렉토리 아래에는 dic.irregular, dic.word, grammar.in 파일이 있어야 합니다.
     * @param loadPath 저장된 빌드 데이터의 디렉토리 경로
     */
    public void load(String loadPath) {
        wordDic.load(loadPath + File.separator + FILENAME.WORD_DIC);
        irrDic.load(loadPath + File.separator + FILENAME.IRREGULAR_DIC);
        grammar.load(loadPath + File.separator + FILENAME.GRAMMAR);
    }

    /**
     * coporaPath 밑에 있는 모든 파일을 빌드합니다. </p>
     * 모든 서브 디렉토리에 있는 파일들도 빌드됩니다.
     *
     * @param corporaPath 빌드 대상 파일들이 포함된 최상위 디렉토리 경로
     */
    public void buildPath(String corporaPath) {
        this.buildPath(corporaPath, null);
    }

    /**
     * coporaPath 밑에 있는 모든 파일 중 파일 확장자가 suffix로 끝나는 파일들만 빌드합니다. </p>
     * 모든 서브 디렉토리에 있는 파일들도 빌드됩낟.
     *
     * @param corporaPath 빌드 대상 파일들이 포함된 최상위 디렉토리 경로
     * @param suffix      빌드 대상 파일확장자
     */
    public void buildPath(String corporaPath, String suffix) {
        List<String> filenames = FileUtil.getFileNames(corporaPath);
        for (String filename : filenames) {
            if (suffix != null && filename.endsWith(suffix)) {
                System.out.println(filename);
                this.build(FileUtil.load2List(filename, StandardCharsets.UTF_8));
            }
            if (suffix == null) {
                System.out.println(filename);
                this.build(FileUtil.load2List(filename, StandardCharsets.UTF_8));
            }
        }

        //TODO : https://github.com/shineware/KOMORAN/issues/94
//        removeInvalidWords();
        pruningWordDictionary(0.01, 10);
        pruningIrregularDictionary(10);
    }

    public void buildFromLine(List<String> lines) {
        this.build(lines);
        pruningWordDictionary(0.01, 10);
        pruningIrregularDictionary(10);
    }

    private void removeInvalidWords() {
        Dictionary prunedWordDic = new Dictionary();
        Map<String, Map<String, Integer>> wordDictionary = this.wordDic.getDictionary();
        for (Map.Entry<String, Map<String, Integer>> morphPosFreqEntry : wordDictionary.entrySet()) {
            Map<String, Integer> prunedPosFreqMap;
            String morph = morphPosFreqEntry.getKey();
            prunedPosFreqMap = removeNoise(morphPosFreqEntry);
            updatePrunedDic(prunedWordDic, morph, prunedPosFreqMap);
        }
        this.wordDic = prunedWordDic;
    }

    private Map<String, Integer> removeNoise(Map.Entry<String, Map<String, Integer>> morphPosFreqEntry) {
        double totalFreq = getTotalFreq(morphPosFreqEntry.getValue());
        Map<String, Integer> prunedPosFreqMap = new HashMap<>();
        for (Map.Entry<String, Integer> posFreqMap : morphPosFreqEntry.getValue().entrySet()) {
            String pos = posFreqMap.getKey();
            int freq = posFreqMap.getValue();
            if (pos.equals("VA") && this.convertJaso(morphPosFreqEntry.getKey()).endsWith("ㅆ") && !morphPosFreqEntry.getKey().equals("있")) {
                continue;
            }
            prunedPosFreqMap.put(pos, freq);
        }
        return prunedPosFreqMap;
    }

    private void pruningIrregularDictionary(int pruningFreq) {
        Dictionary prunedWordDic = new Dictionary();
        Map<String, Map<String, Integer>> wordDictionary = this.irrDic.getDictionary();
        for (Map.Entry<String, Map<String, Integer>> morphPosFreqEntry : wordDictionary.entrySet()) {
            Map<String, Integer> prunedPosFreqMap;
            String morph = morphPosFreqEntry.getKey();
            prunedPosFreqMap = pruning(morphPosFreqEntry, 2.0, pruningFreq);
            updatePrunedDic(prunedWordDic, morph, prunedPosFreqMap);
        }
        this.irrDic = prunedWordDic;
    }

    private void pruningWordDictionary(double pruningFactor, int pruningFreq) {
        Dictionary prunedWordDic = new Dictionary();
        Map<String, Map<String, Integer>> wordDictionary = this.wordDic.getDictionary();
        for (Map.Entry<String, Map<String, Integer>> morphPosFreqEntry : wordDictionary.entrySet()) {
            Map<String, Integer> prunedPosFreqMap = morphPosFreqEntry.getValue();
            String morph = morphPosFreqEntry.getKey();
            if (morphPosFreqEntry.getValue().size() > 1) {
                prunedPosFreqMap = pruning(morphPosFreqEntry, pruningFactor, pruningFreq);
            }
            updatePrunedDic(prunedWordDic, morph, prunedPosFreqMap);
        }
        this.wordDic = prunedWordDic;
    }

    private void updatePrunedDic(Dictionary prunedWordDic, String morph, Map<String, Integer> prunedPosFreqMap) {
        for (Map.Entry<String, Integer> posFreqMap : prunedPosFreqMap.entrySet()) {
            prunedWordDic.append(morph, posFreqMap.getKey(), posFreqMap.getValue());
        }
    }

    private Map<String, Integer> pruning(Map.Entry<String, Map<String, Integer>> morphPosFreqEntry, double pruningFactor, int pruningFreq) {
        double totalFreq = getTotalFreq(morphPosFreqEntry.getValue());
        Map<String, Integer> prunedPosFreqMap = new HashMap<>();
        for (Map.Entry<String, Integer> posFreqMap : morphPosFreqEntry.getValue().entrySet()) {
            String pos = posFreqMap.getKey();
            int freq = posFreqMap.getValue();
            if ((freq / totalFreq) <= pruningFactor && freq < pruningFreq) {
                continue;
            }
            prunedPosFreqMap.put(pos, freq);
        }
        return prunedPosFreqMap;
    }

    private double getTotalFreq(Map<String, Integer> posFreqMap) {
        double totalFreq = 0.0;
        for (Map.Entry<String, Integer> posFreq : posFreqMap.entrySet()) {
            totalFreq += posFreq.getValue();
        }
        return totalFreq;
    }

    private void build(List<String> lines) {
        try {
            for (String line : lines) {
                int lineCount = 0;
                lineCount += 1;
                line = this.refineFormat(line);
                if (line.length() == 0) {
                    continue;
                }

                ProblemAnswerPair paPair = null;
                try {
                    paPair = this.corpusParser.parse(line);
                } catch (FileFormatException e) {
                    System.err.println(lineCount + " : " + line);
                    e.printStackTrace();
                    System.exit(1);
                }
                this.appendWordDictionary(paPair.getAnswerList());

                this.appendIrregularDictionary(paPair);

                this.appendGrammar(paPair.getAnswerList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * filename에 해당하는 파일을 빌드합니다.
     *
     * @param filename 빌드 대상 파일 경로
     */
    public void build(String filename) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
            String line;
            int lineCount = 0;

            while ((line = br.readLine()) != null) {
                lineCount += 1;
                line = this.refineFormat(line);
                if (lineCount < 10) {
                    System.out.println(line);
                }
                if (line.length() == 0) {
                    continue;
                }

                ProblemAnswerPair paPair = null;
                try {
                    paPair = this.corpusParser.parse(line);
                } catch (FileFormatException e) {
                    System.err.println(lineCount + " : " + line);
                    e.printStackTrace();
                    System.exit(1);
                }
                this.appendWordDictionary(paPair.getAnswerList());

                this.appendIrregularDictionary(paPair);

                this.appendGrammar(paPair.getAnswerList());
            }

            br.close();
            br = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * problem과 answerList의 morph 부분(first of pair)을 비교하여 불규칙이 발생하는지 여부를 판단 <br>
     * 단, 특수문자는 제외후 비교한다
     *
     * @param problem
     * @param answerList
     * @return
     */
    private boolean isIrregular(String problem,
                                List<Pair<String, String>> answerList) {
        StringBuffer answer = new StringBuffer();
        for (Pair<String, String> pair : answerList) {
            answer.append(pair.getFirst());
        }
        String problemUnits = this.unitParser.parse(problem);
        String answerUnits = this.unitParser.parse(answer.toString());
        return !StringUtil.getKorean(problemUnits).equals(
                StringUtil.getKorean(answerUnits));
    }

    /**
     * 불규칙 사전에 추가
     *
     * @param paPair
     */
    private void appendIrregularDictionary(ProblemAnswerPair paPair) {
        if (this.isIrregular(paPair.getProblem(), paPair.getAnswerList())) {
            // 자소 단위로 변환하여 불규칙 패턴 추출
            List<Pair<String, String>> irrRuleList = irrParser.parse(
                    this.convertJaso(paPair.getProblem()),
                    this.convertJaso(paPair.getAnswerList()));
            for (Pair<String, String> pair : irrRuleList) {
                //트레이닝 셋의 오류로 인한 skip(세종 코퍼스 기준)
                if (pair.getSecond().trim().length() == 0) {
                } else {
                    //불규칙 대상에 자소 단위가 포함된 경우 skip
                    if (this.irrExclusiveSet.contains(pair.getFirst() + "\t" + pair.getSecond().substring(0, pair.getSecond().lastIndexOf("/")))) {
                        continue;
                    }
                    boolean hasJamoProblem = false;
                    String tmpProblem = this.unitParser.combine(pair.getFirst());
                    for (int i = 0; i < tmpProblem.length(); i++) {
                        if (StringUtil.getUnicodeBlock(tmpProblem.charAt(i)) == UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
                            hasJamoProblem = true;
                            break;
                        }
                    }
                    if (hasJamoProblem) continue;
                    //놓으 -> 놓+으시와 같은 경우 skip
                    //않으 -> 않+으시
                    if (pair.getFirst().endsWith("ㅇㅡ") && pair.getSecond().endsWith("ㅇㅡㅅㅣ/EP")) {
                        continue;
                    }
                    irrDic.append(this.unitParser.combine(pair.getFirst()), this.unitParser.combine(pair.getSecond()));
                    //					irrDic.append(pair.getFirst(), pair.getSecond());
                }
            }
        }
    }

    /**
     * answerList의 형태소 부분을 자소 형태로 변환하여 반환
     *
     * @param answerList
     * @return
     */
    private List<Pair<String, String>> convertJaso(
            List<Pair<String, String>> answerList) {
        List<Pair<String, String>> resultList = new ArrayList<>();
        for (Pair<String, String> pair : answerList) {
            resultList.add(new Pair<String, String>(unitParser.parse(pair
                    .getFirst()), pair.getSecond()));
        }
        return resultList;
    }

    /**
     * problem을 자소 형태로 변환하여 반환
     *
     * @param problem
     * @return
     */
    private String convertJaso(String problem) {
        return unitParser.parse(problem);
    }

    /**
     * 문법에 품사 시퀀스 추가 <br>
     * answerList pair의 second 시퀀스만 사용<br>
     * 시퀀스 앞과 뒤에 <start> <end> 추가
     *
     * @param answerList
     */
    private void appendGrammar(List<Pair<String, String>> answerList) {
        String prevPos = SYMBOL.BOE;
        for (Pair<String, String> wordPosPair : answerList) {
            this.grammar.append(prevPos, wordPosPair.getSecond());
            prevPos = wordPosPair.getSecond();
        }
        String endPos = SYMBOL.EOE;
        this.grammar.append(prevPos, endPos);
    }

    /**
     * 단어 사전에 형태소, 품사 쌍 데이터 추가
     *
     * @param answerList
     */
    private void appendWordDictionary(List<Pair<String, String>> answerList) {
        for (Pair<String, String> pair : answerList) {
            if (pair.getFirst().trim().length() == 1) {
                if (StringUtil.getUnicodeBlock(pair.getFirst().trim().charAt(0)) == UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                        && pair.getSecond().contains("NN")) {
                    continue;
                }
            }

            if (pair.getSecond().equals("SH") ||
                    pair.getSecond().equals("SN") ||
                    pair.getSecond().equals("SL")) {
                continue;
            }

            //analyzer와 의존성이 있는 관계로 rule parser에 해당 내용이 포함되어 있어야함
            //근데 이걸 하면 빨라질까?
//			if(pair.getSecond().equals("SF")	//마침표, 물음표, 느낌표 . ? !
//					|| pair.getSecond().equals("SP")	//쉼표, 가운뎃점, 콜론, 빗금 , / ; :
//					|| pair.getSecond().equals("SS")	//따옴표, 괄호표, 줄표 " ' ` - < > { } [ ] ( )
//					|| pair.getSecond().equals("SO") 	//붙임표(물결, 숨김, 빠짐) ~
//					){	//줄임표 ...
//				continue;
//			}

            wordDic.append(pair.getFirst(), pair.getSecond());
        }
    }

    /**
     * 입력된 line의 형식을 정규화 <br>
     * 1. 2개 이상의 공백은 하나로 대체<br>
     * 2. line의 양 끝 trim
     *
     * @param line
     * @return
     */
    private String refineFormat(String line) {
        return line.replaceAll("[ ]+", " ").trim();
    }

    public void setExclusiveIrrRule(String filename) {
        try {
            this.irrExclusiveSet = new HashSet<>();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
//            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                //key :
                //remove :
                String key = line.substring(6);
                line = br.readLine();
                String remove = line.substring(9);
                this.irrExclusiveSet.add(key + "\t" + remove);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * filename에 해당하는 사용자 사전을 추가합니다. </p>
     * 사용자 사전은 코퍼스 빌드 시 함께 빌드됩니다.
     *
     * @param filename 사용자 사전 경로
     */
    public void appendUserDic(String filename) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
//            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#') continue;
                if (this.wordDic.getPosList(line) == null) {
                    this.wordDic.append(line, "NNP");
                } else {
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * path 밑에 있는 모든 파일 중 확장자가 suffix로 끝나는 파일들만 사용자 사전으로 추가합니다. </p>
     * 추가된 사용자 사전들은 코퍼스 빌드 시 함께 빌드됩니다.
     *
     * @param path   사용자 사전들이 포함된 최상위 디렉토리 경로
     * @param suffix 사용자 사전의 파일확장자
     */
    public void appendUserDicPath(String path, String suffix) {
        List<String> filenames = FileUtil.getFileNames(path);
        for (String filename : filenames) {
            if (filename.endsWith("." + suffix)) {
                System.out.println(filename);
                this.appendUserDic(filename);
            }
        }
        filenames = null;
    }
}

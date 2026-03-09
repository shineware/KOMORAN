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
package kr.co.shineware.nlp.komoran.core;

import kr.co.shineware.nlp.komoran.constant.*;
import kr.co.shineware.nlp.komoran.core.model.*;
import kr.co.shineware.nlp.komoran.core.model.combinationrules.CombinationRuleChecker;
import kr.co.shineware.nlp.komoran.core.model.combinationrules.MergedCombinationRuleChecker;
import kr.co.shineware.nlp.komoran.corpus.parser.CorpusParser;
import kr.co.shineware.nlp.komoran.corpus.parser.model.ProblemAnswerPair;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.MorphTag;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularNode;
import kr.co.shineware.nlp.komoran.modeler.model.Observation;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.nlp.komoran.util.KomoranCallable;
import kr.co.shineware.util.common.file.FileUtil;
import kr.co.shineware.util.common.model.Pair;
import kr.co.shineware.util.common.string.StringUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * KOMORAN core 클래스입니다.
 */
public class Komoran {

    private CombinationRuleChecker combinationRuleChecker;
    private Resources resources;
    private Observation userDic;
    private KoreanUnitParser unitParser;

    private HashMap<String, List<Pair<String, String>>> fwd;

    /**
     * modelPath 디렉토리에 있는 모델 파일들을 로딩하여 객체를 생성합니다. </p>
     * modelPath 디렉토리에는 pos.table, observation.model, transition.model, irregular.model 파일이 포함되어 있어야 합니다. </p>
     * 각 파일은 ModelBuilder를 통해 생성됩니다.
     *
     * @param modelPath 모델 파일들이 포함되어 있는 디렉토리 경로
     */
    public Komoran(String modelPath) {
        this.resources = new Resources();
        this.load(modelPath);
        this.unitParser = new KoreanUnitParser();
        MorphUtil morphUtil = new MorphUtil();
        TagUtil tagUtil = new TagUtil(this.resources.getTable());
        this.combinationRuleChecker = new MergedCombinationRuleChecker(morphUtil, tagUtil);
    }

    /**
     * 기본 내장 모델을 로딩하여 객체를 생성합니다.
     */
    public Komoran() {
        this.resources = new Resources();
        this.resources.init();
        String modelPath = FILENAME.DEFAULT_MODEL_PATH;

        String delimiter = "/";
        try (InputStream posTableFile =
                     this.getResourceStream(modelPath + delimiter + FILENAME.POS_TABLE);
             InputStream irrModelFile =
                     this.getResourceStream(modelPath + delimiter + FILENAME.IRREGULAR_MODEL);
             InputStream observationFile =
                     this.getResourceStream(modelPath + delimiter + FILENAME.OBSERVATION);
             InputStream transitionFile =
                     this.getResourceStream(modelPath + delimiter + FILENAME.TRANSITION)) {

            this.resources.loadPosTable(posTableFile);
            this.resources.loadIrregular(irrModelFile);
            this.resources.loadObservation(observationFile);
            this.resources.loadTransition(transitionFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load model resources: " + modelPath, e);
        }
        this.unitParser = new KoreanUnitParser();

        MorphUtil morphUtil = new MorphUtil();
        TagUtil tagUtil = new TagUtil(this.resources.getTable());
        this.combinationRuleChecker = new MergedCombinationRuleChecker(morphUtil, tagUtil);
    }

    private InputStream getResourceStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    /**
     * 파일 단위로 형태소 분석을 진행합니다.
     *
     * @param inputFilename  분석할 파일 경로
     * @param outputFilename 분석 결과가 저장될 파일 경로
     * @param thread         분석 시 사용할 thread 수
     */
    public void analyzeTextFile(String inputFilename, String outputFilename, int thread) {

        ExecutorService executor = Executors.newFixedThreadPool(thread);
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFilename), StandardCharsets.UTF_8))) {

            List<String> lines = FileUtil.load2List(inputFilename);
            List<Future<KomoranResult>> komoranResultList = new ArrayList<>();

            for (String line : lines) {
                KomoranCallable komoranCallable = new KomoranCallable(this, line);
                komoranResultList.add(executor.submit(komoranCallable));
            }

            for (Future<KomoranResult> komoranResultFuture : komoranResultList) {
                KomoranResult komoranResult = komoranResultFuture.get();
                bw.write(komoranResult.getPlainText());
                bw.newLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

    }

    /**
     * 여러 문장을 입력 받아 형태소 분석을 진행합니다.
     *
     * @param sentences 분석할 문장들이 담긴 List. 각 원소는 하나의 문장이라고 간주합니다.
     * @param thread    분석 시 사용할 thread 수
     * @return 문장 별 형태소 분석 결과가 담긴 List
     */
    public List<KomoranResult> analyze(List<String> sentences, int thread) {

        List<KomoranResult> komoranResultList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(thread);

        try {
            List<Future<KomoranResult>> komoranResultFutureList = new ArrayList<>();

            for (String line : sentences) {
                KomoranCallable komoranCallable = new KomoranCallable(this, line);
                komoranResultFutureList.add(executor.submit(komoranCallable));
            }

            for (Future<KomoranResult> komoranResultFuture : komoranResultFutureList) {
                komoranResultList.add(komoranResultFuture.get());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        return komoranResultList;
    }

    /**
     * 입력된 문장에 대해서 형태소 분석을 진행합니다.
     *
     * @param sentence 분석 대상 문장
     * @return 형태소 분석 결과
     */
    public KomoranResult analyze(String sentence) {
        return this.analyze(sentence, 1).get(0);
    }

    public double scoreDebug(List<String> analyzeResultList, List<String> analyzePosList){

        String prevPos = SYMBOL.BOE;
        double score = 0.0;

        for(int i=0;i<analyzePosList.size();i++){
            String currentMorph = analyzeResultList.get(i);
            String currentPos = analyzePosList.get(i);
            DoubleArrayAhoCorasick.DAFindContext findContext = this.resources.getObservation().getTrieDictionary().newFindContext();
            String jasoUnits = unitParser.parse(currentMorph);
            ScoredTag scoredTag = getScoredTag(jasoUnits, findContext, currentPos);
            score += getScore(currentMorph, scoredTag, prevPos, currentPos);
            prevPos = currentPos;
        }
        return score;
    }

    private double getScore(String currentMorph, ScoredTag scoredTag, String prevPos, String currentPos) {
        int prevId = this.resources.getTable().getId(prevPos);
        int currentId = this.resources.getTable().getId(currentPos);
        double transitionScore = this.resources.getTransition().getScore(prevId, currentId);

        if (scoredTag == null) {
            return transitionScore;
        }
        return transitionScore + scoredTag.getScore();

    }

    private ScoredTag getScoredTag(String jasoUnits, DoubleArrayAhoCorasick.DAFindContext findContext, String posResult) {

        for(int i=0;i<jasoUnits.length();i++){
            Map<String, List<ScoredTag>> keyScoreTagMap = this.resources.getObservation().getTrieDictionary().get(findContext, jasoUnits.charAt(i));
            if(keyScoreTagMap.get(jasoUnits) == null){
                continue;
            }
            for (ScoredTag scoredTag : keyScoreTagMap.get(jasoUnits)) {
                if(scoredTag.getTag().equals(posResult)){
                    return scoredTag;
                }
            }
        }
        return null;
    }

    /**
     * 입력된 문장에 대해서 형태소 분석을 진행 후 n-best 결과를 반환합니다.
     *
     * @param sentence 분석 대상 문장
     * @param nbest    분석 결과 중 추출할 상위 n개의 수
     * @return 형태소 분석 결과 중 nbest 수 만큼의 결과
     */
    public List<KomoranResult> analyze(String sentence, int nbest) {

        if(sentence == null || sentence.length() == 0){
            return new ArrayList<>(
                    Collections.singletonList(new KomoranResult(new ArrayList<>(), ""))
            );
        }

        Lattice lattice = new Lattice(this.resources, this.userDic, nbest, combinationRuleChecker);

        //연속된 숫자, 외래어, 기호 등을 파싱 하기 위한 버퍼
        ContinuousSymbolBuffer continuousSymbolBuffer = new ContinuousSymbolBuffer();

        //자소 단위로 분할 (parseWithType에서 결과를 재사용하여 jasoUnits 생성)
        List<Pair<Character, KoreanUnitParser.UnitType>> jasoUnitsWithType = unitParser.parseWithType(sentence);
        StringBuilder jasoBuilder = new StringBuilder(jasoUnitsWithType.size());
        for (Pair<Character, KoreanUnitParser.UnitType> pair : jasoUnitsWithType) {
            jasoBuilder.append(pair.getFirst());
        }
        String jasoUnits = jasoBuilder.toString();

        int length = jasoUnits.length();
        //start 노드 또는 end 노드의 바로 다음 인덱스
        //어절의 시작을 알리는 idx
        int whitespaceIndex = 0;
        boolean inserted;

        for (int curJasoIndex = 0; curJasoIndex < length; curJasoIndex++) {

            //기분석 사전
            int skipIdx = this.lookupFwd(lattice, jasoUnits, curJasoIndex);
            if (skipIdx != -1) {
                curJasoIndex = skipIdx - 1;
                continue;
            }

            char curJaso = jasoUnits.charAt(curJasoIndex);

            //띄어쓰기인 경우
            if (curJaso == ' ') {
                this.consumeContiniousSymbolParserBuffer(lattice, curJasoIndex, continuousSymbolBuffer);
                this.bridgeToken(lattice, curJasoIndex, jasoUnits, whitespaceIndex, jasoUnitsWithType);
                whitespaceIndex = curJasoIndex + 1;
            }

            this.continuousSymbolParsing(lattice, curJaso, curJasoIndex, continuousSymbolBuffer);
            this.symbolParsing(lattice, curJaso, curJasoIndex);
            if (this.userDic != null) {
                this.userDicParsing(lattice, curJaso, curJasoIndex);
            }
            this.regularParsing(lattice, curJaso, curJasoIndex);
            this.irregularParsing(lattice, curJaso, curJasoIndex);
            this.irregularExtends(lattice, curJaso, curJasoIndex);
        }


        this.consumeContiniousSymbolParserBuffer(lattice, jasoUnits, continuousSymbolBuffer);
        lattice.setLastIdx(jasoUnits.length());
        inserted = lattice.appendEndNode();
        //입력 문장의 끝에 END 품사가 올 수 없는 경우
        if (!inserted) {
            double NAPenaltyScore = SCORE.NA;
            if (whitespaceIndex != 0) {
                NAPenaltyScore += lattice.getNodeList(whitespaceIndex).get(0).getScore();
            }
            String combinedWord = unitParser.combineWithType(jasoUnitsWithType.subList(whitespaceIndex, jasoUnits.length()));
            LatticeNode latticeNode = new LatticeNode(whitespaceIndex, jasoUnits.length(), new MorphTag(combinedWord, SYMBOL.NA, SEJONGTAGS.NA_ID), NAPenaltyScore);
            latticeNode.setPrevNodeIdx(0);
            lattice.appendNode(latticeNode);
            lattice.appendEndNode();
        }

        List<List<LatticeNode>> nBestPath = lattice.findNBestPath();

        List<KomoranResult> nbestResultList = new ArrayList<>();

        //입력 문장 전체가 미분석인 경우
        if (nBestPath == null) {
            List<LatticeNode> resultList = new ArrayList<>();
            resultList.add(new LatticeNode(0, jasoUnits.length(), new MorphTag(sentence, "NA", -1), SCORE.NA));
            nbestResultList.add(new KomoranResult(resultList, jasoUnits));
        } else {
            for (List<LatticeNode> shortestPath : nBestPath) {
                Collections.reverse(shortestPath);
                List<LatticeNode> resultList = new ArrayList<>(shortestPath);
                nbestResultList.add(new KomoranResult(resultList, jasoUnits));
            }
        }

        return nbestResultList;
    }


    private void bridgeToken(Lattice lattice, int curIdx, String jasoUnits, int prevBeginSymbolIdx, List<Pair<Character, KoreanUnitParser.UnitType>> jasoUnitsWithType) {


        if (lattice.put(curIdx, curIdx + 1, SYMBOL.EOE, SYMBOL.EOE, SEJONGTAGS.EOE_ID, 0.0)) {
            return;
        }
        //공백이라면 END 기호를 삽입
        LatticeNode naLatticeNode = lattice.makeNode(prevBeginSymbolIdx, curIdx, unitParser.combineWithType(jasoUnitsWithType.subList(prevBeginSymbolIdx, curIdx)), SYMBOL.NA, this.resources.getTable().getId(SYMBOL.NA), SCORE.NA, 0);

        int naNodeIndex = lattice.appendNode(naLatticeNode);
        LatticeNode endLatticeNode = lattice.makeNode(curIdx, curIdx + 1, SYMBOL.EOE, SYMBOL.EOE, SEJONGTAGS.EOE_ID, 0.0, naNodeIndex);
        lattice.appendNode(endLatticeNode);
    }

    private void symbolParsing(Lattice lattice, char jaso, int idx) {

        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(jaso);
        //숫자
        if (StringUtil.isNumeric(jaso)) {
        } else if (unicodeBlock == Character.UnicodeBlock.BASIC_LATIN) {
            if (!isEnglishCharacter(jaso) && !isWhitespaceCharacter(jaso) && !isDictionaryEntryCharacter(jaso)) {
                lattice.put(idx, idx + 1, String.valueOf(jaso), SYMBOL.SW, SEJONGTAGS.SW_ID, SCORE.SW);
            }
        } else if (!StringUtil.isKorean(jaso) && !StringUtil.isJapanese(jaso) && !StringUtil.isChinese(jaso)) {
            lattice.put(idx, idx + 1, String.valueOf(jaso), SYMBOL.SW, SEJONGTAGS.SW_ID, SCORE.SW);
        }
    }

    private boolean isDictionaryEntryCharacter(char jaso) {
        return this.resources.getObservation().getTrieDictionary().getValue(jaso) != null;
    }

    private boolean isWhitespaceCharacter(char jaso) {
        return jaso == ' ';
    }

    private boolean isEnglishCharacter(char jaso) {
        return ((jaso >= 'A') && (jaso <= 'Z')) || ((jaso >= 'a') && (jaso <= 'z'));
    }

    private void userDicParsing(Lattice lattice, char jaso, int curIndex) {
        //Aho-corasick TRIE 기반의 사전 검색하여 형태소와 품사 및 품사 점수(observation)를 얻어옴
        Map<String, List<ScoredTag>> morphScoredTagsMap = lattice.retrievalUserDicObservation(jaso);

        if (morphScoredTagsMap == null || morphScoredTagsMap.size() == 0) {
            return;
        }

        //형태소 정보만 얻어옴
        Set<String> morphes = morphScoredTagsMap.keySet();

        //각 형태소와 품사 정보를 lattice에 삽입
        for (String morph : morphes) {
            int beginIdx = curIndex - morph.length() + 1;
            int endIdx = curIndex + 1;

            //형태소에 대한 품사 및 점수(observation) 정보를 List 형태로 가져옴
            List<ScoredTag> scoredTags = morphScoredTagsMap.get(morph);
            for (ScoredTag scoredTag : scoredTags) {
                lattice.put(beginIdx, endIdx, morph, scoredTag.getTag(), scoredTag.getTagId(), scoredTag.getScore());
            }
        }
    }

    private int lookupFwd(Lattice lattice, String token, int curJasoIndex) {

        if (this.fwd == null) {
            return -1;
        }

        //현재 인덱스가 시작이거나 이전 인덱스가 공백인 경우 (word 단어인 경우)
        //즉, 현재 인덱스가 온전한 단어의 시작 부분인 경우
        if (curJasoIndex == 0 || token.charAt(curJasoIndex - 1) == ' ') {
            //다음 공백을 찾아 단어(word)의 마지막 인덱스를 가져옴
            //다음에 공백이 없다면 입력 문자열의 마지막 인덱스를 가져옴 = 마지막 단어인 경우
            int wordEndIdx = token.indexOf(' ', curJasoIndex);
            wordEndIdx = wordEndIdx == -1 ? token.length() : wordEndIdx;
            String targetWord = token.substring(curJasoIndex, wordEndIdx);
            List<Pair<String, String>> fwdResultList = this.fwd.get(targetWord);

            if (fwdResultList != null) {
                this.insertLatticeForFwd(lattice, curJasoIndex, wordEndIdx, fwdResultList, targetWord);
                return wordEndIdx;
            }
        }
        return -1;
    }

    private void insertLatticeForFwd(Lattice lattice, int beginIdx, int endIdx,
                                     List<Pair<String, String>> fwdResultList, String targetWord) {

        lattice.put(beginIdx, endIdx, fwdResultList);
    }

    private static double getScoreForPos(String pos) {
        switch (pos) {
            case "SL": return SCORE.SL;
            case "SN": return SCORE.SN;
            case "SH": return SCORE.SH;
            default: return Double.NEGATIVE_INFINITY;
        }
    }

    private void continuousSymbolParsing(Lattice lattice, char charAt, int i, ContinuousSymbolBuffer continuousSymbolBuffer) {
        String curPos = "";
        if (StringUtil.isEnglish(charAt)) {
            curPos = "SL";
        } else if (StringUtil.isNumeric(charAt)) {
            curPos = "SN";
        } else if (StringUtil.isChinese(charAt)) {
            curPos = "SH";
        } else if (StringUtil.isForeign(charAt)) {
            curPos = "SL";
        }

        if (curPos.equals(continuousSymbolBuffer.getPrevPos())) {
            continuousSymbolBuffer.setPrevMorph(continuousSymbolBuffer.getPrevMorph() + charAt);
        } else {
            String prevPos = continuousSymbolBuffer.getPrevPos();
            double score = getScoreForPos(prevPos);
            if (score != Double.NEGATIVE_INFINITY) {
                lattice.put(continuousSymbolBuffer.getPrevBeginIdx(), i,
                        continuousSymbolBuffer.getPrevMorph(),
                        prevPos,
                        this.resources.getTable().getId(prevPos),
                        score);
            }
            continuousSymbolBuffer.setPrevBeginIdx(i);
            continuousSymbolBuffer.setPrevMorph(String.valueOf(charAt));
            continuousSymbolBuffer.setPrevPos(curPos);
        }
    }

    private void consumeContiniousSymbolParserBuffer(Lattice lattice, String in, ContinuousSymbolBuffer continuousSymbolBuffer) {
        this.consumeContiniousSymbolParserBuffer(lattice, in.length(), continuousSymbolBuffer);
    }

    private void consumeContiniousSymbolParserBuffer(Lattice lattice, int endIdx, ContinuousSymbolBuffer continuousSymbolBuffer) {
        String prevPos = continuousSymbolBuffer.getPrevPos();
        if (prevPos.isEmpty()) return;
        double score = getScoreForPos(prevPos);
        if (score != Double.NEGATIVE_INFINITY) {
            lattice.put(continuousSymbolBuffer.getPrevBeginIdx(), endIdx, continuousSymbolBuffer.getPrevMorph(),
                    prevPos, this.resources.getTable().getId(prevPos), score);
        }
    }

    private void irregularExtends(Lattice lattice, char jaso, int curIndex) {
        List<LatticeNode> prevLatticeNodes = lattice.getNodeList(curIndex);
        if (prevLatticeNodes != null) {
            Set<LatticeNode> extendedIrrNodeList = new HashSet<>();

            for (LatticeNode prevLatticeNode : prevLatticeNodes) {
                if (prevLatticeNode.getMorphTag().getTagId() == SYMBOL.IRREGULAR_ID) {
                    String lastMorph = prevLatticeNode.getMorphTag().getMorph();

                    // char[] 버퍼를 한번만 생성하여 재사용
                    char[] morphWithJaso = new char[lastMorph.length() + 1];
                    lastMorph.getChars(0, lastMorph.length(), morphWithJaso, 0);
                    morphWithJaso[lastMorph.length()] = jaso;
                    String morphJasoStr = new String(morphWithJaso);

                    if (this.resources.getObservation().getTrieDictionary().hasChild(morphWithJaso)) {
                        LatticeNode extendedIrregularNode = new LatticeNode();
                        extendedIrregularNode.setBeginIdx(prevLatticeNode.getBeginIdx());
                        extendedIrregularNode.setEndIdx(curIndex + 1);
                        extendedIrregularNode.setMorphTag(new MorphTag(morphJasoStr, SYMBOL.IRREGULAR, SYMBOL.IRREGULAR_ID));
                        extendedIrregularNode.setPrevNodeIdx(prevLatticeNode.getPrevNodeIdx());
                        extendedIrregularNode.setScore(prevLatticeNode.getScore());
                        extendedIrrNodeList.add(extendedIrregularNode);
                    }
                    List<ScoredTag> lastScoredTags = this.resources.getObservation().getTrieDictionary().getValue(morphJasoStr);
                    if (lastScoredTags == null) {
                        continue;
                    }

                    for (ScoredTag scoredTag : lastScoredTags) {
                        lattice.put(prevLatticeNode.getBeginIdx(), curIndex + 1, morphJasoStr,
                                scoredTag.getTag(), scoredTag.getTagId(), scoredTag.getScore());
                    }
                }
            }
            for (LatticeNode extendedIrrNode : extendedIrrNodeList) {
                lattice.appendNode(extendedIrrNode);
            }
        }
    }

    private void irregularParsing(Lattice lattice, char jaso, int curIndex) {
        //불규칙 노드들을 얻어옴
        Map<String, List<IrregularNode>> morphIrrNodesMap = lattice.retrievalIrregularNodes(jaso);
        if (morphIrrNodesMap == null || morphIrrNodesMap.size() == 0) {
            return;
        }

        //형태소 정보만 얻어옴
        Set<String> morphs = morphIrrNodesMap.keySet();
        for (String morph : morphs) {

            int beginIdx = curIndex - morph.length() + 1;
            int endIdx = curIndex + 1;

            List<IrregularNode> irrNodes = morphIrrNodesMap.get(morph);
            for (IrregularNode irregularNode : irrNodes) {
                lattice.put(beginIdx, endIdx, irregularNode);
            }
        }
    }

    private void regularParsing(Lattice lattice, char jaso, int curIndex) {
        Map<String, List<ScoredTag>> morphScoredTagsMap = lattice.retrievalObservation(jaso);

        if (morphScoredTagsMap == null || morphScoredTagsMap.size() == 0) {
            return;
        }

        int endIdx = curIndex + 1;
        for (Map.Entry<String, List<ScoredTag>> entry : morphScoredTagsMap.entrySet()) {
            String morph = entry.getKey();
            int beginIdx = curIndex - morph.length() + 1;

            List<ScoredTag> scoredTags = entry.getValue();
            for (ScoredTag scoredTag : scoredTags) {
                lattice.put(beginIdx, endIdx, morph, scoredTag.getTag(), scoredTag.getTagId(), scoredTag.getScore());
                if (scoredTag.getTagId() == SEJONGTAGS.EC_ID) {
                    lattice.put(beginIdx, endIdx, morph, SYMBOL.EF, SEJONGTAGS.EF_ID, scoredTag.getScore());
                }
            }
        }
    }

    private void load(String modelPath) {
        this.resources.load(modelPath);
    }

    /**
     * 형태소 분석 시 사용될 기분석 사전을 로드합니다. </p>
     * 형태소 분석 진행 전에 로드되어야 합니다. </p>
     * <pre>
     *     Komoran komoran = new Komoran();
     *     komoran.setFWDic("user_data/fwd.user");
     *     KomoranResult komoranResult = komoran.analyze("감기는 자주 걸리는 병이다");
     * </pre>
     *
     * @param filename 기분석 사전 파일 경로
     */
    public void setFWDic(String filename) {
        try {
            CorpusParser corpusParser = new CorpusParser();
            this.fwd = new HashMap<>();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tmp = line.split("\t");
                    //주석이거나 format에 안 맞는 경우는 skip
                    if (tmp.length != 2 || tmp[0].charAt(0) == '#') {
                        continue;
                    }
                    ProblemAnswerPair problemAnswerPair = corpusParser.parse(line);
                    List<Pair<String, String>> convertAnswerList = new ArrayList<>();
                    for (Pair<String, String> pair : problemAnswerPair.getAnswerList()) {
                        convertAnswerList.add(
                                new Pair<>(pair.getFirst(), pair.getSecond()));
                    }

                    this.fwd.put(this.unitParser.parse(problemAnswerPair.getProblem()),
                            convertAnswerList);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 형태소 분석 시 사용될 사용자 사전을 로드합니다. </p>
     * 형태소 분석 진행 전에 로드되어야 합니다.
     * <pre>
     *     Komoran komoran = new Komoran();
     *     komoran.setUserDic("user_date/dic.user");
     *     KomoranResult komoranResult = komoran.analyze("바람과 함께 사라지다를 봤어");
     * </pre>
     *
     * @param userDic 사용자 사전 파일 경로
     */
    public void setUserDic(String userDic) {
        try {

            this.userDic = new Observation();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(userDic), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0 || line.charAt(0) == '#') continue;
                    int lastIdx = line.lastIndexOf("\t");

                    String morph;
                    String pos;
                    //사용자 사전에 태그가 없는 경우에는 고유 명사로 태깅
                    if (lastIdx == -1) {
                        morph = line.trim();
                        pos = "NNP";
                    } else {
                        morph = line.substring(0, lastIdx);
                        pos = line.substring(lastIdx + 1);
                    }
                    this.userDic.put(morph, pos, this.resources.getTable().getId(pos), 0.0);
                }
            }

            //init
            this.userDic.getTrieDictionary().buildFailLink();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

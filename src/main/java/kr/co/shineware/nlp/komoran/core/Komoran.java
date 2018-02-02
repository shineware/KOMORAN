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

import kr.co.shineware.ds.aho_corasick.FindContext;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.constant.FILENAME;
import kr.co.shineware.nlp.komoran.constant.SCORE;
import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.model.ContinuousSymbolInfo;
import kr.co.shineware.nlp.komoran.core.model.Lattice;
import kr.co.shineware.nlp.komoran.core.model.LatticeNode;
import kr.co.shineware.nlp.komoran.core.model.Resources;
import kr.co.shineware.nlp.komoran.corpus.parser.CorpusParser;
import kr.co.shineware.nlp.komoran.corpus.parser.model.ProblemAnswerPair;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.MorphTag;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.model.Tag;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularNode;
import kr.co.shineware.nlp.komoran.modeler.model.Observation;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.nlp.komoran.util.KomoranCallable;
import kr.co.shineware.util.common.file.FileUtil;
import kr.co.shineware.util.common.model.Pair;
import kr.co.shineware.util.common.string.StringUtil;

import java.io.*;
import java.lang.Character.UnicodeBlock;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class Komoran implements Cloneable {

    private Resources resources;
    private Observation userDic;
    private KoreanUnitParser unitParser;

    private HashMap<String, List<Pair<String, String>>> fwd;

    public Komoran(String modelPath) {
        this.resources = new Resources();
        this.load(modelPath);
        this.unitParser = new KoreanUnitParser();
    }

    public Komoran(DEFAULT_MODEL modelType) {

        this.resources = new Resources();
        this.resources.init();
        String modelPath;
        if (modelType == DEFAULT_MODEL.FULL) {
            modelPath = FILENAME.FULL_MODEL;
        } else if (modelType == DEFAULT_MODEL.LIGHT) {
            modelPath = FILENAME.LIGHT_MODEL;
        } else {
            modelPath = FILENAME.FULL_MODEL;
        }

        String delimiter = "/";
        InputStream posTableFile =
            this.getResourceStream(String.join(delimiter, modelPath, FILENAME.POS_TABLE));
        InputStream irrModelFile =
            this.getResourceStream(String.join(delimiter, modelPath, FILENAME.IRREGULAR_MODEL));
        InputStream observationFile =
            this.getResourceStream(String.join(delimiter, modelPath, FILENAME.OBSERVATION));
        InputStream transitionFile =
            this.getResourceStream(String.join(delimiter, modelPath, FILENAME.TRANSITION));

        this.resources.loadPosTable(posTableFile);
        this.resources.loadIrregular(irrModelFile);
        this.resources.loadObservation(observationFile);
        this.resources.loadTransition(transitionFile);
        this.unitParser = new KoreanUnitParser();
    }

    private InputStream getResourceStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    public void analyzeTextFile(String inputFilename, String outputFilename, int thread) {

        try {
            List<String> lines = FileUtil.load2List(inputFilename);

            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilename));
            List<Future<KomoranResult>> komoranResultList = new ArrayList<>();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(thread);

            for (String line : lines) {
                KomoranCallable komoranCallable = new KomoranCallable(this, line);
                komoranResultList.add(executor.submit(komoranCallable));
            }

            for (Future<KomoranResult> komoranResultFuture : komoranResultList) {
                KomoranResult komoranResult = komoranResultFuture.get();
                bw.write(komoranResult.getPlainText());
                bw.newLine();
            }
            bw.close();
            executor.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public KomoranResult analyze(String sentence) {

        FindContext<List<ScoredTag>> observationFindContext;
        FindContext<List<IrregularNode>> irregularFindContext;
        FindContext<List<ScoredTag>> userDicFindContext = null;

        observationFindContext = this.resources.getObservation().getTrieDictionary().newFindContext();

        irregularFindContext = this.resources.getIrrTrie().getTrieDictionary().newFindContext();
        if (this.userDic != null) {
            userDicFindContext = this.userDic.getTrieDictionary().newFindContext();
        }

        sentence = sentence.replaceAll("[ ]+", " ").trim();

        List<LatticeNode> resultList = new ArrayList<>();

        Lattice lattice;

        lattice = new Lattice(this.resources);
        lattice.setUnitParser(this.unitParser);

        //연속된 숫자, 외래어, 기호 등을 파싱 하기 위한 버퍼
//		String prevPos;
//		String prevMorph;
//		int prevBeginIdx;
        ContinuousSymbolInfo continuousSymbolInfo = new ContinuousSymbolInfo();

        //자소 단위로 분할
        String jasoUnits = unitParser.parse(sentence);
        List<Pair<Character, KoreanUnitParser.UnitType>> jasoUnitsWithType = unitParser.parseWithType(sentence);

        int length = jasoUnits.length();
        //start 노드 또는 end 노드의 바로 다음 인덱스
        //어절의 시작을 알리는 idx
        int prevStartIdx = 0;
        boolean inserted;

        for (int i = 0; i < length; i++) {
            //기분석 사전
            int skipIdx = this.lookupFwd(lattice, jasoUnits, i);
            if (skipIdx != -1) {
                i = skipIdx - 1;
                continue;
            }

            //띄어쓰기인 경우
            if (jasoUnits.charAt(i) == ' ') {
                this.consumeContiniousSymbolParserBuffer(lattice, i, continuousSymbolInfo);
                this.bridgeToken(lattice, i, jasoUnits, prevStartIdx);
                prevStartIdx = i + 1;
            }
            this.continiousSymbolParsing(lattice, jasoUnits.charAt(i), i, continuousSymbolInfo); //숫자, 영어, 외래어 파싱
            this.symbolParsing(lattice, jasoUnits.charAt(i), i); // 기타 심볼 파싱
            this.userDicParsing(lattice, userDicFindContext, jasoUnits.charAt(i), i); //사용자 사전 적용

            this.regularParsing(lattice, observationFindContext, jasoUnits.charAt(i), i); //일반규칙 파싱
            this.irregularParsing(lattice, irregularFindContext, jasoUnits.charAt(i), i); //불규칙 파싱
            this.irregularExtends(lattice, jasoUnits.charAt(i), i); //불규칙 확장

        }

        this.consumeContiniousSymbolParserBuffer(lattice, jasoUnits, continuousSymbolInfo);
        lattice.setLastIdx(jasoUnits.length());
        inserted = lattice.appendEndNode();
        //입력 문장의 끝에 END 품사가 올 수 없는 경우
        if (!inserted) {
            double NAPenaltyScore = SCORE.NA;
            if (prevStartIdx != 0) {
                NAPenaltyScore += lattice.getNodeList(prevStartIdx).get(0).getScore();
            }
            String combinedWord = unitParser.combineWithType(jasoUnitsWithType.subList(prevStartIdx, jasoUnits.length()));
            LatticeNode latticeNode = new LatticeNode(prevStartIdx, jasoUnits.length(), new MorphTag(combinedWord, SYMBOL.NA, this.resources.getTable().getId(SYMBOL.NA)), NAPenaltyScore);
            latticeNode.setPrevNodeIdx(0);
            lattice.appendNode(latticeNode);
            lattice.appendEndNode();
        }

        List<LatticeNode> shortestPathList = lattice.findPath();

        //입력 문장 전체가 미분석인 경우
        if (shortestPathList == null) {
            resultList.add(new LatticeNode(0, jasoUnits.length(), new MorphTag(sentence, "NA", -1), SCORE.NA));
        } else {
            Collections.reverse(shortestPathList);
            resultList.addAll(shortestPathList);
        }

        return new KomoranResult(resultList, jasoUnits);
    }

    private void bridgeToken(Lattice lattice, int curIdx, String jasoUnits, int prevBeginSymbolIdx) {


        if (lattice.put(curIdx, curIdx + 1, SYMBOL.END, SYMBOL.END, this.resources.getTable().getId(SYMBOL.END), 0.0)) {
            return;
        }

        //공백이라면 END 기호를 삽입
        LatticeNode naLatticeNode = lattice.makeNode(prevBeginSymbolIdx, curIdx, jasoUnits.substring(prevBeginSymbolIdx, curIdx), SYMBOL.NA, this.resources.getTable().getId(SYMBOL.NA), SCORE.NA, 0);

        int naNodeIndex = lattice.appendNode(naLatticeNode);
        LatticeNode endLatticeNode = lattice.makeNode(curIdx, curIdx + 1, SYMBOL.END, SYMBOL.END, this.resources.getTable().getId(SYMBOL.END), 0.0, naNodeIndex);
        lattice.appendNode(endLatticeNode);
    }

    private boolean symbolParsing(Lattice lattice, char jaso, int idx) {

        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(jaso);
        //숫자
        if (Character.isDigit(jaso)) {
            return false;
        } else if (unicodeBlock == Character.UnicodeBlock.BASIC_LATIN) {
            //영어
            if (((jaso >= 'A') && (jaso <= 'Z')) || ((jaso >= 'a') && (jaso <= 'z'))) {
                return false;
            } else if (this.resources.getObservation().getTrieDictionary().getValue("" + jaso) != null) {
                return false;
            } else if (jaso == ' ') {
                return false;
            }
            //아스키 코드 범위 내에 사전에 없는 경우에는 기타 문자
            else {
                lattice.put(idx, idx + 1, "" + jaso, SYMBOL.SW, this.resources.getTable().getId(SYMBOL.SW), SCORE.SW);
                return true;
            }
        }
        //한글
        else if (unicodeBlock == UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || unicodeBlock == UnicodeBlock.HANGUL_JAMO
                || unicodeBlock == UnicodeBlock.HANGUL_JAMO_EXTENDED_A
                || unicodeBlock == UnicodeBlock.HANGUL_JAMO_EXTENDED_B
                || unicodeBlock == UnicodeBlock.HANGUL_SYLLABLES) {
            return false;
        }
        //일본어
        else if (unicodeBlock == UnicodeBlock.KATAKANA
                || unicodeBlock == UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS) {
            return false;
        }
        //중국어
        else if (UnicodeBlock.CJK_COMPATIBILITY.equals(unicodeBlock)
                || UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(unicodeBlock)
                || UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(unicodeBlock)
                || UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B.equals(unicodeBlock)
                || UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(unicodeBlock)) {
            return false;
        }
        //그 외 문자인 경우
        else {
            lattice.put(idx, idx + 1, "" + jaso, SYMBOL.SW, this.resources.getTable().getId(SYMBOL.SW), SCORE.SW);
            return true;
        }
    }

    private boolean userDicParsing(Lattice lattice, FindContext<List<ScoredTag>> userDicFindContext, char jaso, int curIndex) {
        //TRIE 기반의 사전 검색하여 형태소와 품사 및 품사 점수(observation)를 얻어옴
        Map<String, List<ScoredTag>> morphScoredTagsMap = this.getMorphScoredTagMapFromUserDic(userDicFindContext, jaso);

        if (morphScoredTagsMap == null) {
            return false;
        }

        //형태소 정보만 얻어옴
        Set<String> morphes = this.getMorphes(morphScoredTagsMap);

        //각 형태소와 품사 정보를 lattice에 삽입
        for (String morph : morphes) {
            int beginIdx = curIndex - morph.length() + 1;
            int endIdx = curIndex + 1;

            //형태소에 대한 품사 및 점수(observation) 정보를 List 형태로 가져옴
            List<ScoredTag> scoredTags = morphScoredTagsMap.get(morph);
            for (ScoredTag scoredTag : scoredTags) {
                this.insertLattice(lattice, beginIdx, endIdx, morph, scoredTag, scoredTag.getScore());
            }
        }
        return true;
    }

    //TO DO
    //기분석 사전을 어떻게 적용할 것인가....
    //Lucene에서 활용할 때 인덱스 정보를 어떻게 keep 할 것인가..
    private int lookupFwd(Lattice lattice, String token, int curIdx) {

        if (this.fwd == null) {
            return -1;
        }

        //현재 인덱스가 시작이거나 이전 인덱스가 공백인 경우 (word 단어인 경우)
        //현재 인덱스가 온전한 단어의 시작 부분인 경우
        if (curIdx == 0 || token.charAt(curIdx - 1) == ' ') {
            //공백을 찾아 단어(word)의 마지막 인덱스를 가져옴
            int wordEndIdx = token.indexOf(' ', curIdx);
            wordEndIdx = wordEndIdx == -1 ? token.length() : wordEndIdx;
            String targetWord = token.substring(curIdx, wordEndIdx);
            List<Pair<String, String>> fwdResultList = this.fwd.get(targetWord);

            if (fwdResultList != null) {
                this.insertLatticeForFwd(lattice, curIdx, wordEndIdx, fwdResultList);
                return wordEndIdx;
            }
        }
        return -1;
    }

    private void insertLatticeForFwd(Lattice lattice, int beginIdx, int endIdx,
                                     List<Pair<String, String>> fwdResultList) {
        lattice.put(beginIdx, endIdx, fwdResultList);
    }

    private void continiousSymbolParsing(Lattice lattice, char charAt, int i, ContinuousSymbolInfo continuousSymbolInfo) {
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

        if (curPos.equals(continuousSymbolInfo.getPrevPos())) {
            continuousSymbolInfo.setPrevMorph(continuousSymbolInfo.getPrevMorph() + charAt);
        } else {
            switch (continuousSymbolInfo.getPrevPos()) {
                case "SL":
//					lattice.put(this.prevBeginIdx, i, this.prevMorph, this.prevPos, this.resources.getTable().getId(this.prevPos), SCORE.SL);
                    lattice.put(continuousSymbolInfo.getPrevBeginIdx(), i,
                            continuousSymbolInfo.getPrevMorph(),
                            continuousSymbolInfo.getPrevPos(),
                            this.resources.getTable().getId(continuousSymbolInfo.getPrevPos()),
                            SCORE.SL
                    );
                    break;
                case "SN":
//					lattice.put(this.prevBeginIdx, i, this.prevMorph, this.prevPos, this.resources.getTable().getId(this.prevPos), SCORE.SN);
                    lattice.put(continuousSymbolInfo.getPrevBeginIdx(), i,
                            continuousSymbolInfo.getPrevMorph(),
                            continuousSymbolInfo.getPrevPos(),
                            this.resources.getTable().getId(continuousSymbolInfo.getPrevPos()),
                            SCORE.SN
                    );
                    break;
                case "SH":
//					lattice.put(this.prevBeginIdx, i, this.prevMorph, this.prevPos, this.resources.getTable().getId(this.prevPos), SCORE.SH);
                    lattice.put(continuousSymbolInfo.getPrevBeginIdx(), i,
                            continuousSymbolInfo.getPrevMorph(),
                            continuousSymbolInfo.getPrevPos(),
                            this.resources.getTable().getId(continuousSymbolInfo.getPrevPos()),
                            SCORE.SH
                    );
                    break;
            }
            continuousSymbolInfo.setPrevBeginIdx(i);
            continuousSymbolInfo.setPrevMorph("" + charAt);
            continuousSymbolInfo.setPrevPos(curPos);
//			this.prevBeginIdx = i;
//			this.prevMorph = ""+charAt;
//			this.prevPos = curPos;
        }
    }

    private void consumeContiniousSymbolParserBuffer(Lattice lattice, String in, ContinuousSymbolInfo continuousSymbolInfo) {
        if (continuousSymbolInfo.getPrevPos().trim().length() != 0) {
            switch (continuousSymbolInfo.getPrevPos()) {
                case "SL":
//					lattice.put(this.prevBeginIdx, in.length(), this.prevMorph, this.prevPos, this.resources.getTable().getId(this.prevPos), SCORE.SL);
                    lattice.put(continuousSymbolInfo.getPrevBeginIdx(),
                            in.length(),
                            continuousSymbolInfo.getPrevMorph(),
                            continuousSymbolInfo.getPrevPos(),
                            this.resources.getTable().getId(continuousSymbolInfo.getPrevPos()),
                            SCORE.SL
                    );
                    break;
                case "SH":
//					lattice.put(this.prevBeginIdx, in.length(), this.prevMorph, this.prevPos, this.resources.getTable().getId(this.prevPos), SCORE.SH);
                    lattice.put(continuousSymbolInfo.getPrevBeginIdx(),
                            in.length(),
                            continuousSymbolInfo.getPrevMorph(),
                            continuousSymbolInfo.getPrevPos(),
                            this.resources.getTable().getId(continuousSymbolInfo.getPrevPos()),
                            SCORE.SH
                    );
                    break;
                case "SN":
//					lattice.put(this.prevBeginIdx, in.length(), this.prevMorph, this.prevPos, this.resources.getTable().getId(this.prevPos), SCORE.SN);
                    lattice.put(continuousSymbolInfo.getPrevBeginIdx(),
                            in.length(),
                            continuousSymbolInfo.getPrevMorph(),
                            continuousSymbolInfo.getPrevPos(),
                            this.resources.getTable().getId(continuousSymbolInfo.getPrevPos()),
                            SCORE.SN
                    );
                    break;
            }
        }
    }

    private void consumeContiniousSymbolParserBuffer(Lattice lattice, int endIdx, ContinuousSymbolInfo continuousSymbolInfo) {
        if (continuousSymbolInfo.getPrevPos().trim().length() != 0) {
            switch (continuousSymbolInfo.getPrevPos()) {
                case "SL":
                    lattice.put(continuousSymbolInfo.getPrevBeginIdx(), endIdx, continuousSymbolInfo.getPrevMorph(),
                            continuousSymbolInfo.getPrevPos(), this.resources.getTable().getId(continuousSymbolInfo.getPrevPos()), SCORE.SL);
                    break;
                case "SH":
                    lattice.put(continuousSymbolInfo.getPrevBeginIdx(), endIdx, continuousSymbolInfo.getPrevMorph(),
                            continuousSymbolInfo.getPrevPos(), this.resources.getTable().getId(continuousSymbolInfo.getPrevPos()), SCORE.SH);
                    break;
                case "SN":
                    lattice.put(continuousSymbolInfo.getPrevBeginIdx(), endIdx, continuousSymbolInfo.getPrevMorph(),
                            continuousSymbolInfo.getPrevPos(), this.resources.getTable().getId(continuousSymbolInfo.getPrevPos()), SCORE.SN);
//					lattice.put(this.prevBeginIdx, endIdx, this.prevMorph, this.prevPos, this.resources.getTable().getId(this.prevPos), SCORE.SN);
                    break;
            }
        }
    }

    private void irregularExtends(Lattice lattice, char jaso, int curIndex) {
        List<LatticeNode> prevLatticeNodes = lattice.getNodeList(curIndex);
        if (prevLatticeNodes != null) {
            Set<LatticeNode> extendedIrrNodeList = new HashSet<>();

            for (LatticeNode prevLatticeNode : prevLatticeNodes) {
                //불규칙 태그인 경우에 대해서만
                if (prevLatticeNode.getMorphTag().getTagId() == SYMBOL.IRREGULAR_ID) {
                    //마지막 형태소 정보를 얻어옴
                    String lastMorph = prevLatticeNode.getMorphTag().getMorph();

                    //불규칙의 마지막 형태소에 현재 자소 단위를 합쳤을 때 자식 노드가 있다면 계속 탐색 가능 후보로 처리 해야함
                    if (this.resources.getObservation().getTrieDictionary().hasChild((lastMorph + jaso).toCharArray())) {
                        LatticeNode extendedIrregularNode = new LatticeNode();
                        extendedIrregularNode.setBeginIdx(prevLatticeNode.getBeginIdx());
                        extendedIrregularNode.setEndIdx(curIndex + 1);
                        extendedIrregularNode.setMorphTag(new MorphTag(prevLatticeNode.getMorphTag().getMorph() + jaso, SYMBOL.IRREGULAR, SYMBOL.IRREGULAR_ID));
                        extendedIrregularNode.setPrevNodeIdx(prevLatticeNode.getPrevNodeIdx());
                        extendedIrregularNode.setScore(prevLatticeNode.getScore());
                        extendedIrrNodeList.add(extendedIrregularNode);
                    }
                    //불규칙의 마지막 형태소에 현재 자소 단위를 합쳐 점수를 얻어옴
                    List<ScoredTag> lastScoredTags = this.resources.getObservation().getTrieDictionary().getValue(lastMorph + jaso);
                    if (lastScoredTags == null) {
                        continue;
                    }

                    //얻어온 점수를 토대로 lattice에 넣음
                    for (ScoredTag scoredTag : lastScoredTags) {
                        lattice.put(prevLatticeNode.getBeginIdx(), curIndex + 1, prevLatticeNode.getMorphTag().getMorph() + jaso,
                                scoredTag.getTag(), scoredTag.getTagId(), scoredTag.getScore());
                    }
                }
            }
            for (LatticeNode extendedIrrNode : extendedIrrNodeList) {
                lattice.appendNode(extendedIrrNode);
            }

        }
    }

    private boolean irregularParsing(Lattice lattice, FindContext<List<IrregularNode>> irregularFindContext, char jaso, int curIndex) {
        //불규칙 노드들을 얻어옴
        Map<String, List<IrregularNode>> morphIrrNodesMap = this.getIrregularNodes(irregularFindContext, jaso);
        if (morphIrrNodesMap == null) {
            return false;
        }

        //형태소 정보만 얻어옴
        Set<String> morphs = morphIrrNodesMap.keySet();
        for (String morph : morphs) {
            List<IrregularNode> irrNodes = morphIrrNodesMap.get(morph);
            int beginIdx = curIndex - morph.length() + 1;
            int endIdx = curIndex + 1;
            for (IrregularNode irregularNode : irrNodes) {
                this.insertLattice(lattice, beginIdx, endIdx, irregularNode);
            }
        }

        return true;
    }

    private void insertLattice(Lattice lattice, int beginIdx, int endIdx,
                               IrregularNode irregularNode) {
        lattice.put(beginIdx, endIdx, irregularNode);
    }

    private void regularParsing(Lattice lattice, FindContext<List<ScoredTag>> observationFindContext, char jaso, int curIndex) {
        //TRIE 기반의 사전 검색하여 형태소와 품사 및 품사 점수(observation)를 얻어옴
        Map<String, List<ScoredTag>> morphScoredTagsMap = this.getMorphScoredTagsMap(observationFindContext, jaso);

        if (morphScoredTagsMap == null) {
            return;
        }

        //형태소 정보만 얻어옴
        Set<String> morphes = this.getMorphes(morphScoredTagsMap);

        //각 형태소와 품사 정보를 lattice에 삽입
        for (String morph : morphes) {
            int beginIdx = curIndex - morph.length() + 1;
            int endIdx = curIndex + 1;

            //형태소에 대한 품사 및 점수(observation) 정보를 List 형태로 가져옴
            List<ScoredTag> scoredTags = morphScoredTagsMap.get(morph);
            for (ScoredTag scoredTag : scoredTags) {
                lattice.put(beginIdx, endIdx, morph, scoredTag.getTag(), scoredTag.getTagId(), scoredTag.getScore());
                //품사가 EC인 경우에 품사를 EF로 변환하여 lattice에 추가
                if (scoredTag.getTag().equals(SYMBOL.EC)) {
                    lattice.put(beginIdx, endIdx, morph, SYMBOL.EF, this.resources.getTable().getId(SYMBOL.EF), scoredTag.getScore());
                }
            }
        }
    }

    private Map<String, List<IrregularNode>> getIrregularNodes(FindContext<List<IrregularNode>> irregularFindContext, char jaso) {
        return this.resources.getIrrTrie().getTrieDictionary().get(irregularFindContext, jaso);
    }

    private void insertLattice(Lattice lattice, int beginIdx, int endIdx, String morph,
                               Tag tag, double score) {
        lattice.put(beginIdx, endIdx, morph, tag.getTag(), tag.getTagId(), score);
    }

    private Set<String> getMorphes(
            Map<String, List<ScoredTag>> morphScoredTagMap) {
        return morphScoredTagMap.keySet();
    }

    private Map<String, List<ScoredTag>> getMorphScoredTagsMap(FindContext<List<ScoredTag>> observationFindContext, char jaso) {
        return this.resources.getObservation().getTrieDictionary().get(observationFindContext, jaso);
    }

    private Map<String, List<ScoredTag>> getMorphScoredTagMapFromUserDic(FindContext<List<ScoredTag>> userDicFindContext, char jaso) {
        if (this.userDic == null) {
            return null;
        }
        if (userDicFindContext == null) {
            userDicFindContext = this.userDic.getTrieDictionary().newFindContext();
        }
        return this.userDic.getTrieDictionary().get(userDicFindContext, jaso);
    }

    public void load(String modelPath) {
        this.resources.load(modelPath);
    }

    //기분석 사전
    public void setFWDic(String filename) {
        try {
            CorpusParser corpusParser = new CorpusParser();
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            this.fwd = new HashMap<>();
            while ((line = br.readLine()) != null) {
                String[] tmp = line.split("\t");
                //주석이거나 format에 안 맞는 경우는 skip
                if (tmp.length != 2 || tmp[0].charAt(0) == '#') {
                    tmp = null;
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
                tmp = null;
                problemAnswerPair = null;
                convertAnswerList = null;
            }
            br.close();

            //init
            corpusParser = null;
            br = null;
            line = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUserDic(String userDic) {
        try {

            this.userDic = new Observation();
            BufferedReader br = new BufferedReader(new FileReader(userDic));
            String line = null;
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

                line = null;
                morph = null;
                pos = null;
            }
            br.close();

            //init
            br = null;
            line = null;
            this.userDic.getTrieDictionary().buildFailLink();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

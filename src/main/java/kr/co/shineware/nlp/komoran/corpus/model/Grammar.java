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
package kr.co.shineware.nlp.komoran.corpus.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import kr.co.shineware.nlp.komoran.interfaces.FileAccessible;
import kr.co.shineware.util.common.file.FileUtil;
import kr.co.shineware.util.common.string.StringUtil;

/**
 * This is model class of grammar. Grammar class implements {@FileAccessible}.
 * Therefore, this is can use like DB instead of save, load.
 *
 * @author Junsoo Shin
 * @version 2.1
 * @see {@link FileAccessible}
 * @since 2.1
 */
public class Grammar implements FileAccessible {

    private Map<String, Map<String, Integer>> grammar;

    /**
     * 문법 생성자
     */
    public Grammar() {
        grammar = new HashMap<String, Map<String, Integer>>();
    }

    /**
     * 문법 생성자로써 filename에 저장되어 있는 grammar를 로드
     *
     * @param filename
     */
    public Grammar(String filename) {
        this.load(filename);
    }

    /**
     * 현재 메모리에 있는 grammar를 반환<br>
     * Map<prevPos,Map<nextPos,tf>> 형태로 구성되어 있음
     *
     * @return
     */
    public Map<String, Map<String, Integer>> getGrammar() {
        return grammar;
    }

    /**
     * grammar에 prevPos to nextPos 문법 추가<br>
     * 기존에 존재하는 문법이면 문법의 출현 빈도수를 1만큼 증가 시킴
     *
     * @param prevPos
     * @param nextPos
     */
    public void append(String prevPos, String nextPos) {
        Map<String, Integer> nextMorphMap = grammar.get(prevPos);
        if (nextMorphMap == null) {
            nextMorphMap = new HashMap<String, Integer>();
        }
        Integer tf = nextMorphMap.get(nextPos);
        if (tf == null) {
            tf = 0;
        }
        tf++;
        nextMorphMap.put(nextPos, tf);
        grammar.put(prevPos, nextMorphMap);
    }

    /**
     * 현재 grammar에 존재하는 문법인지 검사
     *
     * @param prevPos
     * @param nextPos
     * @return
     */
    public boolean has(String prevPos, String nextPos) {
        Map<String, Integer> nextPosMap = grammar.get(prevPos);

        if (nextPosMap == null) {
            return false;
        } else {
            Integer tf = nextPosMap.get(nextPos);
            return tf != null;
        }
    }

    @Override
    public void save(String filename) {
        try {
            BufferedWriter bw = new BufferedWriter(
                    (new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8)));
//			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            Set<Entry<String, Map<String, Integer>>> entrySet = grammar.entrySet();
            for (Entry<String, Map<String, Integer>> entry : entrySet) {

                bw.write(entry.getKey());
                bw.write("\t");

                Set<String> nextMorphSet = entry.getValue().keySet();
                int morphSize = nextMorphSet.size();
                int count = 0;
                for (String nextMorph : nextMorphSet) {
                    bw.write(nextMorph);
                    bw.write(":");
                    Integer tf = entry.getValue().get(nextMorph);
                    bw.write("" + tf);
                    count++;
                    if (morphSize != count) {
                        bw.write(",");
                    }
                }
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(String filename) {
        grammar = new HashMap<String, Map<String, Integer>>();
        List<String> lines = FileUtil.load2List(filename);
        int size = lines.size();
        for (int i = 0; i < size; i++) {
            String line = lines.get(i);

            List<String> lineSplitedList = StringUtil.split(line, "\t");

            //previous POS
            String prevPos = lineSplitedList.get(0);

            //next POS parsing
            Map<String, Integer> nextPosMap = new HashMap<String, Integer>();
            String nextPosChunks = lineSplitedList.get(1);
            List<String> nextPosChunkList = StringUtil.split(nextPosChunks, ",");
            int posChunkListSize = nextPosChunkList.size();
            String commaPos = "";
            for (int j = 0; j < posChunkListSize; j++) {
                String nextPosTfPair = nextPosChunkList.get(j);
                if (nextPosTfPair.length() == 0) {
                    commaPos += ",";
                    continue;
                }
                int separatorIdx = nextPosTfPair.lastIndexOf(':');
//				String nextPos = nextPosTfPair.split(":")[0];
//				Integer tf = Integer.parseInt(nextPosTfPair.split(":")[1]);
                String nextPos;
                if (separatorIdx == 0) {
                    nextPos = "";
                } else {
                    nextPos = nextPosTfPair.substring(0, separatorIdx);
                }
                Integer tf = Integer.parseInt(nextPosTfPair.substring(separatorIdx + 1));
                nextPosMap.put(commaPos + nextPos, tf);
                commaPos = "";
            }

            //load to memory
            grammar.put(prevPos, nextPosMap);
        }
    }

}

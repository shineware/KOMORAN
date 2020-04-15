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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import kr.co.shineware.nlp.komoran.corpus.parser.CorpusParser;
import kr.co.shineware.nlp.komoran.corpus.parser.model.ProblemAnswerPair;
import kr.co.shineware.util.common.file.FileUtil;

public class FWDBuilder {

    private int threshold = 1;
    private CorpusParser parser;
    private Map<String, Map<String, Integer>> fwdMap;

    public FWDBuilder() {
        this.parser = new CorpusParser();
        fwdMap = new HashMap<>();
    }

    public void buildPath(String path, String suffix) {
        List<String> filenames = FileUtil.getFileNames(path);
        for (String filename : filenames) {
            if (filename.endsWith("." + suffix)) {
                System.out.println(filename);
                this.build(filename);
            }
        }
        filenames = null;
    }

    public void buildFromLine(List<String> lines) {
        try {
            for (String line : lines) {
                line = this.refineFormat(line);
                if (line.length() == 0) {
                    continue;
                }
                ProblemAnswerPair problemAnswerPair = parser.parse(line);
                String problem = problemAnswerPair.getProblem();
                String answer = problemAnswerPair.getAnswer();
                this.insertFWDMap(problem, answer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void build(String filename) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
//			BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = null;

            while ((line = br.readLine()) != null) {
                line = this.refineFormat(line);
                if (line.length() == 0) {
                    continue;
                }
                ProblemAnswerPair problemAnswerPair = parser.parse(line);
                String problem = problemAnswerPair.getProblem();
                String answer = problemAnswerPair.getAnswer();
                this.insertFWDMap(problem, answer);
            }

            br.close();
            br = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertFWDMap(String problem, String answer) {
        Map<String, Integer> answerTfMap = fwdMap.get(problem);
        if (answerTfMap == null) {
            answerTfMap = new HashMap<String, Integer>();
        }
        Integer tf = answerTfMap.get(answer);
        if (tf == null) {
            tf = 0;
        }
        tf++;
        answerTfMap.put(answer, tf);
        this.fwdMap.put(problem, answerTfMap);
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

    public void save(String filename, int threshold) {
        this.setThreshold(threshold);
        this.save(filename);
    }

    private void save(String filename) {
        try {
            BufferedWriter bw = new BufferedWriter(
                    (new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8)));
//			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            Set<Entry<String, Map<String, Integer>>> fwdEntrySet = fwdMap.entrySet();
            for (Entry<String, Map<String, Integer>> fwdEntry : fwdEntrySet) {
                String problem = fwdEntry.getKey();
                Map<String, Integer> answerTf = fwdEntry.getValue();
                if (answerTf.size() != 1) continue;
                Set<String> answerSet = answerTf.keySet();
                for (String answer : answerSet) {
                    int tf = answerTf.get(answer);
                    if (tf >= this.getThreshold()) {
                        bw.write(problem + "\t" + answer);
                        bw.newLine();
                    }
                }
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}

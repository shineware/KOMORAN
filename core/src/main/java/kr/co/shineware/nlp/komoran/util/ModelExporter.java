package kr.co.shineware.nlp.komoran.util;

import kr.co.shineware.nlp.komoran.constant.FILENAME;
import kr.co.shineware.nlp.komoran.core.model.DoubleArrayAhoCorasick;
import kr.co.shineware.nlp.komoran.core.model.Resources;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularNode;
import kr.co.shineware.nlp.komoran.modeler.model.Observation;
import kr.co.shineware.nlp.komoran.modeler.model.Transition;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.model.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * KOMORAN 모델을 Rust가 읽을 수 있는 포맷으로 내보내는 유틸리티.
 *
 * 출력 파일:
 * - pos.table (그대로 복사)
 * - observation.txt: morph(jaso)\ttagId\tscore 형태
 * - transition.bin: 45x45 double 배열 (little-endian binary)
 * - irregular.txt: irr_key\tlastMorph\tfirstPosId\tlastPosId\tinnerScore\ttokens...
 */
public class ModelExporter {

    public static void main(String[] args) throws Exception {
        String modelPath;
        String outputPath;

        if (args.length >= 2) {
            modelPath = args[0];
            outputPath = args[1];
        } else {
            // 기본값: 내장 모델 사용
            modelPath = null;
            outputPath = "rust/komoran-rs/model";
        }

        File outDir = new File(outputPath);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        Resources resources = new Resources();
        if (modelPath != null) {
            resources.load(modelPath);
        } else {
            // 내장 모델 로드
            resources.init();
            String delimiter = "/";
            String defaultModelPath = FILENAME.DEFAULT_MODEL_PATH;
            ClassLoader cl = ModelExporter.class.getClassLoader();
            try (InputStream posTableFile = cl.getResourceAsStream(defaultModelPath + delimiter + FILENAME.POS_TABLE);
                 InputStream irrModelFile = cl.getResourceAsStream(defaultModelPath + delimiter + FILENAME.IRREGULAR_MODEL);
                 InputStream observationFile = cl.getResourceAsStream(defaultModelPath + delimiter + FILENAME.OBSERVATION);
                 InputStream transitionFile = cl.getResourceAsStream(defaultModelPath + delimiter + FILENAME.TRANSITION)) {
                resources.loadPosTable(posTableFile);
                resources.loadIrregular(irrModelFile);
                resources.loadObservation(observationFile);
                resources.loadTransition(transitionFile);
            }
        }

        // 1. pos.table 내보내기
        exportPosTable(resources, outputPath);

        // 2. observation 내보내기
        exportObservation(resources, outputPath);

        // 3. transition 내보내기
        exportTransition(resources, outputPath);

        // 4. irregular 내보내기
        exportIrregular(resources, outputPath);

        System.out.println("Model exported to: " + outputPath);
    }

    private static void exportPosTable(Resources resources, String outputPath) throws IOException {
        // pos.table은 이미 텍스트 형식이므로 그대로 복사
        Map<Integer, String> idPosTable = resources.getTable().getIdPosTable();
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputPath + "/pos.table"), StandardCharsets.UTF_8))) {
            for (Map.Entry<Integer, String> entry : idPosTable.entrySet()) {
                bw.write(entry.getValue() + "\t" + entry.getKey());
                bw.newLine();
            }
        }
        System.out.println("pos.table exported: " + idPosTable.size() + " tags");
    }

    private static void exportObservation(Resources resources, String outputPath) throws Exception {
        DoubleArrayAhoCorasick<List<ScoredTag>> trie = resources.getObservation().getTrieDictionary();

        // Reflection으로 내부 데이터에 접근
        java.lang.reflect.Field outputField = DoubleArrayAhoCorasick.class.getDeclaredField("output");
        outputField.setAccessible(true);
        Object[] outputs = (Object[]) outputField.get(trie);

        java.lang.reflect.Field keyCacheField = DoubleArrayAhoCorasick.class.getDeclaredField("keyCache");
        keyCacheField.setAccessible(true);
        String[] keyCache = (String[]) keyCacheField.get(trie);

        java.lang.reflect.Field childKeysField = DoubleArrayAhoCorasick.class.getDeclaredField("childKeys");
        childKeysField.setAccessible(true);
        int[][] childKeys = (int[][]) childKeysField.get(trie);

        java.lang.reflect.Field childValuesField = DoubleArrayAhoCorasick.class.getDeclaredField("childValues");
        childValuesField.setAccessible(true);
        int[][] childValues = (int[][]) childValuesField.get(trie);

        java.lang.reflect.Field failField = DoubleArrayAhoCorasick.class.getDeclaredField("fail");
        failField.setAccessible(true);
        int[] fail = (int[]) failField.get(trie);

        java.lang.reflect.Field nodeCountField = DoubleArrayAhoCorasick.class.getDeclaredField("nodeCount");
        nodeCountField.setAccessible(true);
        int nodeCount = (int) nodeCountField.get(trie);

        // Binary 형식으로 Trie 구조 내보내기
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputPath + "/observation.dat")))) {
            // nodeCount
            dos.writeInt(nodeCount);

            // fail array
            for (int i = 0; i < nodeCount; i++) {
                dos.writeInt(fail[i]);
            }

            // childKeys & childValues
            for (int i = 0; i < nodeCount; i++) {
                if (childKeys[i] == null) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(childKeys[i].length);
                    for (int j = 0; j < childKeys[i].length; j++) {
                        dos.writeInt(childKeys[i][j]);
                        dos.writeInt(childValues[i][j]);
                    }
                }
            }

            // outputs (scored tags)
            for (int i = 0; i < nodeCount; i++) {
                if (outputs[i] == null) {
                    dos.writeInt(0);
                } else {
                    @SuppressWarnings("unchecked")
                    List<ScoredTag> tags = (List<ScoredTag>) outputs[i];
                    dos.writeInt(tags.size());
                    for (ScoredTag tag : tags) {
                        dos.writeInt(tag.getTagId());
                        dos.writeDouble(tag.getScore());
                        dos.writeUTF(tag.getTag());
                    }
                }
            }

            // keyCache
            for (int i = 0; i < nodeCount; i++) {
                if (keyCache == null || i >= keyCache.length || keyCache[i] == null) {
                    dos.writeUTF("");
                } else {
                    dos.writeUTF(keyCache[i]);
                }
            }
        }

        // Count entries
        int entryCount = 0;
        for (int i = 0; i < nodeCount; i++) {
            if (outputs[i] != null) entryCount++;
        }
        System.out.println("observation.dat exported: " + nodeCount + " nodes, " + entryCount + " entries");
    }

    private static void exportTransition(Resources resources, String outputPath) throws Exception {
        Transition transition = resources.getTransition();

        java.lang.reflect.Field matrixField = Transition.class.getDeclaredField("scoreMatrix");
        matrixField.setAccessible(true);
        double[][] matrix = (double[][]) matrixField.get(transition);

        int size = matrix.length;
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputPath + "/transition.dat")))) {
            dos.writeInt(size);
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    dos.writeDouble(matrix[i][j]);
                }
            }
        }
        System.out.println("transition.dat exported: " + size + "x" + size + " matrix");
    }

    @SuppressWarnings("unchecked")
    private static void exportIrregular(Resources resources, String outputPath) throws Exception {
        DoubleArrayAhoCorasick<List<IrregularNode>> trie = resources.getIrrTrie().getTrieDictionary();

        java.lang.reflect.Field outputField = DoubleArrayAhoCorasick.class.getDeclaredField("output");
        outputField.setAccessible(true);
        Object[] outputs = (Object[]) outputField.get(trie);

        java.lang.reflect.Field keyCacheField = DoubleArrayAhoCorasick.class.getDeclaredField("keyCache");
        keyCacheField.setAccessible(true);
        String[] keyCache = (String[]) keyCacheField.get(trie);

        java.lang.reflect.Field childKeysField = DoubleArrayAhoCorasick.class.getDeclaredField("childKeys");
        childKeysField.setAccessible(true);
        int[][] childKeys = (int[][]) childKeysField.get(trie);

        java.lang.reflect.Field childValuesField = DoubleArrayAhoCorasick.class.getDeclaredField("childValues");
        childValuesField.setAccessible(true);
        int[][] childValues = (int[][]) childValuesField.get(trie);

        java.lang.reflect.Field failField = DoubleArrayAhoCorasick.class.getDeclaredField("fail");
        failField.setAccessible(true);
        int[] fail = (int[]) failField.get(trie);

        java.lang.reflect.Field nodeCountField = DoubleArrayAhoCorasick.class.getDeclaredField("nodeCount");
        nodeCountField.setAccessible(true);
        int nodeCount = (int) nodeCountField.get(trie);

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputPath + "/irregular.dat")))) {
            // nodeCount
            dos.writeInt(nodeCount);

            // fail array
            for (int i = 0; i < nodeCount; i++) {
                dos.writeInt(fail[i]);
            }

            // childKeys & childValues
            for (int i = 0; i < nodeCount; i++) {
                if (childKeys[i] == null) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(childKeys[i].length);
                    for (int j = 0; j < childKeys[i].length; j++) {
                        dos.writeInt(childKeys[i][j]);
                        dos.writeInt(childValues[i][j]);
                    }
                }
            }

            // outputs (irregular nodes)
            for (int i = 0; i < nodeCount; i++) {
                if (outputs[i] == null) {
                    dos.writeInt(0);
                } else {
                    List<IrregularNode> nodes = (List<IrregularNode>) outputs[i];
                    dos.writeInt(nodes.size());
                    for (IrregularNode node : nodes) {
                        dos.writeUTF(node.getLastMorph() != null ? node.getLastMorph() : "");
                        dos.writeInt(node.getFirstPosId());
                        dos.writeInt(node.getLastPosId());
                        dos.writeDouble(node.getInnerScore());
                        dos.writeUTF(node.getMorphFormat() != null ? node.getMorphFormat() : "");
                        List<Pair<String, Integer>> tokens = node.getTokens();
                        if (tokens == null) {
                            dos.writeInt(0);
                        } else {
                            dos.writeInt(tokens.size());
                            for (Pair<String, Integer> token : tokens) {
                                dos.writeUTF(token.getFirst());
                                dos.writeInt(token.getSecond());
                            }
                        }
                    }
                }
            }

            // keyCache
            for (int i = 0; i < nodeCount; i++) {
                if (keyCache == null || i >= keyCache.length || keyCache[i] == null) {
                    dos.writeUTF("");
                } else {
                    dos.writeUTF(keyCache[i]);
                }
            }
        }

        int entryCount = 0;
        for (int i = 0; i < nodeCount; i++) {
            if (outputs[i] != null) entryCount++;
        }
        System.out.println("irregular.dat exported: " + nodeCount + " nodes, " + entryCount + " entries");
    }
}

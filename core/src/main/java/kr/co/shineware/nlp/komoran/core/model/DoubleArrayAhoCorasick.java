package kr.co.shineware.nlp.komoran.core.model;

import kr.co.shineware.ds.aho_corasick.AhoCorasickDictionary;
import kr.co.shineware.ds.aho_corasick.model.AhoCorasickNode;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 배열 기반 Aho-Corasick 오토마톤.
 * 기존 AhoCorasickDictionary를 로드한 후, BFS 순서로 노드를 배열에 펼쳐
 * 캐시 친화적인 탐색을 구현한다.
 *
 * 각 노드의 자식 탐색은 compact한 (char→nodeId) 해시맵으로 O(1) 수행.
 * fail link도 int 배열로 관리하여 포인터 체이싱을 최소화한다.
 */
public class DoubleArrayAhoCorasick<V> {

    // 노드 ID → fail link 노드 ID
    private int[] fail;
    // 노드 ID → 출력값
    private V[] output;
    // 노드 ID → 자식 매핑 (encoded char → child node ID)
    private int[][] childKeys;   // 각 노드의 자식 문자 코드 (정렬됨)
    private int[][] childValues; // 대응하는 자식 노드 ID
    private int nodeCount;

    // 출력이 있는 노드의 키를 미리 캐싱
    private String[] keyCache;

    // 기존 AhoCorasickDictionary (로딩/저장용)
    private AhoCorasickDictionary<V> legacyDic;

    public DoubleArrayAhoCorasick() {
        this.legacyDic = new AhoCorasickDictionary<>();
    }

    /**
     * 기존 AhoCorasickDictionary에서 배열 기반 오토마톤을 빌드한다.
     */
    @SuppressWarnings("unchecked")
    public void buildFrom(AhoCorasickDictionary<V> dic) {
        this.legacyDic = dic;

        AhoCorasickNode<V> root = getRoot(dic);
        if (root == null) {
            initEmpty();
            return;
        }

        // 1단계: BFS로 모든 노드에 ID 부여
        Map<AhoCorasickNode<V>, Integer> nodeIdMap = new IdentityHashMap<>();
        List<AhoCorasickNode<V>> nodeList = new ArrayList<>();

        Queue<AhoCorasickNode<V>> bfs = new ArrayDeque<>();
        bfs.add(root);
        nodeIdMap.put(root, 0);
        nodeList.add(root);

        while (!bfs.isEmpty()) {
            AhoCorasickNode<V> node = bfs.poll();
            AhoCorasickNode<V>[] children = node.getChildren();
            if (children != null) {
                for (AhoCorasickNode<V> child : children) {
                    if (child != null && !nodeIdMap.containsKey(child)) {
                        int id = nodeList.size();
                        nodeIdMap.put(child, id);
                        nodeList.add(child);
                        bfs.add(child);
                    }
                }
            }
        }

        this.nodeCount = nodeList.size();

        // 2단계: 배열 빌드
        this.output = (V[]) new Object[nodeCount];
        this.childKeys = new int[nodeCount][];
        this.childValues = new int[nodeCount][];

        for (int i = 0; i < nodeCount; i++) {
            AhoCorasickNode<V> node = nodeList.get(i);
            this.output[i] = node.getValue();

            AhoCorasickNode<V>[] children = node.getChildren();
            if (children != null && children.length > 0) {
                int len = children.length;
                int[] keys = new int[len];
                int[] vals = new int[len];
                for (int j = 0; j < len; j++) {
                    keys[j] = JasoEncoder.encode(children[j].getKey());
                    vals[j] = nodeIdMap.get(children[j]);
                }
                // 키 기준 정렬 (이진 탐색용)
                sortByKeys(keys, vals);
                this.childKeys[i] = keys;
                this.childValues[i] = vals;
            }
        }

        // 3단계: fail link 빌드 (BFS 순서 활용)
        this.fail = new int[nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            AhoCorasickNode<V> node = nodeList.get(i);
            AhoCorasickNode<V> failNode = node.getFailNode();
            if (failNode != null && nodeIdMap.containsKey(failNode)) {
                this.fail[i] = nodeIdMap.get(failNode);
            } else {
                this.fail[i] = 0; // root
            }
        }

        // 4단계: 부모 정보 및 키 캐시 빌드
        buildParentInfo();
        this.keyCache = new String[nodeCount];
        for (int i = 1; i < nodeCount; i++) {
            if (this.output[i] != null) {
                this.keyCache[i] = reconstructKeyFromParent(i);
            }
        }
    }

    private String reconstructKeyFromParent(int state) {
        StringBuilder sb = new StringBuilder();
        int s = state;
        while (s != 0 && parentNode[s] != -1) {
            sb.append(decodeChar(parentEdge[s]));
            s = parentNode[s];
        }
        return sb.reverse().toString();
    }

    private void sortByKeys(int[] keys, int[] vals) {
        // 간단한 삽입 정렬 (자식 수가 적으므로 효율적)
        for (int i = 1; i < keys.length; i++) {
            int key = keys[i];
            int val = vals[i];
            int j = i - 1;
            while (j >= 0 && keys[j] > key) {
                keys[j + 1] = keys[j];
                vals[j + 1] = vals[j];
                j--;
            }
            keys[j + 1] = key;
            vals[j + 1] = val;
        }
    }

    @SuppressWarnings("unchecked")
    private AhoCorasickNode<V> getRoot(AhoCorasickDictionary<V> dic) {
        try {
            Field rootField = AhoCorasickDictionary.class.getDeclaredField("root");
            rootField.setAccessible(true);
            return (AhoCorasickNode<V>) rootField.get(dic);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access root node", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void initEmpty() {
        this.nodeCount = 1;
        this.fail = new int[1];
        this.output = (V[]) new Object[1];
        this.childKeys = new int[1][];
        this.childValues = new int[1][];
    }

    /**
     * 자식 노드 탐색: 이진 탐색으로 O(log n)
     */
    private int getChild(int nodeId, int encodedChar) {
        int[] keys = childKeys[nodeId];
        if (keys == null) return -1;
        int lo = 0, hi = keys.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (keys[mid] == encodedChar) return childValues[nodeId][mid];
            else if (keys[mid] < encodedChar) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    /**
     * 상태 전이: fail link를 따라가면서 전이 가능한 상태를 찾는다.
     */
    public int advance(int state, char ch) {
        int c = JasoEncoder.encode(ch);
        while (state != 0) {
            int child = getChild(state, c);
            if (child != -1) return child;
            state = fail[state];
        }
        int child = getChild(0, c);
        return child != -1 ? child : 0;
    }

    /**
     * Aho-Corasick 스트리밍 검색.
     * DAFindContext 내부의 재사용 맵을 사용하여 GC 압박을 줄인다.
     * 반환된 맵은 다음 get() 호출 시 내용이 덮어씌워지므로 즉시 소비해야 한다.
     * DAFindContext는 Lattice별로 생성되므로 스레드 안전하다.
     */
    public Map<String, V> get(DAFindContext context, char ch) {
        int state = advance(context.getState(), ch);
        context.setState(state);

        Map<String, V> result = context.getReusableResult();
        result.clear();

        int s = state;
        while (s != 0) {
            if (output[s] != null) {
                result.put(keyCache[s], output[s]);
            }
            s = fail[s];
        }
        return result;
    }

    private int[] parentNode;
    private int[] parentEdge;

    private void buildParentInfo() {
        if (parentNode != null) return;
        parentNode = new int[nodeCount];
        parentEdge = new int[nodeCount];
        Arrays.fill(parentNode, -1);

        for (int i = 0; i < nodeCount; i++) {
            int[] keys = childKeys[i];
            if (keys == null) continue;
            for (int j = 0; j < keys.length; j++) {
                int childId = childValues[i][j];
                parentNode[childId] = i;
                parentEdge[childId] = keys[j];
            }
        }
    }

    private static char decodeChar(int code) {
        return DecoderCache.decode(code);
    }

    public V getValue(String key) {
        if (childKeys == null) {
            return legacyDic.getValue(key);
        }
        int state = 0;
        for (int i = 0; i < key.length(); i++) {
            int c = JasoEncoder.encode(key.charAt(i));
            int child = getChild(state, c);
            if (child == -1) return null;
            state = child;
        }
        return output[state];
    }

    public V getValue(char ch) {
        if (childKeys == null) {
            return legacyDic.getValue(String.valueOf(ch));
        }
        int c = JasoEncoder.encode(ch);
        int child = getChild(0, c);
        if (child == -1) return null;
        return output[child];
    }

    public boolean hasChild(char[] chars) {
        if (childKeys == null) {
            return legacyDic.hasChild(chars);
        }
        int state = 0;
        for (char ch : chars) {
            int c = JasoEncoder.encode(ch);
            int child = getChild(state, c);
            if (child == -1) return false;
            state = child;
        }
        return true;
    }

    public DAFindContext newFindContext() {
        return new DAFindContext();
    }

    @SuppressWarnings("rawtypes")
    public static class DAFindContext {
        private int state = 0;
        private final Map reusableResult = new HashMap<>();

        public int getState() { return state; }
        public void setState(int state) { this.state = state; }
        public void reset() { this.state = 0; }

        @SuppressWarnings("unchecked")
        <V> Map<String, V> getReusableResult() { return reusableResult; }
    }

    private static class DecoderCache {
        private static char[] reverseMap;
        private static boolean built = false;

        static synchronized char decode(int code) {
            if (!built || code >= reverseMap.length) {
                buildReverseMap();
            }
            return (code < reverseMap.length) ? reverseMap[code] : '?';
        }

        private static void buildReverseMap() {
            int maxCode = JasoEncoder.getAlphabetSize();
            reverseMap = new char[maxCode];
            try {
                Field f = JasoEncoder.class.getDeclaredField("ENCODE_TABLE");
                f.setAccessible(true);
                int[] table = (int[]) f.get(null);
                for (int i = 0; i < table.length; i++) {
                    if (table[i] >= 0 && table[i] < maxCode) {
                        reverseMap[table[i]] = (char) i;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to build reverse map", e);
            }
            built = true;
        }
    }

    // === Legacy compatibility methods ===

    public AhoCorasickDictionary<V> getLegacyDictionary() {
        return legacyDic;
    }

    public void put(String key, V value) {
        legacyDic.put(key, value);
    }

    public void buildFailLink() {
        legacyDic.buildFailLink();
        buildFrom(legacyDic);
    }

    public void save(String filename) { legacyDic.save(filename); }
    public void save(java.io.File file) { legacyDic.save(file); }

    public void load(String filename) {
        legacyDic.load(filename);
        buildFrom(legacyDic);
    }

    public void load(java.io.File file) {
        legacyDic.load(file);
        buildFrom(legacyDic);
    }

    public void load(java.io.InputStream is) {
        legacyDic.load(is);
        buildFrom(legacyDic);
    }
}

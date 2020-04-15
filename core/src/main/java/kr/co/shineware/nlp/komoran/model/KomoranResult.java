package kr.co.shineware.nlp.komoran.model;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.model.LatticeNode;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.model.Pair;

import java.util.*;

/**
 * Komoran을 통해 분석된 결과를 저장하고 있는 객체입니다.
 */
public class KomoranResult {

    private List<LatticeNode> resultNodeList;
    private String jasoUnits;
    private KoreanUnitParser parser = new KoreanUnitParser();

    /**
     * KomoranResult 생성자 입니다. </p>
     * Komoran 내부에서 사용되며 대부분의 경우에 외부에서 사용되지 않습니다.
     *
     * @param latticeNode
     * @param jasoUnits
     */
    public KomoranResult(List<LatticeNode> latticeNode, String jasoUnits) {
        this.resultNodeList = latticeNode;
        this.jasoUnits = jasoUnits;
    }

    /**
     * 분석 결과를 LatticeNode 리스트로 반환합니다.
     * @return 각 형태소의 LatticeNode List
     */
    public List<LatticeNode> getResultNodeList() {
        return this.resultNodeList;
    }

    /**
     * 형태소 분석의 입력 문장을 jaso 단위로 반환합니다.
     * @return jaso 단위로 변환된 String
     */
    public String getJasoUnits() {
        return this.jasoUnits;
    }

    /**
     * 분석 결과 중 명사류(NNG, NNP)만 반환합니다.
     * @return NNG, NNP에 해당하는 형태소가 포함된 List
     */
    public List<String> getNouns() {
        return this.getMorphesByTags(Arrays.asList(SYMBOL.NNG, SYMBOL.NNP));
    }

    /**
     * 분석 결과 중 원하는 품사에 해당하는 형태소만 추출하여 반환합니다.
     * @param str 추출 대상 품사
     * @return 품사에 해당하는 형태소만 추출된 List
     */
    public List<String> getMorphesByTags(String... str) {
        return this.getMorphesByTags(Arrays.asList(str));
    }

    /**
     * 분석 결과 중 원하는 품사에 해당하는 형태소만 추출하여 반환합니다. </p>
     * @param targetPosCollection 추출 대상 품사가 담긴 List
     * @return 품사에 해당하는 형태소만 추출된 List
     */
    public List<String> getMorphesByTags(Collection<String> targetPosCollection) {

        Set<String> targetPosSet = new HashSet<>(targetPosCollection);

        List<String> morphList = new ArrayList<>();
        for (LatticeNode latticeNode : resultNodeList) {
            if (targetPosSet.contains(latticeNode.getTag())) {
                morphList.add(parser.combine(latticeNode.getMorphTag().getMorph()));
            }
        }
        return morphList;
    }

    /**
     * 형태소 분석 결과를 plainText 형태로 반환합니다. </p>
     * plainText 결과는 아래와 같습니다. </p>
     * <pre>
     *     감기/NNG 는/JX 자주/MAG
     * </pre>
     * @return 형태소 분석 결과의 plainText String
     */
    public String getPlainText() {
        StringBuilder result = new StringBuilder();
        for (LatticeNode latticeNode : resultNodeList) {
            if (latticeNode.getMorphTag().getTag().equals(SYMBOL.EOE)) {
                continue;
            }
            if (latticeNode.getTag().equals(SYMBOL.NA)) {
                result.append(latticeNode.getMorphTag().getMorph()).append("/").append(latticeNode.getMorphTag().getTag()).append(" ");
            } else {
                result.append(parser.combine(latticeNode.getMorphTag().getMorph())).append("/").append(latticeNode.getTag()).append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * 형태소 분석 결과를 Token List 형태소 반환합니다. </p>
     * Token에는 아래와 같은 정보가 포함되어 있습니다. </p>
     * <pre>
     *  private String morph; //형태소
     * 	private String pos; //품사
     * 	private int beginIndex; //입력 문장 내 시작 위치
     * 	private int endIndex; //입력 문장 내 끝 위치
     * </pre>
     * @return 형태소 분석 결과의 Token List
     */
    public List<Token> getTokenList() {
        List<Pair<Integer, Integer>> syllableAreaList = parser.getSyllableAreaList(this.jasoUnits);
        List<Token> tokenList = new ArrayList<>();
        int prevBeginIdx = 0;
        for (LatticeNode latticeNode : resultNodeList) {
            if (latticeNode.getMorphTag().getTag().equals(SYMBOL.EOE)) {
                continue;
            }
            //불규칙이거나 multi token 기분석 사전인 경우
            if (latticeNode.getBeginIdx() < 0) {
                latticeNode.setBeginIdx(prevBeginIdx);
            }
            Pair<Integer, Integer> syllableArea = this.getSyllableArea(latticeNode.getBeginIdx(), latticeNode.getEndIdx(), syllableAreaList);

            tokenList.add(new Token(parser.combine(latticeNode.getMorphTag().getMorph()),
                    parser.combine(latticeNode.getTag()), syllableArea.getFirst(), syllableArea.getSecond()));

            prevBeginIdx = latticeNode.getBeginIdx();
        }
        return tokenList;
    }

    private Pair<Integer, Integer> getSyllableArea(int jasoBeginIdx, int jasoEndIdx,
                                                   List<Pair<Integer, Integer>> syllableAreaList) {
        Pair<Integer, Integer> syllableAreaPair = new Pair<>();
        for (int i = 0; i < syllableAreaList.size(); i++) {

            if (syllableAreaList.get(i).getFirst() <= jasoBeginIdx && jasoBeginIdx <= syllableAreaList.get(i).getSecond()) {
                syllableAreaPair.setFirst(i);
            }
            if (syllableAreaList.get(i).getFirst() < jasoEndIdx && jasoEndIdx <= syllableAreaList.get(i).getSecond()) {
                syllableAreaPair.setSecond(i + 1);
            }
        }
        return syllableAreaPair;
    }

    /**
     * 분석 결과를 형태소, 품사 Pair의 List 형태로 반환합니다.
     * @return 형태소, 품사 정보가 담긴 Pair의 List
     */
    public List<Pair<String, String>> getList() {
        List<Pair<String, String>> resultList = new ArrayList<>();
        for (LatticeNode latticeNode : resultNodeList) {
            if (latticeNode.getMorphTag().getTag().equals(SYMBOL.EOE)) {
                continue;
            }
            resultList.add(new Pair<>(parser.combine(latticeNode.getMorphTag().getMorph()), latticeNode.getTag()));
        }
        return resultList;
    }
}

package kr.co.shineware.nlp.komoran.model;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.model.LatticeNode;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.model.Pair;

import java.util.*;


public class KomoranResult {

    private List<LatticeNode> resultNodeList;
    private String jasoUnits;
    private KoreanUnitParser parser = new KoreanUnitParser();

    public KomoranResult(List<LatticeNode> latticeNode, String jasoUnits) {
        this.resultNodeList = latticeNode;
        this.jasoUnits = jasoUnits;
    }

    public List<String> getNouns() {
        return this.getMorphesByTags(Arrays.asList(SYMBOL.NNG, SYMBOL.NNP));
    }

    public List<String> getMorphesByTags(String... str) {
        return this.getMorphesByTags(Arrays.asList(str));
    }

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

    public String getPlainText() {
        StringBuilder result = new StringBuilder();
        for (LatticeNode latticeNode : resultNodeList) {
            if (latticeNode.getMorphTag().getTag().equals(SYMBOL.END)) {
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

    public List<Token> getTokenList() {
        List<Pair<Integer, Integer>> syllableAreaList = parser.getSyllableAreaList(this.jasoUnits);
        List<Token> tokenList = new ArrayList<>();
        int prevBeginIdx = 0;
        for (LatticeNode latticeNode : resultNodeList) {
            if (latticeNode.getMorphTag().getTag().equals(SYMBOL.END)) {
                continue;
            }
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

    public List<Pair<String, String>> getList() {
        List<Pair<String, String>> resultList = new ArrayList<>();
        for (LatticeNode latticeNode : resultNodeList) {
            if (latticeNode.getMorphTag().getTag().equals(SYMBOL.END)) {
                continue;
            }
            resultList.add(new Pair<>(parser.combine(latticeNode.getMorphTag().getMorph()), latticeNode.getTag()));
        }
        return resultList;
    }
}

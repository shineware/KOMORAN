package kr.co.shineware.nlp.komoran.core.model;

import kr.co.shineware.nlp.komoran.constant.SEJONGTAGS;
import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.model.combinationrules.CombinationRuleChecker;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.modeler.model.*;
import kr.co.shineware.util.common.collection.MapUtil;
import kr.co.shineware.util.common.model.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lattice {

    private static final int IRREGULAR_POS_ID = -1;
    private static final int INITIAL_POSITIVE_CAPACITY = 256;

    // Positive indices: array-based (index 0 ~ capacity-1)
    private List<LatticeNode>[] positiveNodes;
    private int positiveCapacity;

    // Negative indices: HashMap (for irrIdx)
    private Map<Integer, List<LatticeNode>> negativeNodes;

    private PosTable posTable;
    private Transition transition;
    private int lastIdx = -1;
    private int irrIdx = 0;
    private Observation observation;
    private Observation userDicObservation;
    private IrregularTrie irregularTrie;

    private DoubleArrayAhoCorasick.DAFindContext observationFindContext;
    private DoubleArrayAhoCorasick.DAFindContext irregularFindContext;
    private DoubleArrayAhoCorasick.DAFindContext userDicFindContext;

    private final CombinationRuleChecker combinationRuleChecker;

    private double prevMaxScore;
    private LatticeNode prevMaxNode;
    private int prevMaxIdx;
    private int nbest;
    private int beamWidth; // 0 = no pruning (full Viterbi)

    public Lattice(Resources resource, Observation userDic) {
        this(resource, userDic, 1, (prevMorph, prevTagId, morph, tagId) -> true);
    }

    @SuppressWarnings("unchecked")
    public Lattice(Resources resource, Observation userDic, int nbest, CombinationRuleChecker combinationRuleChecker) {
        this(resource, userDic, nbest, combinationRuleChecker, 0);
    }

    @SuppressWarnings("unchecked")
    public Lattice(Resources resource, Observation userDic, int nbest, CombinationRuleChecker combinationRuleChecker, int beamWidth) {
        this.setPosTable(resource.getTable());
        this.setTransition(resource.getTransition());
        this.setObservation(resource.getObservation());
        this.setIrregularTrie(resource.getIrrTrie());
        this.setUserDicObservation(userDic);
        this.positiveCapacity = INITIAL_POSITIVE_CAPACITY;
        this.positiveNodes = new List[positiveCapacity];
        this.negativeNodes = new HashMap<>();
        this.init();
        this.makeNewContexts();
        this.nbest = nbest;
        this.combinationRuleChecker = combinationRuleChecker;
        this.beamWidth = beamWidth;
    }

    private void setUserDicObservation(Observation userDic) {
        this.userDicObservation = userDic;
    }

    private void setIrregularTrie(IrregularTrie irrTrie) {
        this.irregularTrie = irrTrie;
    }

    private void makeNewContexts() {
        this.observationFindContext = this.observation.getTrieDictionary().newFindContext();
        this.irregularFindContext = this.irregularTrie.getTrieDictionary().newFindContext();
        if (this.userDicObservation != null) {
            this.userDicFindContext = this.userDicObservation.getTrieDictionary().newFindContext();
        }
    }

    public Map<String, List<ScoredTag>> retrievalObservation(char jaso) {
        return this.observation.getTrieDictionary().get(this.observationFindContext, jaso);
    }

    public Map<String, List<IrregularNode>> retrievalIrregularNodes(char jaso) {
        return this.irregularTrie.getTrieDictionary().get(this.irregularFindContext, jaso);
    }

    public Map<String, List<ScoredTag>> retrievalUserDicObservation(char jaso) {
        if (this.userDicObservation == null) {
            return null;
        }

        return this.userDicObservation.getTrieDictionary().get(this.userDicFindContext, jaso);
    }

    @SuppressWarnings("unchecked")
    private void init() {
        // Reset arrays
        for (int i = 0; i < positiveCapacity; i++) {
            positiveNodes[i] = null;
        }
        negativeNodes.clear();
        irrIdx = 0;

        List<LatticeNode> latticeNodes = new ArrayList<>();
        latticeNodes.add(this.makeStartNode());

        this.putNodeList(0, latticeNodes);
    }

    private LatticeNode makeStartNode() {
        return new LatticeNode(-1, 0, SYMBOL.BOE, SYMBOL.BOE, SEJONGTAGS.BOE_ID, 0);
    }

    @SuppressWarnings("unchecked")
    private void ensurePositiveCapacity(int index) {
        if (index >= positiveCapacity) {
            int newCapacity = Math.max(positiveCapacity * 2, index + 1);
            List<LatticeNode>[] newArray = new List[newCapacity];
            System.arraycopy(positiveNodes, 0, newArray, 0, positiveCapacity);
            positiveNodes = newArray;
            positiveCapacity = newCapacity;
        }
    }

    private void putNodeList(int index, List<LatticeNode> nodes) {
        if (index >= 0) {
            ensurePositiveCapacity(index);
            positiveNodes[index] = nodes;
        } else {
            negativeNodes.put(index, nodes);
        }
    }

    public List<LatticeNode> getNodeList(int index) {
        if (index >= 0) {
            if (index < positiveCapacity) {
                return positiveNodes[index];
            }
            return null;
        }
        return negativeNodes.get(index);
    }

    //기분석 사전을 위한 lattice put
    public void put(int beginIdx, int endIdx,
                    List<Pair<String, String>> fwdResultList) {

        if (fwdResultList.size() == 1) {
            Pair<String, String> morphPosPair = fwdResultList.get(0);
            this.put(beginIdx, endIdx, morphPosPair.getFirst(), morphPosPair.getSecond(), this.posTable.getId(morphPosPair.getSecond()), 0.0);
        }

        else {
            for (int i = 0; i < fwdResultList.size(); i++) {
                Pair<String, String> morphPosPair = fwdResultList.get(i);
                if (i == 0) {
                    this.put(beginIdx, irrIdx - 1, morphPosPair.getFirst(), morphPosPair.getSecond(), this.posTable.getId(morphPosPair.getSecond()), 0.0);
                } else if (i == fwdResultList.size() - 1) {
                    this.put(irrIdx, endIdx, morphPosPair.getFirst(), morphPosPair.getSecond(), this.posTable.getId(morphPosPair.getSecond()), 0.0);
                } else {
                    this.put(irrIdx, irrIdx - 1, morphPosPair.getFirst(), morphPosPair.getSecond(), this.posTable.getId(morphPosPair.getSecond()), 0.0);
                }
                irrIdx--;
            }
        }
    }

    public void put(int beginIdx, int endIdx, IrregularNode irregularNode) {
        List<LatticeNode> prevLatticeNodes = this.getNodeList(beginIdx);

        if (prevLatticeNodes != null) {
            this.prevMaxIdx = -1;
            this.prevMaxNode = null;
            this.prevMaxScore = Double.NEGATIVE_INFINITY;
            this.getMaxTransitionIdxFromPrevNodes(prevLatticeNodes, irregularNode.getFirstPosId());

            if (this.prevMaxNode != null) {
                List<Pair<String, Integer>> irregularTokens = irregularNode.getTokens();
                int prevMaxIdx = this.prevMaxIdx;
                double prevMaxScore = this.prevMaxScore;
                this.putIrregularExtendTokens(beginIdx, endIdx, irregularTokens, prevMaxScore, prevMaxIdx);
            }
        }
    }

    private void putIrregularExtendTokens(int beginIdx, int endIdx,
                                          List<Pair<String, Integer>> irregularTokens, double prevMaxScore, int prevMaxIdx) {

        if (irregularTokens == null || irregularTokens.size() == 0) {
            return;
        }

    	Pair<String, Integer> morphPosPair;
    	List<ScoredTag> scoredTags;

    	if (irregularTokens.size() == 1) {
        	morphPosPair = irregularTokens.get(0);
            scoredTags = this.observation.getTrieDictionary().getValue(morphPosPair.getFirst());
            if (scoredTags == null) {
                return;
            }
            for (ScoredTag scoredTag : scoredTags) {
                if (scoredTag.getTagId() == morphPosPair.getSecond()) {
                    LatticeNode firstIrregularNode = this.makeNode(beginIdx, endIdx, morphPosPair.getFirst(), scoredTag.getTag(), scoredTag.getTagId(), prevMaxScore + scoredTag.getScore(), prevMaxIdx);
                    this.appendNode(firstIrregularNode);
                    if (scoredTag.getTagId() == SEJONGTAGS.EC_ID) {
                        LatticeNode extendIrregularNode = this.makeNode(beginIdx, endIdx, morphPosPair.getFirst(), SYMBOL.EF, this.posTable.getId(SYMBOL.EF), prevMaxScore + scoredTag.getScore(), prevMaxIdx);
                        this.appendNode(extendIrregularNode);
                    }
                }
            }
            return;
        }

       	morphPosPair = irregularTokens.get(0);
        scoredTags = this.observation.getTrieDictionary().getValue(morphPosPair.getFirst());
        if (scoredTags == null) {
            return;
        }
        for (ScoredTag scoredTag : scoredTags) {
            if (scoredTag.getTagId() == morphPosPair.getSecond()) {
                LatticeNode firstIrregularNode = this.makeNode(beginIdx, irrIdx - 1, morphPosPair.getFirst(), scoredTag.getTag(), scoredTag.getTagId(), prevMaxScore + scoredTag.getScore(), prevMaxIdx);
                irrIdx--;
                this.appendNode(firstIrregularNode);
            }
        }

        for (int i = 1; i < irregularTokens.size(); i++) {
        	morphPosPair = irregularTokens.get(i);
        	scoredTags = this.observation.getTrieDictionary().getValue(morphPosPair.getFirst());
            if (scoredTags == null) {
                irrIdx--;
                continue;
            }
            if (i == irregularTokens.size() - 1) {
                for (ScoredTag scoredTag : scoredTags) {
                    if (scoredTag.getTagId() == morphPosPair.getSecond()) {
                        this.put(irrIdx, endIdx, morphPosPair.getFirst(), this.posTable.getPos(morphPosPair.getSecond()), morphPosPair.getSecond(), scoredTag.getScore());
                        if (morphPosPair.getSecond() == SEJONGTAGS.EC_ID) {
                            this.put(irrIdx, endIdx, morphPosPair.getFirst(), SYMBOL.EF, SEJONGTAGS.EF_ID, scoredTag.getScore());
                        }
                    }
                }
                LatticeNode latticeNode = this.makeNode(irrIdx, endIdx, morphPosPair.getFirst(), SYMBOL.IRREGULAR, IRREGULAR_POS_ID, 0.0, 0);
                this.appendNode(latticeNode);

            } else {
                for (ScoredTag scoredTag : scoredTags) {
                    if (scoredTag.getTagId() == morphPosPair.getSecond()) {
                        this.put(irrIdx, irrIdx - 1, morphPosPair.getFirst(), this.posTable.getPos(morphPosPair.getSecond()), morphPosPair.getSecond(), scoredTag.getScore());
                    }
                }
            }
            irrIdx--;
        }
    }

    public boolean put(int beginIdx, int endIdx, String morph, String tag, int tagId, double score) {

        List<LatticeNode> prevLatticeNodes = this.getNodeList(beginIdx);

        if (prevLatticeNodes != null) {
            if (nbest != 1) {
                List<LatticeNode> nbestLatticeNodeList = this.getNbestMaxTransitionNodeFromPrevNodes(prevLatticeNodes, beginIdx, endIdx, morph, tag, tagId, score, this.nbest);

                if (nbestLatticeNodeList != null) {
                    for (LatticeNode latticeNode : nbestLatticeNodeList) {
                        this.appendNode(latticeNode);
                    }
                    return true;
                }
            } else {
                LatticeNode maxLatticeNode = this.getMaxTransitionNodeFromPrevNodes(prevLatticeNodes, beginIdx, endIdx, morph, tag, tagId, score);
                if (maxLatticeNode != null) {
                    this.appendNode(maxLatticeNode);
                    return true;
                }
            }

        }
        return false;
    }

    private List<LatticeNode> getNbestMaxTransitionNodeFromPrevNodes(
            List<LatticeNode> prevLatticeNodes, int beginIdx, int endIdx,
            String morph, String tag, int tagId, double score, int nbest) {

        List<LatticeNode> nbestPrevNodeList = new ArrayList<>();
        double[] colScores = transition.getColumnScores(tagId);
        int size = prevLatticeNodes.size();
        for (int i = 0; i < size; i++) {
            LatticeNode prevLatticeNode = prevLatticeNodes.get(i);
            int prevLatticeTagId = prevLatticeNode.getTagId();
            if (prevLatticeTagId == -1) {
                continue;
            }
            int prevTagId;
            String prevMorph;
            if (prevLatticeTagId == SEJONGTAGS.EOE_ID) {
                prevTagId = SEJONGTAGS.BOE_ID;
                prevMorph = SYMBOL.BOE;
            } else {
                prevTagId = prevLatticeTagId;
                prevMorph = prevLatticeNode.getMorph();
            }
            double transitionScore = colScores[prevTagId];
            if (transitionScore == Double.NEGATIVE_INFINITY) {
                continue;
            }

            if (!isValidCombination(prevMorph, prevTagId, morph, tagId)) {
                continue;
            }

            double total = transitionScore + prevLatticeNode.getScore() + score;

            if (nbestPrevNodeList.size() < nbest) {
                nbestPrevNodeList.add(
                        this.makeNode(beginIdx, endIdx, morph, tag, tagId, total, i)
                );
                continue;
            }

            int nbestMinIndex = 0;
            double nbestMinScore = nbestPrevNodeList.get(0).getScore();

            for (int j = 1; j < nbestPrevNodeList.size(); j++) {
                if (nbestMinScore > nbestPrevNodeList.get(j).getScore()) {
                    nbestMinIndex = j;
                    nbestMinScore = nbestPrevNodeList.get(j).getScore();
                }
            }

            if (nbestMinScore < total) {
                nbestPrevNodeList.set(
                        nbestMinIndex,
                        this.makeNode(beginIdx, endIdx, morph, tag, tagId, total, i)
                );
            }
        }
        if (nbestPrevNodeList.size() != 0) {
            return nbestPrevNodeList;
        }
        return null;
    }

    private boolean isValidCombination(String prevMorph, int prevTagId, String morph, int tagId) {
        return this.combinationRuleChecker.isValidRule(prevMorph, prevTagId, morph, tagId);
    }

    private LatticeNode getMaxTransitionNodeFromPrevNodes(
            List<LatticeNode> prevLatticeNodes, int beginIdx, int endIdx,
            String morph, String tag, int tagId, double score) {

        double prevMaxScore = Double.NEGATIVE_INFINITY;
        int prevLatticeNodeIdx = -1;
        double[] colScores = transition.getColumnScores(tagId);
        int size = prevLatticeNodes.size();
        for (int i = 0; i < size; i++) {
            LatticeNode prevLatticeNode = prevLatticeNodes.get(i);
            int prevLatticeTagId = prevLatticeNode.getTagId();
            if (prevLatticeTagId == -1) {
                continue;
            }
            int prevTagId;
            String prevMorph;
            if (prevLatticeTagId == SEJONGTAGS.EOE_ID) {
                prevTagId = SEJONGTAGS.BOE_ID;
                prevMorph = SYMBOL.BOE;
            } else {
                prevTagId = prevLatticeTagId;
                prevMorph = prevLatticeNode.getMorph();
            }
            double transitionScore = colScores[prevTagId];
            if (transitionScore == Double.NEGATIVE_INFINITY) {
                continue;
            }

            if (!isValidCombination(prevMorph, prevTagId, morph, tagId)) {
                continue;
            }

            double total = transitionScore + prevLatticeNode.getScore();

            if (prevMaxScore < total) {
                prevMaxScore = total;
                prevLatticeNodeIdx = i;
            }
        }
        if (prevLatticeNodeIdx != -1) {
            return this.makeNode(beginIdx, endIdx, morph, tag, tagId, prevMaxScore + score, prevLatticeNodeIdx);
        }
        return null;
    }

    public LatticeNode makeNode(int beginIdx, int endIdx, String morph,
                                String tag, int tagId, double score, int prevNodeIdx) {
        LatticeNode latticeNode = new LatticeNode(beginIdx, endIdx, morph, tag, tagId, score);
        latticeNode.setPrevNodeIdx(prevNodeIdx);
        return latticeNode;
    }

    public int appendNode(LatticeNode latticeNode) {
        List<LatticeNode> latticeNodeList = this.getNodeList(latticeNode.getEndIdx());
        if (latticeNodeList == null) {
            latticeNodeList = new ArrayList<>();
        }
        latticeNodeList.add(latticeNode);
        this.putNodeList(latticeNode.getEndIdx(), latticeNodeList);

        // Beam pruning: 양수 인덱스에서만 적용 (음수 = 불규칙 중간 노드)
        if (beamWidth > 0 && latticeNode.getEndIdx() >= 0 && latticeNodeList.size() > beamWidth) {
            pruneToBeamWidth(latticeNodeList);
        }

        return latticeNodeList.size() - 1;
    }

    private void pruneToBeamWidth(List<LatticeNode> nodeList) {
        while (nodeList.size() > beamWidth) {
            int minIdx = 0;
            double minScore = nodeList.get(0).getScore();
            for (int i = 1; i < nodeList.size(); i++) {
                double s = nodeList.get(i).getScore();
                if (s < minScore) {
                    minScore = s;
                    minIdx = i;
                }
            }
            nodeList.remove(minIdx);
        }
    }

    private void getMaxTransitionIdxFromPrevNodes(List<LatticeNode> prevLatticeNodes, int tagId) {
        this.getMaxTransitionInfoFromPrevNodes(prevLatticeNodes, tagId);
    }

    private void getMaxTransitionInfoFromPrevNodes(List<LatticeNode> prevLatticeNodes, int tagId) {

        double[] colScores = transition.getColumnScores(tagId);
        int size = prevLatticeNodes.size();
        for (int i = 0; i < size; i++) {
            LatticeNode prevLatticeNode = prevLatticeNodes.get(i);
            int prevLatticeTagId = prevLatticeNode.getTagId();
            if (prevLatticeTagId == -1) {
                continue;
            }
            int prevTagId;
            if (prevLatticeTagId == SEJONGTAGS.EOE_ID) {
                prevTagId = SEJONGTAGS.BOE_ID;
            } else {
                prevTagId = prevLatticeTagId;
            }
            double transitionScore = colScores[prevTagId];
            if (transitionScore == Double.NEGATIVE_INFINITY) {
                continue;
            }

            double total = transitionScore + prevLatticeNode.getScore();

            if (this.prevMaxScore < total) {
                this.prevMaxScore = total;
                this.prevMaxNode = prevLatticeNode;
                this.prevMaxIdx = i;
            }
        }
    }

    public void setPosTable(PosTable posTable) {
        this.posTable = posTable;
    }

    public void setTransition(Transition transition) {
        this.transition = transition;
    }

    public void printLattice() {
        int totalLatticeSize = 0;
        for (int i = irrIdx; i < this.getLastIdx() + 2; i++) {
            System.out.println("[" + i + "]");
            List<LatticeNode> nodeList = this.getNodeList(i);
            if (nodeList == null) {
                continue;
            }
            totalLatticeSize += nodeList.size();
            int nodeIndex = 0;
            for (LatticeNode latticeNode : nodeList) {
                System.out.println(nodeIndex + " : "+latticeNode);
                nodeIndex++;
            }
            System.out.println();
        }
        System.out.println("Total lattice size : " + totalLatticeSize);
    }

    public int getLastIdx() {
        return lastIdx;
    }

    public void setLastIdx(int lastIdx) {
        this.lastIdx = lastIdx;
    }

    public boolean appendEndNode() {
        return this.put(this.lastIdx, this.lastIdx + 1, SYMBOL.EOE, SYMBOL.EOE, SEJONGTAGS.EOE_ID, 0);
    }

    public List<LatticeNode> findPath() {
        List<LatticeNode> shortestPathList = new ArrayList<>();
        int idx = this.getLastIdx() + 1;
        if (this.getNodeList(idx) == null) {
            return null;
        }

        LatticeNode latticeNode = this.getNodeList(idx).get(0);

        int prevLatticeEndIndex = latticeNode.getEndIdx();
        while (true) {
            latticeNode = this.getNodeList(latticeNode.getBeginIdx()).get(latticeNode.getPrevNodeIdx());
            if (latticeNode.getEndIdx() < 0) {
                latticeNode.setEndIdx(prevLatticeEndIndex);
            }
            shortestPathList.add(latticeNode);
            prevLatticeEndIndex = latticeNode.getEndIdx();
            if (latticeNode.getBeginIdx() == 0) {
                break;
            }
        }

        return shortestPathList;
    }

    public void setObservation(Observation observation) {
        this.observation = observation;
    }

    public List<List<LatticeNode>> findNBestPath() {
        List<List<LatticeNode>> nBestShortestPathList = new ArrayList<>();
        int idx = this.getLastIdx() + 1;
        List<LatticeNode> endNodes = this.getNodeList(idx);
        if (endNodes == null) {
            return null;
        }

        for (LatticeNode endNode : endNodes) {
            List<LatticeNode> shortestPathList = new ArrayList<>();
            int prevLatticeEndIndex = endNode.getEndIdx();
            LatticeNode latticeNode = endNode;
            shortestPathList.add(latticeNode);
            while (true) {
                latticeNode = this.getNodeList(latticeNode.getBeginIdx()).get(latticeNode.getPrevNodeIdx());
                if (latticeNode.getEndIdx() < 0) {
                    LatticeNode copy = new LatticeNode(latticeNode);
                    copy.setPrevNodeIdx(latticeNode.getPrevNodeIdx());
                    copy.setEndIdx(prevLatticeEndIndex);
                    latticeNode = copy;
                }
                shortestPathList.add(latticeNode);
                prevLatticeEndIndex = latticeNode.getEndIdx();
                if (latticeNode.getBeginIdx() == 0) {
                    break;
                }
            }

            nBestShortestPathList.add(shortestPathList);

        }

        if (nBestShortestPathList.size() > 1) {
            nBestShortestPathList = sortNBestByScore(nBestShortestPathList);
        }

        return nBestShortestPathList;
    }

    private List<List<LatticeNode>> sortNBestByScore(List<List<LatticeNode>> nBestShortestPathList) {
        Map<List<LatticeNode>, Double> sortedLatticeNodeList = new HashMap<>();
        for (List<LatticeNode> latticeNodes : nBestShortestPathList) {
            double score = latticeNodes.get(0).getScore();
            sortedLatticeNodeList.put(latticeNodes, score);
        }

        return new ArrayList<>(MapUtil.sortByValue(sortedLatticeNodeList, MapUtil.DESCENDING_ORDER).keySet());
    }
}

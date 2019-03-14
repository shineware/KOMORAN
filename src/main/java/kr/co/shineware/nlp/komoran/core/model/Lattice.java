package kr.co.shineware.nlp.komoran.core.model;

import kr.co.shineware.ds.aho_corasick.FindContext;
import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.model.MorphTag;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.modeler.model.*;
import kr.co.shineware.util.common.model.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lattice {

    private static final int IRREGULAR_POS_ID = -1;
    private Map<Integer, List<LatticeNode>> lattice;
    private PosTable posTable;
    private Transition transition;
    private int lastIdx = -1;
    private int irrIdx = 0;
    private Observation observation;
    private Observation userDicObservation;
    private IrregularTrie irregularTrie;

    private FindContext<List<ScoredTag>> observationFindContext;
    private FindContext<List<IrregularNode>> irregularFindContext;
    private FindContext<List<ScoredTag>> userDicFindContext;

    private double prevMaxScore;
    private LatticeNode prevMaxNode;
    private int prevMaxIdx;
    private int nbest;

    public Lattice(Resources resource, Observation userDic) {
        this(resource, userDic, 1);
    }

    public Lattice(Resources resource, Observation userDic, int nbest) {
        this.setPosTable(resource.getTable());
        this.setTransition(resource.getTransition());
        this.setObservation(resource.getObservation());
        this.setIrregularTrie(resource.getIrrTrie());
        this.setUserDicObservation(userDic);
        this.init();
        this.makeNewContexts();
        this.nbest = nbest;
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

    public Map<String, List<ScoredTag>> retrievalObservation(char jaso){
        return this.observation.getTrieDictionary().get(this.observationFindContext, jaso);
    }

    private void init() {

        this.lattice = new HashMap<>();
        irrIdx = 0;

        List<LatticeNode> latticeNodes = new ArrayList<>();
        latticeNodes.add(this.makeStartNode());

        this.lattice.put(0, latticeNodes);
    }

    private LatticeNode makeStartNode() {
        return new LatticeNode(-1, 0, new MorphTag(SYMBOL.START, SYMBOL.START, this.getPosTable().getId(SYMBOL.START)), 0);
    }

    //기분석 사전을 위한 lattice put
    public void put(int beginIdx, int endIdx,
                    List<Pair<String, String>> fwdResultList) {

        if (fwdResultList.size() == 1) {
            Pair<String, String> morphPosPair = fwdResultList.get(0);
            this.put(beginIdx, endIdx, morphPosPair.getFirst(), morphPosPair.getSecond(), this.posTable.getId(morphPosPair.getSecond()), 0.0);
        }

        //TODO : find solution for better code to simplify calculation of FWD transition score
        //이 로직은 뭐지? 왜 이렇게 만들어 놨을까..
        //아..기분석 결과가 여러 형태소로 이뤄진 경우에는 그 형태소 간의 전이확률을 구해야하는데
        //이거 때문에 irrIdx라는 가상의 index를 주고 그걸로 잇는구나.. 이게 최선인가?
        //이건 코드 짠 사람이 아니면 이해하기 어려울 것 같다. 개선이 필요해 보임. 일단 irrIdx를 전역 변수로 쓰고 있는 것 자체가 별로임
        //어떻게 할까..생각해보면 lattice 자체는 thread safe 하지 않은데.. lattice를 생성하는 로직이 thread safe하기 때문에 이 로직이 가능한 구조인데.. 어떻게 가져가야할까..고민을 좀 해보자..
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
        //현재 node를 연결 시킬 이전 node list들을 가져옴
        List<LatticeNode> prevLatticeNodes = this.lattice.get(beginIdx);

        //아 이거 아래 코드는 심오한데....
        if (prevLatticeNodes != null) {
            this.prevMaxIdx = -1;
            this.prevMaxNode = null;
            this.prevMaxScore = Double.NEGATIVE_INFINITY;
            this.getMaxTransitionIdxFromPrevNodes(prevLatticeNodes, irregularNode.getFirstPosId());

            if (this.prevMaxNode != null) {
                List<Pair<String, Integer>> irregularTokens = irregularNode.getTokens();
                //불규칙확장을 위한 노드 추가
                int prevMaxIdx = this.prevMaxIdx;
                double prevMaxScore = this.prevMaxScore;
                this.putIrregularExtendTokens(beginIdx, endIdx, irregularTokens, prevMaxScore, prevMaxIdx);

                //일반 불규칙을 노드를 추가하기 위한 루틴
                this.putFirstIrrgularNode(beginIdx, endIdx, irregularTokens, prevMaxScore, prevMaxIdx);
                this.putIrregularTokens(beginIdx, endIdx, irregularTokens);
            }
        }
    }

    private void putIrregularExtendTokens(int beginIdx, int endIdx,
                                          List<Pair<String, Integer>> morphPosIdList, double prevMaxScore, int prevMaxIdx) {

        //첫번쨰 토큰에 대한 처리
        if (morphPosIdList.size() != 0) {
            Pair<String, Integer> morphPosId = morphPosIdList.get(0);
            List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morphPosId.getFirst());
            for (ScoredTag scoredTag : scoredTags) {
                if (scoredTag.getTagId() == morphPosId.getSecond()) {
                    LatticeNode firstIrregularNode = this.makeNode(beginIdx, irrIdx - 1, morphPosId.getFirst(), scoredTag.getTag(), scoredTag.getTagId(), prevMaxScore + scoredTag.getScore(), prevMaxIdx);
                    irrIdx--;
                    this.appendNode(firstIrregularNode);
                }
            }
        }

        for (int i = 1; i < morphPosIdList.size(); i++) {
            Pair<String, Integer> morphPosId = morphPosIdList.get(i);
            //마지막 토큰에 대해서는 IRR 태그를 넣어줌 이때 score는 0.0을 줌
            if (i == morphPosIdList.size() - 1) {
                //					this.put(irrIdx, endIdx, morphPosId.getFirst(), "IRR", morphPosId.getSecond(), 0.0);
                //				LatticeNode latticeNode = new LatticeNode(irrIdx, endIdx,new MorphTag(morphPosId.getFirst(),"IRR", IRREGULAR_POS_ID),0.0);
                LatticeNode latticeNode = this.makeNode(irrIdx, endIdx, morphPosId.getFirst(), SYMBOL.IRREGULAR, IRREGULAR_POS_ID, 0.0, 0);
                this.appendNode(latticeNode);

            } else {
                List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morphPosId.getFirst());
                for (ScoredTag scoredTag : scoredTags) {
                    if (scoredTag.getTagId() == morphPosId.getSecond()) {
                        this.put(irrIdx, irrIdx - 1, morphPosId.getFirst(), this.posTable.getPos(morphPosId.getSecond()), morphPosId.getSecond(), scoredTag.getScore());
                    }
                }
            }
            irrIdx--;
        }
    }

    private void putFirstIrrgularNode(int beginIdx, int endIdx,
                                      List<Pair<String, Integer>> irregularTokens, double score,
                                      int maxTransitionPrevIdx) {
        if (irregularTokens.size() == 1) {
            Pair<String, Integer> morphPosId = irregularTokens.get(0);
            List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morphPosId.getFirst());
            for (ScoredTag scoredTag : scoredTags) {
                if (scoredTag.getTagId() == morphPosId.getSecond()) {
                    LatticeNode firstIrregularNode = this.makeNode(beginIdx, endIdx, morphPosId.getFirst(), scoredTag.getTag(), scoredTag.getTagId(), score + scoredTag.getScore(), maxTransitionPrevIdx);
                    this.appendNode(firstIrregularNode);
                    //마지막 노드가 EC인 경우에는 EF를 변환하여 노드를 추가한다
                    if (scoredTag.getTagId() == this.posTable.getId(SYMBOL.EC)) {
                        LatticeNode extendIrregularNode = this.makeNode(beginIdx, endIdx, morphPosId.getFirst(), SYMBOL.EF, this.posTable.getId(SYMBOL.EF), score + scoredTag.getScore(), maxTransitionPrevIdx);
                        this.appendNode(extendIrregularNode);
                    }
                }
            }
        } else {
            Pair<String, Integer> morphPosId = irregularTokens.get(0);
            List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morphPosId.getFirst());
            for (ScoredTag scoredTag : scoredTags) {
                if (scoredTag.getTagId() == morphPosId.getSecond()) {
                    LatticeNode firstIrregularNode = this.makeNode(beginIdx, irrIdx - 1, morphPosId.getFirst(), scoredTag.getTag(), scoredTag.getTagId(), score + scoredTag.getScore(), maxTransitionPrevIdx);
                    irrIdx--;
                    this.appendNode(firstIrregularNode);
                }
            }

        }
    }

    public boolean put(int beginIdx, int endIdx, String morph, String tag, int tagId, double score) {

        List<LatticeNode> prevLatticeNodes = this.getNodeList(beginIdx);

        if (prevLatticeNodes != null) {

            List<LatticeNode> nbestLatticeNodeList = this.getNbestMaxTransitionNodeFromPrevNodes(prevLatticeNodes, beginIdx, endIdx, morph, tag, tagId, score, this.nbest);

            if (nbestLatticeNodeList != null) {
                for (LatticeNode latticeNode : nbestLatticeNodeList) {
                    this.appendNode(latticeNode);
                }
                return true;
            }
        }
        return false;
    }

    private List<LatticeNode> getNbestMaxTransitionNodeFromPrevNodes(
            List<LatticeNode> prevLatticeNodes, int beginIdx, int endIdx,
            String morph, String tag, int tagId, double score, int nbest) {

        List<LatticeNode> nbestPrevNodeList = new ArrayList<>();
        int latticeNodeIdx = -1;
        for (LatticeNode prevLatticeNode : prevLatticeNodes) {
            latticeNodeIdx++;
            //불규칙인경우
            if (prevLatticeNode.getMorphTag().getTagId() == -1) {
                continue;
            }
            int prevTagId;
            String prevMorph;
            if (prevLatticeNode.getMorphTag().getTag().equals(SYMBOL.END)) {
                prevTagId = this.getPosTable().getId(SYMBOL.START);
                prevMorph = SYMBOL.START;
            } else {
                prevTagId = prevLatticeNode.getMorphTag().getTagId();
                prevMorph = prevLatticeNode.getMorphTag().getMorph();
            }
            //전이 확률 값 가져옴
            Double transitionScore = this.transition.get(prevTagId, tagId);
            if (transitionScore == null) {
                continue;
            }

            //자소 결합규칙 체크
            if (tagId == this.posTable.getId(SYMBOL.JKO)) {
                if (this.hasJongsung(prevMorph)) {
                    if (morph.charAt(0) != 'ㅇ') {
                        continue;
                    }
                } else {
                    if (morph.charAt(0) == 'ㅇ') {
                        continue;
                    }
                }
            } else if (tagId == this.posTable.getId(SYMBOL.JKS)
                    || tagId == this.posTable.getId(SYMBOL.JKC)) {
                if (this.hasJongsung(prevMorph)) {
                    if (morph.charAt(0) == 'ㄱ' && morph.charAt(1) == 'ㅏ') {
                        continue;
                    }
                } else {
                    if (morph.charAt(0) == 'ㅇ' && morph.charAt(1) == 'ㅣ') {
                        continue;
                    }
                }
            }

            double prevObservationScore = prevLatticeNode.getScore();

            if (nbestPrevNodeList.size() < nbest) {
                nbestPrevNodeList.add(
                        this.makeNode(beginIdx, endIdx, morph, tag, tagId, transitionScore + prevObservationScore + score, latticeNodeIdx)
                );
                continue;
            }

            int nbestMinIndex = 0;
            double nbestMinScore = nbestPrevNodeList.get(0).getScore();

            for (int i = 1; i < nbestPrevNodeList.size(); i++) {
                if (nbestMinScore > nbestPrevNodeList.get(i).getScore()) {
                    nbestMinIndex = i;
                    nbestMinScore = nbestPrevNodeList.get(i).getScore();
                }
            }

            if (nbestMinScore < transitionScore + prevObservationScore + score) {
                nbestPrevNodeList.set(
                        nbestMinIndex,
                        this.makeNode(beginIdx, endIdx, morph, tag, tagId, transitionScore + prevObservationScore + score, latticeNodeIdx)
                );
            }
        }
        if (nbestPrevNodeList.size() != 0) {
            return nbestPrevNodeList;
        }
        return null;
    }

    private LatticeNode getMaxTransitionNodeFromPrevNodes(
            List<LatticeNode> prevLatticeNodes, int beginIdx, int endIdx,
            String morph, String tag, int tagId, double score) {

        double prevMaxScore = Double.NEGATIVE_INFINITY;
        LatticeNode prevMaxNode = null;
        int latticeNodeIdx = -1;
        int prevLatticeNodeIdx = -1;
        for (LatticeNode prevLatticeNode : prevLatticeNodes) {
            latticeNodeIdx++;
            //불규칙인경우
            if (prevLatticeNode.getMorphTag().getTagId() == -1) {
                continue;
            }
            int prevTagId;
            String prevMorph;
            if (prevLatticeNode.getMorphTag().getTag().equals(SYMBOL.END)) {
                prevTagId = this.getPosTable().getId(SYMBOL.START);
                prevMorph = SYMBOL.START;
            } else {
                prevTagId = prevLatticeNode.getMorphTag().getTagId();
                prevMorph = prevLatticeNode.getMorphTag().getMorph();
            }
            //전이 확률 값 가져옴
            Double transitionScore = this.transition.get(prevTagId, tagId);
            if (transitionScore == null) {
                continue;
            }

            //자소 결합규칙 체크
            if (tagId == this.posTable.getId(SYMBOL.JKO)) {
                if (this.hasJongsung(prevMorph)) {
                    if (morph.charAt(0) != 'ㅇ') {
                        continue;
                    }
                } else {
                    if (morph.charAt(0) == 'ㅇ') {
                        continue;
                    }
                }
            } else if (tagId == this.posTable.getId(SYMBOL.JKS)
                    || tagId == this.posTable.getId(SYMBOL.JKC)) {
                if (this.hasJongsung(prevMorph)) {
                    if (morph.charAt(0) == 'ㄱ' && morph.charAt(1) == 'ㅏ') {
                        continue;
                    }
                } else {
                    if (morph.charAt(0) == 'ㅇ' && morph.charAt(1) == 'ㅣ') {
                        continue;
                    }
                }
            }

            double prevObservationScore = prevLatticeNode.getScore();

            if (prevMaxScore < transitionScore + prevObservationScore) {
                prevMaxScore = transitionScore + prevObservationScore;
                prevMaxNode = prevLatticeNode;
                prevLatticeNodeIdx = latticeNodeIdx;
            }
        }
        if (prevMaxNode != null) {
            return this.makeNode(beginIdx, endIdx, morph, tag, tagId, prevMaxScore + score, prevLatticeNodeIdx);
        }
        return null;
    }

    public LatticeNode makeNode(int beginIdx, int endIdx, String morph,
                                String tag, int tagId, double score, int prevNodeHash) {
        LatticeNode latticeNode = new LatticeNode(beginIdx, endIdx, new MorphTag(morph, tag, tagId), score);
        latticeNode.setPrevNodeIdx(prevNodeHash);
        return latticeNode;
    }

    public int appendNode(LatticeNode latticeNode) {
        List<LatticeNode> latticeNodeList = this.getNodeList(latticeNode.getEndIdx());
        if (latticeNodeList == null) {
            latticeNodeList = new ArrayList<>();
        }
        latticeNodeList.add(latticeNode);
        this.lattice.put(latticeNode.getEndIdx(), latticeNodeList);
        return latticeNodeList.size() - 1;
    }

    public List<LatticeNode> getNodeList(int index) {
        return this.lattice.get(index);
    }

    private void getMaxTransitionIdxFromPrevNodes(List<LatticeNode> prevLatticeNodes, int tagId) {
        this.getMaxTransitionInfoFromPrevNodes(prevLatticeNodes, tagId);
    }

    private void getMaxTransitionInfoFromPrevNodes(List<LatticeNode> prevLatticeNodes, int tagId) {

        int prevMaxNodeIdx = -1;
        for (LatticeNode prevLatticeNode : prevLatticeNodes) {
            prevMaxNodeIdx++;
            //불규칙인경우
            if (prevLatticeNode.getMorphTag().getTagId() == -1) {
                continue;
            }
            int prevTagId;
//            String prevMorph;
            if (prevLatticeNode.getMorphTag().getTag().equals(SYMBOL.END)) {
                prevTagId = this.getPosTable().getId(SYMBOL.START);
            } else {
                prevTagId = prevLatticeNode.getMorphTag().getTagId();
            }
            //전이 확률 값 가져옴
            Double transitionScore = this.transition.get(prevTagId, tagId);
            if (transitionScore == null) {
                continue;
            }

            double prevObservationScore = prevLatticeNode.getScore();

            if (this.prevMaxScore < transitionScore + prevObservationScore) {
                this.prevMaxScore = transitionScore + prevObservationScore;
                this.prevMaxNode = prevLatticeNode;
                this.prevMaxIdx = prevMaxNodeIdx;
            }
        }
    }

    private boolean hasJongsung(String str) {
        char prevLastJaso = str.charAt(str.length() - 1);
        if (0x3131 <= prevLastJaso && prevLastJaso <= 0x314e) {
            return prevLastJaso != 0x3138 && prevLastJaso != 0x3143 && prevLastJaso != 0x3149;
        }
        return false;
    }

    public PosTable getPosTable() {
        return posTable;
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
            List<LatticeNode> nodeList = this.lattice.get(i);
            if (nodeList == null) {
                continue;
            }
            totalLatticeSize += nodeList.size();

            for (LatticeNode latticeNode : nodeList) {
                System.out.println(latticeNode);
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
        return this.put(this.lastIdx, this.lastIdx + 1, SYMBOL.END, SYMBOL.END, this.getPosTable().getId(SYMBOL.END), 0);
    }

    public List<LatticeNode> findPath() {
        List<LatticeNode> shortestPathList = new ArrayList<>();
        int idx = this.getLastIdx() + 1;
        //마지막 연결 노드가 없는 경우에는 null 반환
        if (!this.lattice.containsKey(idx)) {
            return null;
        }

        LatticeNode latticeNode = this.lattice.get(idx).get(0);

        int prevLatticeEndIndex = latticeNode.getEndIdx();
        while (true) {
            latticeNode = this.lattice.get(latticeNode.getBeginIdx()).get(latticeNode.getPrevNodeIdx());
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


    private void putIrregularTokens(int beginIdx, int endIdx, List<Pair<String, Integer>> morphPosIdList) {

        for (int i = 1; i < morphPosIdList.size(); i++) {
            Pair<String, Integer> morphPosId = morphPosIdList.get(i);
            List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morphPosId.getFirst());
            if (i == morphPosIdList.size() - 1) {
                for (ScoredTag scoredTag : scoredTags) {
                    if (scoredTag.getTagId() == morphPosId.getSecond()) {
                        this.put(irrIdx, endIdx, morphPosId.getFirst(), this.posTable.getPos(morphPosId.getSecond()), morphPosId.getSecond(), scoredTag.getScore());
                        if (morphPosId.getSecond() == this.posTable.getId(SYMBOL.EC)) {
                            this.put(irrIdx, endIdx, morphPosId.getFirst(), SYMBOL.EF, this.posTable.getId(SYMBOL.EF), scoredTag.getScore());
                        }
                    }
                }
            } else {
                for (ScoredTag scoredTag : scoredTags) {
                    if (scoredTag.getTagId() == morphPosId.getSecond()) {
                        this.put(irrIdx, irrIdx - 1, morphPosId.getFirst(), this.posTable.getPos(morphPosId.getSecond()), morphPosId.getSecond(), scoredTag.getScore());
                    }
                }
            }
            irrIdx--;
        }
    }

    public void setObservation(Observation observation) {
        this.observation = observation;
    }

    public List<List<LatticeNode>> findNBestPath() {
        List<List<LatticeNode>> nBestShortestPathList = new ArrayList<>();
        int idx = this.getLastIdx() + 1;
        //마지막 연결 노드가 없는 경우에는 null 반환
        if (!this.lattice.containsKey(idx)) {
            return null;
        }

        for (LatticeNode endNode : this.lattice.get(idx)) {
            List<LatticeNode> shortestPathList = new ArrayList<>();
            int prevLatticeEndIndex = endNode.getEndIdx();
            LatticeNode latticeNode = endNode;
            while (true) {
                latticeNode = this.lattice.get(latticeNode.getBeginIdx()).get(latticeNode.getPrevNodeIdx());
                if (latticeNode.getEndIdx() < 0) {
                    latticeNode.setEndIdx(prevLatticeEndIndex);
                }
                shortestPathList.add(latticeNode);
                prevLatticeEndIndex = latticeNode.getEndIdx();
                if (latticeNode.getBeginIdx() == 0) {
                    break;
                }
            }

            nBestShortestPathList.add(shortestPathList);

        }

        return nBestShortestPathList;
    }
}

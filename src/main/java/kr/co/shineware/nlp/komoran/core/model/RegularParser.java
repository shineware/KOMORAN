package kr.co.shineware.nlp.komoran.core.model;

import kr.co.shineware.ds.aho_corasick.FindContext;
import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.model.ScoredTag;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegularParser implements Parser {

    private Lattice lattice;
    private FindContext<List<ScoredTag>> observationFindContext;
    private char jaso;
    private int curIndex;
    private Resources resources;

    public RegularParser(Resources resources){
        this.resources = resources;
    }

    public void setParseInfo(Lattice lattice, FindContext<List<ScoredTag>> observationFindContext, char jaso, int curIndex){
        this.lattice = lattice;
        this.observationFindContext = observationFindContext;
        this.jaso = jaso;
        this.curIndex = curIndex;
    }

    @Override
    public void parse() {
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
    private Map<String, List<ScoredTag>> getMorphScoredTagsMap(FindContext<List<ScoredTag>> observationFindContext, char jaso) {
        return this.resources.getObservation().getTrieDictionary().get(observationFindContext, jaso);
    }

    private Set<String> getMorphes(
            Map<String, List<ScoredTag>> morphScoredTagMap) {
        return morphScoredTagMap.keySet();
    }
}

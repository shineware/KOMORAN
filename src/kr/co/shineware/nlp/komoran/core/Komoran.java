package kr.co.shineware.nlp.komoran.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.co.shineware.nlp.komoran.core.model.Lattice;
import kr.co.shineware.nlp.komoran.core.model.Resources;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.model.Tag;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;

public class Komoran {

	private Resources resources;
	private KoreanUnitParser unitParser;
	private Lattice lattice;

	public Komoran(String modelPath){
		this.resources = new Resources();
		this.load(modelPath);
		this.unitParser = new KoreanUnitParser();
	}

	public String analyze(String in){
		in = in.trim();
		if(in.length() == 0){
			return null;
		}
		
		this.lattice = new Lattice(this.resources);

		//자소 단위로 분할
		String jasoUnits = unitParser.parse(in);
		int length = jasoUnits.length();
		
		for(int i=0; i<length; i++){
			//TRIE 기반의 사전 검색하여 형태소와 품사 및 품사 점수(observation)를 얻어옴
			Map<String, List<ScoredTag>> morphScoredTagsMap = this.getMorphScoredTagsMap(jasoUnits.charAt(i));
			if(morphScoredTagsMap == null){
				continue;
			}
			
			//형태소 정보만 얻어옴
			Set<String> morphes = this.getMorphes(morphScoredTagsMap);
			
			//각 형태소와 품사 정보를 lattice에 삽입
			for (String morph : morphes) {
				int beginIdx = i-morph.length()+1;
				int endIdx = i+1;
				//형태소에 대한 품사 및 점수(observation) 정보를 List 형태로 가져옴
				List<ScoredTag> scoredTags = morphScoredTagsMap.get(morph);
				
				for (ScoredTag scoredTag : scoredTags) {
					this.insertLattice(beginIdx,endIdx,morph,scoredTag,scoredTag.getScore());
				}
			}
		}
		
		this.lattice.setLastIdx(jasoUnits.length());
		this.lattice.appendEndNode();
//		this.lattice.printLattice();
		this.lattice.findPath();
		return null;
	}

	private void insertLattice(int beginIdx, int endIdx, String morph,
			Tag tag, double score) {
		this.lattice.put(beginIdx,endIdx,morph,tag,score);
	}

	private Set<String> getMorphes(
			Map<String, List<ScoredTag>> morphScoredTagMap) {
		return morphScoredTagMap.keySet();
	}

	private Map<String, List<ScoredTag>> getMorphScoredTagsMap(char jaso) {
		return this.resources.getObservation().getTrieDictionary().get(jaso);
	}

	public void load(String modelPath){
		this.resources.load(modelPath);
	}
}

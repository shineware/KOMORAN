package kr.co.shineware.nlp.komoran.core.model;

import kr.co.shineware.ds.aho_corasick.FindContext;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class IrregularParser implements Parser {

    private Resources resources;
    private Lattice lattice;
    private FindContext<List<IrregularNode>> irregularFindContext;
    private char jaso;
    private int curIndex;

    public IrregularParser(Resources resources){
        this.resources = resources;
    }

    public void setParseInfo(Lattice lattice, FindContext<List<IrregularNode>> irregularFindContext, char jaso, int curIndex) {
        this.lattice = lattice;
        this.irregularFindContext = irregularFindContext;
        this.jaso = jaso;
        this.curIndex = curIndex;
    }

    @Override
    public void parse() {
        //불규칙 노드들을 얻어옴
        Map<String, List<IrregularNode>> morphIrrNodesMap = this.getIrregularNodes(irregularFindContext, jaso);
        if (morphIrrNodesMap != null) {
            //형태소 정보만 얻어옴
            Set<String> morphs = morphIrrNodesMap.keySet();
            for (String morph : morphs) {
                List<IrregularNode> irrNodes = morphIrrNodesMap.get(morph);
                int beginIdx = this.curIndex - morph.length() + 1;
                int endIdx = this.curIndex + 1;
                for (IrregularNode irregularNode : irrNodes) {
                    this.insertLattice(this.lattice, beginIdx, endIdx, irregularNode);
                }
            }

        }
    }

    private void insertLattice(Lattice lattice, int beginIdx, int endIdx,
                               IrregularNode irregularNode) {
        lattice.put(beginIdx, endIdx, irregularNode);
    }

    private Map<String, List<IrregularNode>> getIrregularNodes(FindContext<List<IrregularNode>> irregularFindContext, char jaso) {
        return this.resources.getIrrTrie().getTrieDictionary().get(irregularFindContext, jaso);
    }
}

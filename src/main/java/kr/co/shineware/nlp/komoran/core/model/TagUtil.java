package kr.co.shineware.nlp.komoran.core.model;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.modeler.model.PosTable;

import java.util.Map;
import java.util.Set;

public class TagUtil {

    private final PosTable posTable;
    private boolean[] nounMask;
    private boolean[] eomiMask;
    private boolean[] josaMask;

    public TagUtil(PosTable posTable) {
        this.posTable = posTable;
        buildNounMask();
        buildEomiMask();
        buildJosaMask();
    }

    private void buildJosaMask() {
        this.josaMask = buildMask(SYMBOL.JOSA_SET);
    }


    private void buildEomiMask() {
        this.eomiMask = buildMask(SYMBOL.EOMI_SET);
    }

    private void buildNounMask() {
        this.nounMask = buildMask(SYMBOL.NOUN_SET);
    }

    private boolean[] buildMask(Set<String> symbolSet) {
        Set<Map.Entry<Integer, String>> idPosSet = this.posTable.getIdPosTable().entrySet();
        int maxIdSize = idPosSet.size();
        boolean[] mask = new boolean[maxIdSize];
        for (Map.Entry<Integer, String> idPosEntry : idPosSet) {
            Integer id = idPosEntry.getKey();
            mask[id] = hasTagName(id, symbolSet);
        }
        return mask;
    }

    public int getId(String tagName) {
        return this.posTable.getId(tagName);
    }

    private boolean hasTagName(int tagId, String[] symbols) {
        for (String tagName : symbols) {
            if (tagId == this.posTable.getId(tagName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTagName(int tagId, Set<String> symbols) {
        return symbols.contains(this.posTable.getPos(tagId));
    }

    public boolean isJosa(int tagId) {
        return josaMask[tagId];
//        return hasTagName(tagId, SYMBOL.JOSA_SET);
    }

    public boolean isNoun(int tagId) {
        return nounMask[tagId];
//        return hasTagName(tagId, SYMBOL.NOUN_SET);
    }

    public boolean isEomi(int tagId) {
        return eomiMask[tagId];
//        return hasTagName(tagId, SYMBOL.EOMI_SET);
    }
}

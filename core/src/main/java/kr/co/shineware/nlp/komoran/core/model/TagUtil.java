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
        int maxId = 0;
        for (Map.Entry<Integer, String> idPosEntry : idPosSet) {
            if (idPosEntry.getKey() > maxId) {
                maxId = idPosEntry.getKey();
            }
        }
        boolean[] mask = new boolean[maxId + 1];
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
        if (tagId < 0 || tagId >= josaMask.length) return false;
        return josaMask[tagId];
    }

    public boolean isNoun(int tagId) {
        if (tagId < 0 || tagId >= nounMask.length) return false;
        return nounMask[tagId];
    }

    public boolean isEomi(int tagId) {
        if (tagId < 0 || tagId >= eomiMask.length) return false;
        return eomiMask[tagId];
    }
}

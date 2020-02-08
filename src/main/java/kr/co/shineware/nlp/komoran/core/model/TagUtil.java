package kr.co.shineware.nlp.komoran.core.model;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.modeler.model.PosTable;

public class TagUtil {

    private final PosTable posTable;

    public TagUtil(PosTable posTable) {
        this.posTable = posTable;
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

    public boolean isJosa(int tagId) {
        return hasTagName(tagId, SYMBOL.JOSA);
    }

    public boolean isNoun(int tagId) {
        return hasTagName(tagId, SYMBOL.NOUN);
    }

    public boolean isEomi(int tagId) {
        return hasTagName(tagId, SYMBOL.EOMI);
    }
}

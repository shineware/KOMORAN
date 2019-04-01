package kr.co.shineware.nlp.komoran.admin.util.parser.stream;

import antlr.Grammar;
import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.GrammarIn;
import kr.co.shineware.nlp.komoran.admin.domain.GrammarType;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceMalformedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GrammarInStreamParser implements StreamParser {

    public static Stream<GrammarIn> parse(String input) {
        String[] elements = input.split("\t");
        List<GrammarIn> resultItems = new ArrayList<>();
        GrammarType start;

        // INVALID FORM
        if (elements.length != 2) {
            throw new ResourceMalformedException("Tab 구분자 오류, " + input);
        }

        String startInRaw = elements[0];
        String[] attrs = elements[1].split(",");

        try {
            start = GrammarType.valueOf(startInRaw);
        }
        catch (IllegalArgumentException e) {
            throw new ResourceMalformedException("잘못된 시작 품사, " + input);
        }

        for (String anAttr : attrs) {
            GrammarIn tmpItem = new GrammarIn();
            String[] tmpAttr = anAttr.split(":");

            // INVALID FORM
            if (tmpAttr.length < 2) {
                throw new ResourceMalformedException(": 구분자 오류, " + input);
            }

            GrammarType next;
            int tf;

            try {
                next = GrammarType.valueOf(tmpAttr[0]);
                tf = Integer.valueOf(tmpAttr[1]);

                if (tf < 0) {
                    throw new ResourceMalformedException("잘못된 빈도, " + input);
                }
            } catch (IllegalArgumentException e) {
                throw new ResourceMalformedException("잘못된 품사 또는 빈도, " + input);
            }

            tmpItem.setStart(start);
            tmpItem.setNext(next);
            tmpItem.setTf(tf);

            resultItems.add(tmpItem);
        }

        return resultItems.stream();
    }
}

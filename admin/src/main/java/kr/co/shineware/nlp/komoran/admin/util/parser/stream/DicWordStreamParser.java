package kr.co.shineware.nlp.komoran.admin.util.parser.stream;

import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceMalformedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class DicWordStreamParser implements StreamParser {

    public static Stream<DicWord> parse(String input) {
        String[] elements = input.split("\t");
        List<DicWord> resultItems = new ArrayList<>();

        // INVALID FORM
        if (elements.length < 2) {
            throw new ResourceMalformedException("Tab 구분자 오류, " + input);
        }

        String token = elements[0];
        String[] attrs = Arrays.copyOfRange(elements, 1, elements.length);

        for (String anAttr : attrs) {
            DicWord tmpItem = new DicWord();
            String[] tmpAttr = anAttr.split(":");

            // INVALID FORM
            if (tmpAttr.length < 2) {
                throw new ResourceMalformedException(": 구분자 오류, " + input);
            }

            PosType pos;
            int tf;

            try {
                pos = PosType.valueOf(tmpAttr[0].toUpperCase());
                tf = Integer.valueOf(tmpAttr[1]);

                if (tf < 0) {
                    throw new ResourceMalformedException("잘못된 빈도, " + input);
                }
            } catch (IllegalArgumentException e) {
                throw new ResourceMalformedException("잘못된 품사 또는 빈도, " + input);
            }

            tmpItem.setToken(token);
            tmpItem.setPos(pos);
            tmpItem.setTf(tf);

            resultItems.add(tmpItem);
        }

        return resultItems.stream();
    }
}

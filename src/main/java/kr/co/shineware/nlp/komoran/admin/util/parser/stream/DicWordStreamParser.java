package kr.co.shineware.nlp.komoran.admin.util.parser.stream;

import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class DicWordStreamParser implements StreamParser {

    public static Stream<DicWord> parse(String input) {
        String[] elements = input.split("\t");
        List<DicWord> resultItems = new ArrayList<>();

        // SKIP::INVALID FORM
        if (elements.length < 2) {
            return null;
        }

        String token = elements[0];
        String[] attrs = Arrays.copyOfRange(elements, 1, elements.length);

        for (String anAttr : attrs) {
            DicWord tmpItem = new DicWord();
            String[] tmpAttr = anAttr.split(":");

            // SKIP::INVALID FORM
            if (tmpAttr.length < 2) {
                continue;
            }

            PosType pos;
            int tf;

            try {
                pos = PosType.valueOf(tmpAttr[0].toUpperCase());
                tf = Integer.valueOf(tmpAttr[1]);
            }
            catch(IllegalArgumentException e) {
                continue;
            }

            tmpItem.setToken(token);
            tmpItem.setPos(pos);
            tmpItem.setTf(tf);

            resultItems.add(tmpItem);
        }

        return resultItems.stream();
    }
}

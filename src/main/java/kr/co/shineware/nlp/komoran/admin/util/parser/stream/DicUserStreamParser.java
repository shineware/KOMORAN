package kr.co.shineware.nlp.komoran.admin.util.parser.stream;

import kr.co.shineware.nlp.komoran.admin.domain.DicUser;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceMalformedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DicUserStreamParser implements StreamParser {

    public static Stream<DicUser> parse(String input) {
        String[] elements = input.split("\t");
        List<DicUser> resultItems = new ArrayList<>();
        DicUser anItem = new DicUser();
        String token;
        PosType pos;

        // TOKEN ONLY
        if (elements.length < 2) {
            token = input.trim();
            pos = PosType.valueOf("NNP");
        }
        // TOKEN & POS
        else if (elements.length == 2){
            token = elements[0];
            String posInRaw = elements[1];

            try {
                pos = PosType.valueOf(posInRaw.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResourceMalformedException("잘못된 품사, " + input);
            }
        }
        else {
            throw new ResourceMalformedException("잘못된 입력, " + input);
        }

        anItem.setToken(token);
        anItem.setPos(pos);

        resultItems.add(anItem);

        return resultItems.stream();
    }

}

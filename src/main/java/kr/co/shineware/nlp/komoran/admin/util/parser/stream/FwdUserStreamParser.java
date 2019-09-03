package kr.co.shineware.nlp.komoran.admin.util.parser.stream;

import kr.co.shineware.nlp.komoran.admin.domain.FwdUser;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceMalformedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FwdUserStreamParser {
    public static Stream<FwdUser> parse(String input) {
        String[] elements = input.split("\t");
        List<FwdUser> resultItems = new ArrayList<>();
        FwdUser fwdItem = new FwdUser();

        // INVALID FORM
        if (elements.length != 2) {
            throw new ResourceMalformedException("Tab 구분자 오류, " + input);
        }

        String fullInRaw = elements[0];
        String analyzedInRaw = elements[1];
        String[] analyzedResults = elements[1].split(" ");

        if ("".equals(fullInRaw)) {
            throw new ResourceMalformedException("잘못된 기분석 어절, " + input);
        }

        String full = fullInRaw;

        for (String anAnalyzedResult : analyzedResults) {
            String[] tmpAnalyzedResults = anAnalyzedResult.split("/");

            // INVALID FORM
            if (tmpAnalyzedResults.length < 2) {
                throw new ResourceMalformedException("분석 결과 내 / 구분자 오류, " + input);
            }

            // FOR VALIDATING ANALYZED RESULT, PARSE IT
            String tmpAnalyzedTerm;
            String tmpAnalyzedPosInRaw;

            // Term includes delimiter '/'
            if (tmpAnalyzedResults.length > 2) {
                int delimiterIdx = anAnalyzedResult.lastIndexOf("/");
                tmpAnalyzedTerm = anAnalyzedResult.substring(0, delimiterIdx);
                tmpAnalyzedPosInRaw = anAnalyzedResult.substring(delimiterIdx+1);
            }
            else {
                tmpAnalyzedTerm = tmpAnalyzedResults[0];
                tmpAnalyzedPosInRaw = tmpAnalyzedResults[1];
            }

            // VALIDATE ANALYZED TERM
            if ("".equals(tmpAnalyzedTerm)) {
                throw new ResourceMalformedException("잘못된 분석 결과(단어), " + input);
            }

            // VALIDATE ANALYZED POS
            try {
                PosType.valueOf(tmpAnalyzedPosInRaw);
            } catch (IllegalArgumentException e) {
                throw new ResourceMalformedException("잘못된 분석 결과(품사), " + input);
            }
        }

        fwdItem.setFull(full);
        fwdItem.setAnalyzed(analyzedInRaw);

        // TODO: Only one FwdUser item in resultsItems -> need to refactor
        resultItems.add(fwdItem);

        return resultItems.stream();
    }
}

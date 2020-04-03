package kr.co.shineware.nlp.komoran.admin.grammarin;

import kr.co.shineware.nlp.komoran.admin.domain.GrammarIn;
import kr.co.shineware.nlp.komoran.admin.domain.GrammarType;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceMalformedException;
import kr.co.shineware.nlp.komoran.admin.util.parser.stream.GrammarInStreamParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
public class ParserTest {

    @Before
    public void init() {

    }

    @Test
    public void GrammarInParser_Test() {
        String strToTest = "BOE\tVV:318295,VCN:7887,VX:72227,NP:56882,VCP:120,NNB:107799";
        List<GrammarIn> result = GrammarInStreamParser.parse(strToTest).collect(Collectors.toList());

        assertThat(result.get(0).getStart(), is(GrammarType.BOE));
        assertThat(result.get(0).getNext(), is(GrammarType.VV));
        assertThat(result.get(0).getTf(), is(318295));

        assertThat(result.get(1).getStart(), is(GrammarType.BOE));
        assertThat(result.get(1).getNext(), is(GrammarType.VCN));
        assertThat(result.get(1).getTf(), is(7887));

        assertThat(result.get(5).getStart(), is(GrammarType.BOE));
        assertThat(result.get(5).getNext(), is(GrammarType.NNB));
        assertThat(result.get(5).getTf(), is(107799));
    }


    @Test(expected = ResourceMalformedException.class)
    public void GrammarInParser_Malformed_Test() {
        String strToTest = "BOE";
        List<GrammarIn> result = GrammarInStreamParser.parse(strToTest).collect(Collectors.toList());
    }

    @Test(expected = ResourceMalformedException.class)
    public void GrammarInParser_InvalidStart_Test() {
        String strToTest = "ABC\tVV:318295,VCN:7887,VX:72227,NP:56882,VCP:120,NNB:107799";
        List<GrammarIn> result = GrammarInStreamParser.parse(strToTest).collect(Collectors.toList());
    }


    @Test(expected = ResourceMalformedException.class)
    public void GrammarInParser_InvalidNext_Test() {
        String strToTest = "BOE\tABC:318295,VCN:7887,VX:72227,NP:56882,VCP:120,NNB:107799";
        List<GrammarIn> result = GrammarInStreamParser.parse(strToTest).collect(Collectors.toList());
    }


    @Test(expected = ResourceMalformedException.class)
    public void GrammarInParser_InvalidTf_Test() {
        String strToTest = "BOE\tABC:318295,VCN:-7887,VX:72227,NP:56882,VCP:120,NNB:107799";
        List<GrammarIn> result = GrammarInStreamParser.parse(strToTest).collect(Collectors.toList());
    }


    @Test(expected = ResourceMalformedException.class)
    public void GrammarInParser_NotNumericTf_Test() {
        String strToTest = "BOE\tABC:318295,VCN:7887,VX:INF,NP:56882,VCP:120,NNB:107799";
        List<GrammarIn> result = GrammarInStreamParser.parse(strToTest).collect(Collectors.toList());
    }
}

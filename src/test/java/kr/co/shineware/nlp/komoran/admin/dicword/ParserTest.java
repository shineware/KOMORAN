package kr.co.shineware.nlp.komoran.admin.dicword;

import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.util.parser.stream.DicWordStreamParser;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
    public void DicWordParserTest() {
        String strToTest = "탄로\tNNG:10\tNNP:20\tNNP:10";
        List<DicWord> result = DicWordStreamParser.parse(strToTest).collect(Collectors.toList());

        assertThat(result.get(0).getToken(), is("탄로"));
        MatcherAssert.assertThat(result.get(0).getPos(), Matchers.is(PosType.NNG));
        assertThat(result.get(0).getTf(), is(10));

        assertThat(result.get(1).getToken(), is("탄로"));
        assertThat(result.get(1).getPos(), is(PosType.NNP));
        assertThat(result.get(1).getTf(), is(20));
    }

}

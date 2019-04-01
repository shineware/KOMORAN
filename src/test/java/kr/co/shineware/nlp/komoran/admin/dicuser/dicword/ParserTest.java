package kr.co.shineware.nlp.komoran.admin.dicuser.dicword;

import kr.co.shineware.nlp.komoran.admin.domain.DicUser;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceMalformedException;
import kr.co.shineware.nlp.komoran.admin.util.parser.stream.DicUserStreamParser;
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
    public void DicUserParser_Test() {
        String strToTest = "탄로\tNNP";
        List<DicUser> result = DicUserStreamParser.parse(strToTest).collect(Collectors.toList());

        assertThat(result.get(0).getToken(), is("탄로"));
        assertThat(result.get(0).getPos(), is(PosType.NNP));
    }


    @Test
    public void DicUserParser_TokenOnly_Test() {
        String strToTest = "가나다라";
        List<DicUser> result = DicUserStreamParser.parse(strToTest).collect(Collectors.toList());

        assertThat(result.get(0).getToken(), is("가나다라"));
        assertThat(result.get(0).getPos(), is(PosType.NNP));
    }


    @Test(expected = ResourceMalformedException.class)
    public void DicUserParser_Malformed_Test() {
        String strToTest = "탄로\tNNP\tNNG";
        List<DicUser> result = DicUserStreamParser.parse(strToTest).collect(Collectors.toList());
    }


    @Test(expected = ResourceMalformedException.class)
    public void DicUserParser_PosNotExist_Test() {
        String strToTest = "탄로\tABC";
        List<DicUser> result = DicUserStreamParser.parse(strToTest).collect(Collectors.toList());
    }

}

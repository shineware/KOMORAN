package kr.co.shineware.nlp.komoran.admin.fwduser;

import kr.co.shineware.nlp.komoran.admin.domain.FwdUser;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceMalformedException;
import kr.co.shineware.nlp.komoran.admin.util.parser.stream.FwdUserStreamParser;
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
    public void FwdUserParser_Test() {
        String strToTest = "하늘을\t하늘/NNG 을/JKO";
        List<FwdUser> result = FwdUserStreamParser.parse(strToTest).collect(Collectors.toList());

        assertThat(result.get(0).getFull(), is("하늘을"));
        assertThat(result.get(0).getAnalyzed(), is("하늘/NNG 을/JKO"));
    }

    @Test
    public void FwdUserParser_DelimiterInTerm_Test() {
        String strToTest = "하늘을///\t하늘/NNG 을/JKO ////NA";
        List<FwdUser> result = FwdUserStreamParser.parse(strToTest).collect(Collectors.toList());

        assertThat(result.get(0).getFull(), is("하늘을///"));
        assertThat(result.get(0).getAnalyzed(), is("하늘/NNG 을/JKO ////NA"));
    }


    @Test(expected = ResourceMalformedException.class)
    public void FwdUserParser_Malformed_Test() {
        String strToTest = "하늘을\t";
        List<FwdUser> result = FwdUserStreamParser.parse(strToTest).collect(Collectors.toList());
    }

    @Test(expected = ResourceMalformedException.class)
    public void FwdUserParser_InvalidFull_Test() {
        String strToTest = "\t달리/VV 다/EC";
        List<FwdUser> result = FwdUserStreamParser.parse(strToTest).collect(Collectors.toList());
    }


    @Test(expected = ResourceMalformedException.class)
    public void FwdUserParser_InvalidAnalyzed_Test() {
        String strToTest = "달리다\t 다/EC";
        List<FwdUser> result = FwdUserStreamParser.parse(strToTest).collect(Collectors.toList());
    }

    @Test(expected = ResourceMalformedException.class)
    public void FwdUserParser_InvalidAnalyzedTerm_Test() {
        String strToTest = "달리다\t/VV 다/EC";
        List<FwdUser> result = FwdUserStreamParser.parse(strToTest).collect(Collectors.toList());
    }

    @Test(expected = ResourceMalformedException.class)
    public void FwdUserParser_InvalidAnalyzedPos_Test() {
        String strToTest = "달리다\t달리/ABC 다/EC";
        List<FwdUser> result = FwdUserStreamParser.parse(strToTest).collect(Collectors.toList());
    }

}

package kr.co.shineware.nlp.komoran.issue;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AnalyzeIssues {

    private Komoran komoran;

    @Before
    public void initKomoran() {
        this.komoran = new Komoran(DEFAULT_MODEL.LIGHT);
    }

    @Test
    //https://github.com/shin285/KOMORAN/issues/74
    public void issue74() {
        Assert.assertNotEquals(komoran.analyze("대학로").getPlainText(),"대학/NNG 로/JKB");
        Assert.assertNotEquals(komoran.analyze("미남로").getPlainText(),"미남/NNG 로/JKB");
        Assert.assertNotEquals(komoran.analyze("구남로").getPlainText(),"구남/NNG 로/JKB");

        Assert.assertNotEquals(komoran.analyze("대학로", 2).get(0).getPlainText(),"대학/NNG 로/JKB");
        Assert.assertNotEquals(komoran.analyze("미남로", 2).get(0).getPlainText(),"미남/NNG 로/JKB");
        Assert.assertNotEquals(komoran.analyze("구남로", 2).get(0).getPlainText(),"구남/NNG 로/JKB");
    }
}

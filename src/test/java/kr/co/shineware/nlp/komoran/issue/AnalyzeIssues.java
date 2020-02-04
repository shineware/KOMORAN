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
        Assert.assertNotEquals("대학/NNG 로/JKB", komoran.analyze("대학로").getPlainText());
        Assert.assertNotEquals("미남/NNG 로/JKB", komoran.analyze("미남로").getPlainText());
        Assert.assertNotEquals("구남/NNG 로/JKB", komoran.analyze("구남로").getPlainText());

        Assert.assertNotEquals("대학/NNG 로/JKB", komoran.analyze("대학로", 2).get(0).getPlainText());
        Assert.assertNotEquals("미남/NNG 로/JKB", komoran.analyze("미남로", 2).get(0).getPlainText());
        Assert.assertNotEquals("구남/NNG 로/JKB", komoran.analyze("구남로", 2).get(0).getPlainText());
    }

    @Test
    //https://github.com/shin285/KOMORAN/issues/75
    public void issue75() {
        Assert.assertNotEquals("가/VV 을/ETM", komoran.analyze("가을").getPlainText());
        Assert.assertNotEquals("가/VV 을/ETM", komoran.analyze("가을", 2).get(0).getPlainText());
    }

    @Test
    //https://github.com/shin285/KOMORAN/issues/77
    public void issue77() {
        System.out.println(komoran.analyze("황토은").getPlainText());
        System.out.println(komoran.analyze("황토은", 2).get(0).getPlainText());
        Assert.assertNotEquals("황토/NNG 은/JX", komoran.analyze("황토은").getPlainText());
        Assert.assertNotEquals("황토/NNG 은/JX", komoran.analyze("황토은", 2).get(0).getPlainText());
        Assert.assertNotEquals("황토/NNG 은/JC", komoran.analyze("황토은").getPlainText());
        Assert.assertNotEquals("황토/NNG 은/JC", komoran.analyze("황토은", 2).get(0).getPlainText());
        Assert.assertEquals("황토/NNG 은/NNG", komoran.analyze("황토은").getPlainText());
        Assert.assertEquals("황토/NNG 은/NNG", komoran.analyze("황토은", 2).get(0).getPlainText());
    }
}

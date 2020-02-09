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

        String analyzeTarget = "황토은";

        String analyzeResult = komoran.analyze(analyzeTarget).getPlainText();
        String nBestAnalyzeResult = komoran.analyze(analyzeTarget, 2).get(0).getPlainText();

        Assert.assertEquals(analyzeResult, nBestAnalyzeResult);
        Assert.assertNotEquals("황토/NNG 은/JX", analyzeResult);
        Assert.assertNotEquals("황토/NNG 은/JX", nBestAnalyzeResult);
        Assert.assertNotEquals("황토/NNG 은/JC", analyzeResult);
        Assert.assertNotEquals("황토/NNG 은/JC", nBestAnalyzeResult);
        Assert.assertEquals("황토/NNG 은/NNG", analyzeResult);
        Assert.assertEquals("황토/NNG 은/NNG", nBestAnalyzeResult);
    }

    @Test
    //https://github.com/shin285/KOMORAN/issues/88
    public void issue88() {
        //TODO : 테스트 케이스 작성
        assertNotEqualsOfAnalyzeResult("가위은", "가위/NNG 은/JKS");
        assertNotEqualsOfAnalyzeResult("마늘는", "마늘/NNG 는/JKS");

        assertNotEqualsOfAnalyzeResult("가위은", "가위/NNG 이/JKC");
        assertNotEqualsOfAnalyzeResult("마늘는", "마늘/NNG 가/JKC");

        assertNotEqualsOfAnalyzeResult("가위을", "가위/NNG 을/JKO");
        assertNotEqualsOfAnalyzeResult("마늘를", "마늘/NNG 를/JKO");

        assertNotEqualsOfAnalyzeResult("가위아", "가위/NNG 아/JKV");
        assertNotEqualsOfAnalyzeResult("마늘야", "마늘/NNG 야/JKV");

        assertNotEqualsOfAnalyzeResult("가위과", "가위/NNG 과/JC");
        assertNotEqualsOfAnalyzeResult("가위이나", "가위/NNG 이나/JC");
        assertNotEqualsOfAnalyzeResult("가위이랑", "가위/NNG 이랑/JC");
        assertNotEqualsOfAnalyzeResult("마늘와", "마늘/NNG 와/JC");
        assertNotEqualsOfAnalyzeResult("마늘나", "마늘/NNG 나/JC");
        assertNotEqualsOfAnalyzeResult("마늘랑", "마늘/NNG 랑/JC");

        assertNotEqualsOfAnalyzeResult("가위과", "가위/NNG 과/JKB");
        assertNotEqualsOfAnalyzeResult("가위으로", "가위/NNG 으로/JKB");
        assertNotEqualsOfAnalyzeResult("마늘와", "마늘/NNG 와/JKB");
        assertNotEqualsOfAnalyzeResult("마늘로", "마늘/NNG 로/JKB");

        assertNotEqualsOfAnalyzeResult("가위은", "가위/NNG 은/JX");
        assertNotEqualsOfAnalyzeResult("가위이란", "가위/NNG 이란/JX");
        assertNotEqualsOfAnalyzeResult("마늘는", "마늘/NNG 는/JX");
        assertNotEqualsOfAnalyzeResult("마늘란", "마늘/NNG 란/JX");
    }

    private void assertNotEqualsOfAnalyzeResult(String input, String unexpected) {
        String analyzeResult = komoran.analyze(input).getPlainText();
        String nBestAnalyzeResult = komoran.analyze(input, 2).get(0).getPlainText();
        Assert.assertEquals(analyzeResult, nBestAnalyzeResult);
        Assert.assertNotEquals(unexpected, analyzeResult);
        Assert.assertNotEquals(unexpected, nBestAnalyzeResult);
    }
}

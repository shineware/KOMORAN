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
        this.komoran = new Komoran(DEFAULT_MODEL.STABLE);
    }

    @Test
    public void selfIssues() {
        //TODO : https://github.com/shineware/KOMORAN/issues/94
//        assertEqualsOfAnalyzeResult("재밌네요", "재미/NNG 있/VA 네요");
        assertEqualsOfAnalyzeResult("거리에는", "거리/NNG 에/JKB 는/JX");
        //TODO : https://github.com/shineware/KOMORAN/issues/94 재밌/VA와 재미있/VA에 대한 올바른 분석 결과가 아님. 테스트 케이스 수정 필요
        assertEqualsOfAnalyzeResult("업데이트했어요ㅋㅋㅋㅋ 재밌네요", "업데이트했어요ㅋㅋㅋㅋ/NA 재미있/VA 네요/EC");
        assertEqualsOfAnalyzeResult("하ㅎ 재밌었어요 캡틴마블", "하/NNG ㅎ/XSV 재미있/VA 었/EP 어요/EC 캡틴/NNG 마블/NNG");
    }

    @Test
    //https://github.com/shin285/KOMORAN/issues/74
    public void issue74() {
        assertNotEqualsOfAnalyzeResult("대학로", "대학/NNG 로/JKB");
        assertNotEqualsOfAnalyzeResult("미남로", "미남/NNG 로/JKB");
        assertNotEqualsOfAnalyzeResult("구남로", "구남/NNG 로/JKB");

        assertNotEqualsOfAnalyzeResult("대학로", "대학/NNG 로/JKB");
        assertNotEqualsOfAnalyzeResult("미남로", "미남/NNG 로/JKB");
        assertNotEqualsOfAnalyzeResult("구남로", "구남/NNG 로/JKB");
    }

    @Test
    //https://github.com/shin285/KOMORAN/issues/75
    public void issue75() {
        assertNotEqualsOfAnalyzeResult("가을", "가/VV 을/ETM");
    }

    @Test
    //https://github.com/shin285/KOMORAN/issues/77
    public void issue77() {
        assertNotEqualsOfAnalyzeResult("황토은", "황토/NNG 은/JX");
        assertNotEqualsOfAnalyzeResult("황토은", "황토/NNG 은/JC");
        assertEqualsOfAnalyzeResult("황토은", "황토/NNG 은/NNG");
        assertEqualsOfAnalyzeResult("황토은", "황토/NNG 은/NNG");
    }

    @Test
    //https://github.com/shin285/KOMORAN/issues/88
    public void issue88() {
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

    private void assertEqualsOfAnalyzeResult(String input, String unexpected) {
        String analyzeResult = komoran.analyze(input).getPlainText();
        String nBestAnalyzeResult = komoran.analyze(input, 2).get(0).getPlainText();
        Assert.assertEquals(analyzeResult, nBestAnalyzeResult);
        Assert.assertEquals(unexpected, analyzeResult);
        Assert.assertEquals(unexpected, nBestAnalyzeResult);
    }
}

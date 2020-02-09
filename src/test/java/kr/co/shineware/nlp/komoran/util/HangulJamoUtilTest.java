package kr.co.shineware.nlp.komoran.util;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
public class HangulJamoUtilTest {

    @Test
    public void toHangulCompatibilityJamo() {
        String source = "나쁨에\t나쁘/VA ᄆ/ETN 에/JKB:3\n" +
                "뿌연\t뿌옇/VA ᆫ/ETM:40\n" +
                "쓰임이나\t쓰이/VV ᄆ/ETN 이나/JC:1\n" +
                "얽혀져\t얽히/VV 어/EC 지/VX 어/EC:1\n" +
                "뿌여\t뿌옇/VA:1\n" +
                "읽힌\t읽히/VV ᆫ/ETM:4\n" +
                "흘려보낼\t흘려보내/VV ᆯ/ETM:2\n" +
                "치달릴\t치달리/VV ᆯ/ETM:2\n" +
                "흘려보낸\t흘려보내/VV ᆫ/ETM:1\n" +
                "들러간\t들러가/VV ᆫ/ETM:1";
        System.out.println(hasOnlyValidHangul(source));
        System.out.println(source);
        System.out.println();
        System.out.println(HangulJamoUtil.ToHangulCompatibilityJamo(source));
    }

    private boolean hasOnlyValidHangul(String line) {
        List<Character> jamoList = HangulJamoUtil.getHangulJamos(line);
        if (jamoList.size() != 0) {
            return false;
        }
        return true;
    }
}
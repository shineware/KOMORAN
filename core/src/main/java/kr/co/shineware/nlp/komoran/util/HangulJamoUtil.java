package kr.co.shineware.nlp.komoran.util;

import java.util.ArrayList;
import java.util.List;

public class HangulJamoUtil {

    // Chosung: 0x1100 ~ 0x1112 → compatibility jamo
    private static final char[] CHOSUNG_MAP = {
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    // Jungsung: 0x1161 ~ 0x1175 → compatibility jamo
    private static final char[] JUNGSUNG_MAP = {
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    };

    // Jongsung: 0x11A8 ~ 0x11C2 → compatibility jamo
    private static final char[] JONGSUNG_MAP = {
        'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ',
        'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ',
        'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    public static String ToHangulCompatibilityJamo(String source) {
        StringBuilder dest = new StringBuilder();

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);

            if (ch >= 0x1100 && ch <= 0x1112) {
                ch = CHOSUNG_MAP[ch - 0x1100];
            } else if (ch >= 0x1161 && ch <= 0x1175) {
                ch = JUNGSUNG_MAP[ch - 0x1161];
            } else if (ch >= 0x11A8 && ch <= 0x11C2) {
                ch = JONGSUNG_MAP[ch - 0x11A8];
            }

            dest.append(ch);
        }
        return dest.toString();
    }

    public static List<Character> getHangulJamos(String source) {

        List<Character> jamoList = new ArrayList<>();

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (Character.UnicodeBlock.of(ch) == Character.UnicodeBlock.HANGUL_JAMO) {
                jamoList.add(ch);
            }
        }
        return jamoList;
    }
}

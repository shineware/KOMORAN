package kr.co.shineware.nlp.komoran.util;

import java.util.ArrayList;
import java.util.List;

public class HangulJamoUtil {
    public static String ToHangulCompatibilityJamo(String source) {
        StringBuilder dest = new StringBuilder();

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            ch = convertChosungToCompatibilityJamo(ch);
            ch = convertJungsungToCompatibilityJamo(ch);
            ch = convertJongsungToCompatibilityJamo(ch);
            dest.append(ch);
        }
        return dest.toString();
    }

    private static char convertJongsungToCompatibilityJamo(char ch) {
        if(ch == 0x11A8){
            ch = 'ㄱ';
        }
        if(ch == 0x11A9){
            ch = 'ㄲ';
        }
        if(ch == 0x11AA){
            ch = 'ㄳ';
        }
        if(ch == 0x11AB){
            ch = 'ㄴ';
        }
        if(ch == 0x11AC){
            ch = 'ㄵ';
        }
        if(ch == 0x11AD){
            ch = 'ㄶ';
        }
        if(ch == 0x11AE){
            ch = 'ㄷ';
        }
        if(ch == 0x11AF){
            ch = 'ㄹ';
        }
        if(ch == 0x11B0){
            ch = 'ㄺ';
        }
        if(ch == 0x11B1){
            ch = 'ㄻ';
        }
        if(ch == 0x11B2){
            ch = 'ㄼ';
        }
        if(ch == 0x11B3){
            ch = 'ㄽ';
        }
        if(ch == 0x11B4){
            ch = 'ㄾ';
        }
        if(ch == 0x11B5){
            ch = 'ㄿ';
        }
        if(ch == 0x11B6){
            ch = 'ㅀ';
        }
        if(ch == 0x11B7){
            ch = 'ㅁ';
        }
        if(ch == 0x11B8){
            ch = 'ㅂ';
        }
        if(ch == 0x11B9){
            ch = 'ㅄ';
        }
        if(ch == 0x11BA){
            ch = 'ㅅ';
        }
        if(ch == 0x11BB){
            ch = 'ㅆ';
        }
        if(ch == 0x11BC){
            ch = 'ㅇ';
        }
        if(ch == 0x11BD){
            ch = 'ㅈ';
        }
        if(ch == 0x11BE){
            ch = 'ㅊ';
        }
        if(ch == 0x11BF){
            ch = 'ㅋ';
        }
        if(ch == 0x11C0){
            ch = 'ㅌ';
        }
        if(ch == 0x11C1){
            ch = 'ㅍ';
        }
        if(ch == 0x11C2){
            ch = 'ㅎ';
        }
        return ch;
    }

    private static char convertJungsungToCompatibilityJamo(char ch) {
        if(ch == 0x1161){
            ch = 'ㅏ';
        }
        if(ch == 0x1162){
            ch = 'ㅐ';
        }
        if(ch == 0x1163){
            ch = 'ㅑ';
        }
        if(ch == 0x1164){
            ch = 'ㅒ';
        }
        if(ch == 0x1165){
            ch = 'ㅓ';
        }
        if(ch == 0x1166){
            ch = 'ㅔ';
        }
        if(ch == 0x1167){
            ch = 'ㅕ';
        }
        if(ch == 0x1168){
            ch = 'ㅖ';
        }
        if(ch == 0x1169){
            ch = 'ㅗ';
        }
        if(ch == 0x116A){
            ch = 'ㅘ';
        }
        if(ch == 0x116B){
            ch = 'ㅙ';
        }
        if(ch == 0x116C){
            ch = 'ㅚ';
        }
        if(ch == 0x116D){
            ch = 'ㅛ';
        }
        if(ch == 0x116E){
            ch = 'ㅜ';
        }
        if(ch == 0x116F){
            ch = 'ㅝ';
        }
        if(ch == 0x1170){
            ch = 'ㅞ';
        }
        if(ch == 0x1171){
            ch = 'ㅟ';
        }
        if(ch == 0x1172){
            ch = 'ㅠ';
        }
        if(ch == 0x1173){
            ch = 'ㅡ';
        }
        if(ch == 0x1174){
            ch = 'ㅢ';
        }
        if(ch == 0x1175){
            ch = 'ㅣ';
        }
        return ch;
    }

    private static char convertChosungToCompatibilityJamo(char ch) {
        if(ch == 0x1100){
            ch = 'ㄱ';
        }
        if(ch == 0x1101){
            ch = 'ㄲ';
        }
        if(ch == 0x1102){
            ch = 'ㄴ';
        }
        if(ch == 0x1103){
            ch = 'ㄷ';
        }
        if(ch == 0x1104){
            ch = 'ㄸ';
        }
        if(ch == 0x1105){
            ch = 'ㄹ';
        }
        if(ch == 0x1106){
            ch = 'ㅁ';
        }
        if(ch == 0x1107){
            ch = 'ㅂ';
        }
        if(ch == 0x1108){
            ch = 'ㅃ';
        }
        if(ch == 0x1109){
            ch = 'ㅅ';
        }
        if(ch == 0x110A){
            ch = 'ㅆ';
        }
        if(ch == 0x110B){
            ch = 'ㅇ';
        }
        if(ch == 0x110C){
            ch = 'ㅈ';
        }
        if(ch == 0x110D){
            ch = 'ㅉ';
        }
        if(ch == 0x110E){
            ch = 'ㅊ';
        }
        if(ch == 0x110F){
            ch = 'ㅋ';
        }
        if(ch == 0x1110){
            ch = 'ㅌ';
        }
        if(ch == 0x1111){
            ch = 'ㅍ';
        }
        if(ch == 0x1112){
            ch = 'ㅎ';
        }
        return ch;
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

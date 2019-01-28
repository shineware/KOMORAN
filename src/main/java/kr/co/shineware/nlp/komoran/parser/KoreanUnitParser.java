/*******************************************************************************
 * KOMORAN 3.0 - Korean Morphology Analyzer
 *
 * Copyright 2015 Shineware http://www.shineware.co.kr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package kr.co.shineware.nlp.komoran.parser;

import kr.co.shineware.nlp.komoran.interfaces.UnitParser;
import kr.co.shineware.util.common.model.Pair;

import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KoreanUnitParser implements UnitParser {
    public static char[] ChoSung = {0x3131, 0x3132, 0x3134, 0x3137, 0x3138,
            0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147, 0x3148,
            0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};
    public static char[] JungSung = {0x314f, 0x3150, 0x3151, 0x3152, 0x3153,
            0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a, 0x315b,
            0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162, 0x3163};
    public static char[] JongSung = {0x0000, 0x3131, 0x3132, 0x3133, 0x3134,
            0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d,
            0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146,
            0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};


    public enum UnitType {
        CHOSUNG, JUNGSUNG, JONGSUNG, OTHER
    }


    public List<Pair<Character, UnitType>> parseWithType(String str) {
        List<Pair<Character, UnitType>> result = new ArrayList<>();

        int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
            if (block == UnicodeBlock.HANGUL_SYLLABLES) {
                int cho, jung, jong, tmp;
                tmp = ch - 0xAC00;
                cho = tmp / (21 * 28);
                tmp = tmp % (21 * 28);
                jung = tmp / 28;
                jong = tmp % 28;
                result.add(new Pair<>(ChoSung[cho], UnitType.CHOSUNG));
                result.add(new Pair<>(JungSung[jung], UnitType.JUNGSUNG));
                if (jong != 0) {
                    result.add(new Pair<>(JongSung[jong], UnitType.JONGSUNG));
                }
            } else {
                result.add(new Pair<>(ch, UnitType.OTHER));
            }
        }
        return result;
    }

    public String combineWithType(List<Pair<Character, UnitType>> jasoUnitsWithType) {
        int chosung = 0;
        int jungsung = 0;
        int jongsung = 0;

        boolean hasBuffer = false;

        StringBuilder result = new StringBuilder();

        for (Pair<Character, UnitType> characterUnitTypePair : jasoUnitsWithType) {
            if (characterUnitTypePair.getSecond() == UnitType.CHOSUNG) {
                if (hasBuffer) {
                    result.append((char) (0xac00 + chosung * 588 + jungsung * 28 + jongsung));
                    jungsung = 0;
                    jongsung = 0;
                }
                chosung = Arrays.binarySearch(ChoSung, characterUnitTypePair.getFirst());
                hasBuffer = true;
            } else if (characterUnitTypePair.getSecond() == UnitType.JUNGSUNG) {
                jungsung = Arrays.binarySearch(JungSung, characterUnitTypePair.getFirst());
                hasBuffer = true;
            } else if (characterUnitTypePair.getSecond() == UnitType.JONGSUNG) {
                jongsung = Arrays.binarySearch(JongSung, characterUnitTypePair.getFirst());
                hasBuffer = true;
            } else {
                if (hasBuffer) {
                    result.append((char) (0xac00 + chosung * 588 + jungsung * 28 + jongsung));
                    chosung = 0;
                    jungsung = 0;
                    jongsung = 0;
                }
                result.append(characterUnitTypePair.getFirst());
                hasBuffer = false;
            }
        }
        if (hasBuffer) {
            result.append((char) (0xac00 + chosung * 588 + jungsung * 28 + jongsung));
        }

        return result.toString();
    }

    @Override
    public String parse(String str) {

        StringBuffer result = new StringBuffer();

        int i = 0;
        int length = str.length();
        for (i = 0; i < length; i++) {
            char ch = str.charAt(i);
            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
            if (block == UnicodeBlock.HANGUL_SYLLABLES) {
                int cho, jung, jong, tmp;
                tmp = ch - 0xAC00;
                cho = tmp / (21 * 28);
                tmp = tmp % (21 * 28);
                jung = tmp / 28;
                jong = tmp % 28;
                result.append(ChoSung[cho]);
                result.append(JungSung[jung]);
                if (jong != 0) {
                    result.append(JongSung[jong]);
                }
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public List<Pair<Integer, Integer>> getSyllableAreaList(String str) {
        List<Pair<Integer, Integer>> syllableAreaList = new ArrayList<Pair<Integer, Integer>>();
        StringBuffer result = new StringBuffer();
        int i = 0;
        int length = str.length();
        //0xAC00+cho*588+jung*28+jong
        int prevIdx = 0;
        for (i = 1; i < length; i++) {
            char ch = str.charAt(i);
            int jungsung = Arrays.binarySearch(JungSung, ch);
            if (jungsung >= 0) { //if current character is jungsung

                //i-1 = 초성 인덱스
                int chosung = Arrays.binarySearch(ChoSung, str.charAt(i - 1)); //find chosung
                if (chosung < 0) {
                    continue;
                }

                //append not combined string to result
                //prevIdx ~ i-1 = 자소 조합이 안된 스트링 구
                result.append(str, prevIdx, i - 1);
                if (str.substring(prevIdx, i - 1).length() != 0) {
                    this.appendSplitedSyllableList(prevIdx, i - 1, syllableAreaList);
                }

                int jongsung = 0;

                //i+1 = 종성 인덱스
                if (i + 1 < length) {
                    jongsung = Arrays.binarySearch(JongSung, str.charAt(i + 1));
                }

                //i+2가 중성인지 찾는 분기
                //i+2가 중성이라면 현재 i+1은 종성이 아닌 초성이 됨
                //이 때는 (i-1)~i까지가 하나의 음절로 구성됨
                if (i + 2 < length && Arrays.binarySearch(JungSung, str.charAt(i + 2)) >= 0) {
                    jongsung = 0;
                }
                //종성이 없는 경우
                if (jongsung < 0) {
                    jongsung = 0;
                }
                char syllable = (char) (0xac00 + chosung * 588 + jungsung * 28 + jongsung);
                result.append(syllable);

                int beginIndex = i - 1;

                if (jongsung > 0) {
                    i++;
                }
                int endIndex = i;
                syllableAreaList.add(new Pair<Integer, Integer>(beginIndex, endIndex + 1));
                prevIdx = i + 1;
            }
        }
        if (prevIdx < length) {
            result.append(str.substring(prevIdx));
            this.appendSplitedSyllableList(prevIdx, length, syllableAreaList);
        }
        return syllableAreaList;
    }


    private void appendSplitedSyllableList(int prevIdx,
                                           int endIdx, List<Pair<Integer, Integer>> targetList) {
        for (int i = prevIdx; i < endIdx; i++) {
            targetList.add(new Pair<Integer, Integer>(i, i + 1));
        }
    }

    @Override
    public String combine(String str) {

        StringBuffer result = new StringBuffer();

        int i = 0;
        int length = str.length();
        //0xAC00+cho*588+jung*28+jong
        int prevIdx = 0;
        for (i = 1; i < length; i++) {
            char ch = str.charAt(i);
            int jungsung = Arrays.binarySearch(JungSung, ch);
            if (jungsung >= 0) { //if current character is jungsung

                //i-1 = 초성 인덱스
                int chosung = Arrays.binarySearch(ChoSung, str.charAt(i - 1)); //find chosung
                if (chosung < 0) {
                    continue;
                }

                //append not combined string to result
                //prevIdx ~ i-1 = 자소 조합이 안된 스트링 구
                result.append(str, prevIdx, i - 1);
//				if(str.substring(prevIdx, i-1).length() != 0){
//					System.out.println("["+prevIdx+","+(i-2)+"] : "+str.substring(prevIdx, i-1));
//				}

                int jongsung = 0;

                //i+1 = 종성 인덱스
                if (i + 1 < length) {
                    jongsung = Arrays.binarySearch(JongSung, str.charAt(i + 1));
                }

                //i+2가 중성인지 찾는 분기
                //i+2가 중성이라면 현재 i+1은 종성이 아닌 초성이 됨
                //이 때는 (i-1)~i까지가 하나의 음절로 구성됨
                if (i + 2 < length && Arrays.binarySearch(JungSung, str.charAt(i + 2)) >= 0) {
                    jongsung = 0;
                }
                //종성이 없는 경우
                if (jongsung < 0) {
                    jongsung = 0;
                }
                char syllable = (char) (0xac00 + chosung * 588 + jungsung * 28 + jongsung);
                result.append(syllable);
                //i는 중성
                if (jongsung > 0) {
                    i++;
                }

                prevIdx = i + 1;
            }
        }
        if (prevIdx < length) {
            result.append(str.substring(prevIdx));
        }

        return result.toString();
    }
}

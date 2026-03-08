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
 ******************************************************************************/
package kr.co.shineware.nlp.komoran.parser;

import kr.co.shineware.nlp.komoran.interfaces.UnitParser;
import kr.co.shineware.util.common.model.Pair;

import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // HashMap-based reverse lookups (replacing binarySearch)
    private static final Map<Character, Integer> choSungIndex = new HashMap<>();
    private static final Map<Character, Integer> jungSungIndex = new HashMap<>();
    private static final Map<Character, Integer> jongSungIndex = new HashMap<>();

    static {
        for (int i = 0; i < ChoSung.length; i++) {
            choSungIndex.put(ChoSung[i], i);
        }
        for (int i = 0; i < JungSung.length; i++) {
            jungSungIndex.put(JungSung[i], i);
        }
        for (int i = 0; i < JongSung.length; i++) {
            jongSungIndex.put(JongSung[i], i);
        }
    }

    private static int getChoSungIndex(char ch) {
        Integer idx = choSungIndex.get(ch);
        return idx != null ? idx : -1;
    }

    private static int getJungSungIndex(char ch) {
        Integer idx = jungSungIndex.get(ch);
        return idx != null ? idx : -1;
    }

    private static int getJongSungIndex(char ch) {
        Integer idx = jongSungIndex.get(ch);
        return idx != null ? idx : -1;
    }


    public enum UnitType {
        CHOSUNG, JUNGSUNG, JONGSUNG, OTHER
    }


    public List<Pair<Character, UnitType>> parseWithType(String str) {
        List<Pair<Character, UnitType>> result = new ArrayList<>();

        int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            UnicodeBlock block = UnicodeBlock.of(ch);
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
                chosung = getChoSungIndex(characterUnitTypePair.getFirst());
                hasBuffer = true;
            } else if (characterUnitTypePair.getSecond() == UnitType.JUNGSUNG) {
                jungsung = getJungSungIndex(characterUnitTypePair.getFirst());
                hasBuffer = true;
            } else if (characterUnitTypePair.getSecond() == UnitType.JONGSUNG) {
                jongsung = getJongSungIndex(characterUnitTypePair.getFirst());
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

        StringBuilder result = new StringBuilder();

        int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            UnicodeBlock block = UnicodeBlock.of(ch);
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
        List<Pair<Integer, Integer>> syllableAreaList = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        int length = str.length();
        int prevIdx = 0;
        for (int i = 1; i < length; i++) {
            char ch = str.charAt(i);
            int jungsung = getJungSungIndex(ch);
            if (jungsung >= 0) {

                int chosung = getChoSungIndex(str.charAt(i - 1));
                if (chosung < 0) {
                    continue;
                }

                result.append(str, prevIdx, i - 1);
                if (i - 1 - prevIdx > 0) {
                    this.appendSplitedSyllableList(prevIdx, i - 1, syllableAreaList);
                }

                int jongsung = 0;

                if (i + 1 < length) {
                    jongsung = getJongSungIndex(str.charAt(i + 1));
                }

                if (i + 2 < length && getJungSungIndex(str.charAt(i + 2)) >= 0) {
                    jongsung = 0;
                }
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
                syllableAreaList.add(new Pair<>(beginIndex, endIndex + 1));
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
            targetList.add(new Pair<>(i, i + 1));
        }
    }

    @Override
    public String combine(String str) {

        StringBuilder result = new StringBuilder();

        int length = str.length();
        int prevIdx = 0;
        for (int i = 1; i < length; i++) {
            char ch = str.charAt(i);
            int jungsung = getJungSungIndex(ch);
            if (jungsung >= 0) {

                int chosung = getChoSungIndex(str.charAt(i - 1));
                if (chosung < 0) {
                    continue;
                }

                result.append(str, prevIdx, i - 1);

                int jongsung = 0;

                if (i + 1 < length) {
                    jongsung = getJongSungIndex(str.charAt(i + 1));
                }

                if (i + 2 < length && getJungSungIndex(str.charAt(i + 2)) >= 0) {
                    jongsung = 0;
                }
                if (jongsung < 0) {
                    jongsung = 0;
                }
                char syllable = (char) (0xac00 + chosung * 588 + jungsung * 28 + jongsung);
                result.append(syllable);
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

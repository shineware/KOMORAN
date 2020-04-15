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
package kr.co.shineware.nlp.komoran.constant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * komoran에서 사용되는 SYMBOL에 대한 정의<br>
 *
 * @author Junsoo Shin
 * @version 2.1
 * @since 2.1
 */
public class SYMBOL {

    private static final String EP = "EP";

    //Begin Of Eojeol
    public static final String BOE = "BOE";
    //End Of Eojeol
    public static final String EOE = "EOE";
    public static final String SPACE = "<sp>";
    public static final String NA = "NA";

    public static final String NUMBER = "<number>";
    public static final String SW = "SW";
    public static final String SF = "SF";
    public static final String EC = "EC";
    public static final String EF = "EF";
    public static final String JKO = "JKO";
    public static final String JX = "JX";
    public static final String ETM = "ETM";
    public static final String JKS = "JKS";
    public static final String JKC = "JKC";
    public static final String IRREGULAR = "IRR";
    public static final int IRREGULAR_ID = -1;
    public static final String SS = "SS";
    public static final String NNG = "NNG";
    public static final String NNP = "NNP";
    public static final String NNB = "NNB";

    public static final String JKB = "JKB";
    public static final String VV = "VV";
    public static final String VA = "VA";
    public static final String VX = "VX";
    public static final String VCP = "VCP";
    public static final String VCN = "VCN";
    public static final String NP = "NP";
    public static final String JC = "JC";
    public static final String JKV = "JKV";
    public static final String JKG = "JKG";
    public static final String NR = "NR";
    public static final String ETN = "ETN";

    public static final Set<String> NOUN_SET = new HashSet<>(Arrays.asList(SYMBOL.NNG, SYMBOL.NNP, SYMBOL.NNB, SYMBOL.NP, SYMBOL.NR));
    public static final Set<String> EOMI_SET = new HashSet<>(Arrays.asList(SYMBOL.EP, SYMBOL.EC, SYMBOL.EF, SYMBOL.ETN, SYMBOL.ETM));
    public static final Set<String> JOSA_SET = new HashSet<>(Arrays.asList(SYMBOL.JC, SYMBOL.JKB, SYMBOL.JKC, SYMBOL.JKG, SYMBOL.JKO, SYMBOL.JKS, SYMBOL.JKV, SYMBOL.JX));

}

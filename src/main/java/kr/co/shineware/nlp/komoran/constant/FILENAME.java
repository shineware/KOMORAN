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

/**
 * KOMORAN 내부에서 사용되는 파일명과 관련된 상수 클래스입니다.
 * 아래 내용을 변경하지 마십시오
 */
public class FILENAME {

    public static final String GRAMMAR="grammar.in";
    public static final String WORD_DIC="dic.word";
    public static final String IRREGULAR_DIC="dic.irregular";

    public static final String POS_TABLE="pos.table";

    public static final String OBSERVATION="observation.model";
    public static final String TRANSITION="transition.model";
    public static final String IRREGULAR_MODEL="irregular.model";
    public static final String EXPERIMENT_MODEL = "models_full";
    public static final String STABLE_MODEL = "models_light";
}

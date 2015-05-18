/*
 * KOMORAN 2.0 - Korean Morphology Analyzer
 *
 * Copyright 2014 Shineware http://www.shineware.co.kr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kr.co.shineware.nlp.komoran.constant;

/**
 * Parsing에서 사용할 품사별 score 점수
 * 단, SL, SN, SH는 rule parsing 에서만 사용함
 * @author Junsoo Shin
 * @version 2.1.1
 * @since 2.1.1
 *
 */
public class SCORE {
	public static final double NA = -10000.0;
	public static final double SL = -1.00;
	public static final double SN = -1.00;
	public static final double SH = -1.00;
	public static final double SW = -10000.0;
}

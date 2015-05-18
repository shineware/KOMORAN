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
package kr.co.shineware.nlp.komoran.parser;

import java.lang.Character.UnicodeBlock;
import java.util.Arrays;

import kr.co.shineware.nlp.komoran.interfaces.UnitParser;

public class KoreanUnitParser implements UnitParser{
	public static char[] ChoSung = { 0x3131, 0x3132, 0x3134, 0x3137, 0x3138,
		0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147, 0x3148,
		0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e };
	public static char[] JungSung = { 0x314f, 0x3150, 0x3151, 0x3152, 0x3153,
		0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a, 0x315b,
		0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162, 0x3163 };
	public static char[] JongSung = { 0x0000, 0x3131, 0x3132, 0x3133, 0x3134,
		0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d,
		0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146,
		0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e };
	
	
	@Override
	public String parse(String str) {
		
		StringBuffer result = new StringBuffer();
		
		int i=0;
		int length = str.length();
		for(i=0;i<length;i++){
			char ch = str.charAt(i);
			Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
			if(block == UnicodeBlock.HANGUL_SYLLABLES){
				int cho,jung,jong,tmp;
				tmp = ch - 0xAC00;
				cho = tmp / (21*28);
				tmp = tmp % (21*28);			
				jung = tmp / 28;
				jong = tmp % 28;
				result.append(ChoSung[cho]);
				result.append(JungSung[jung]);
				if(jong != 0){
					result.append(JongSung[jong]);
				}
			}else{
				result.append(ch);
			}
		}
		return result.toString();
	}

	@Override
	public String combine(String str) {
		
		StringBuffer result = new StringBuffer();
		
		int i=0;
		int length = str.length();
		//0xAC00+cho*588+jung*28+jong
		int prevIdx = 0;
		for(i=1;i<length;i++){
			char ch = str.charAt(i);
			int jungsung = Arrays.binarySearch(JungSung, ch);
			if(jungsung >= 0){ //if current character is jungsung
				
				int chosung = Arrays.binarySearch(ChoSung, str.charAt(i-1)); //find chosung
				if(chosung < 0){
					continue;
				}
				
				//append not combined string to result
				result.append(str.substring(prevIdx, i-1));				
				
				int jongsung = 0;
				if(i+1 < length){
					jongsung = Arrays.binarySearch(JongSung, str.charAt(i+1));
				}
				
				if(i+2 < length && Arrays.binarySearch(JungSung, str.charAt(i+2)) >= 0){
					jongsung = 0;
					i--;
				}
				if(jongsung < 0){
					jongsung = 0;
					i--;
				}
				char syllable = (char) (0xac00+chosung*588+jungsung*28+jongsung);
				result.append(syllable);
				i++;
				prevIdx = i+1;
			}
		}
		if(prevIdx < length){
			result.append(str.substring(prevIdx));
		}
		
		return result.toString();
	}
}

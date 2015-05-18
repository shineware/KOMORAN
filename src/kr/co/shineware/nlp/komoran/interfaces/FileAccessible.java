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
package kr.co.shineware.nlp.komoran.interfaces;

/**
 * 
 * @author Junsoo Shin <jsshin@shineware.co.kr>
 * @version 2.1
 * @since 2.1
 *
 */
public interface FileAccessible {
	/**
	 * 현재 사용되고 있는 데이터를 filename에 저장
	 * @param filename
	 */
	public void save(String filename);
	/**
	 * 저장된 filename으로부터 데이터 로드
	 * @param filename
	 */
	public void load(String filename);
}

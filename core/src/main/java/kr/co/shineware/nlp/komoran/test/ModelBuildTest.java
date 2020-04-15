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
package kr.co.shineware.nlp.komoran.test;

import kr.co.shineware.nlp.komoran.modeler.builder.ModelBuilder;

import java.io.File;

public class ModelBuildTest {

	public static void main(String[] args) {
		modelSave(false);
		modelSave(true);
		modelLoad();
	}

	@SuppressWarnings("deprecation")
	private static void modelLoad() {
		ModelBuilder builder = new ModelBuilder();
		builder.load("models_light");
	}

	private static void modelSave(boolean includeWikiTitle) {
		ModelBuilder builder = new ModelBuilder();
		if(includeWikiTitle) {
			builder.setExternalDic("user_data" + File.separator + "wiki.titles");
		}
		builder.buildPath("corpus_build");

		String modelPath = "models";
		if(includeWikiTitle){
			modelPath += "_full";
		}else{
			modelPath += "_light";
		}
		builder.save(modelPath);
	}

}

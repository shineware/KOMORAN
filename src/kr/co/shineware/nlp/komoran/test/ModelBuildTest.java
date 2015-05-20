package kr.co.shineware.nlp.komoran.test;

import kr.co.shineware.nlp.komoran.modeler.builder.ModelBuilder;

public class ModelBuildTest {

	public static void main(String[] args) {
		modelSave();
		modelLoad();
	}

	@SuppressWarnings("deprecation")
	private static void modelLoad() {
		ModelBuilder builder = new ModelBuilder();
		builder.load("models");
	}

	private static void modelSave() {
		ModelBuilder builder = new ModelBuilder();
		builder.buildPath("corpus_build");
		builder.save("models");
	}

}

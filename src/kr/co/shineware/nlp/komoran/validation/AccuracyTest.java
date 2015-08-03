package kr.co.shineware.nlp.komoran.validation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.corpus.builder.CorpusBuilder;
import kr.co.shineware.nlp.komoran.corpus.parser.CorpusParser;
import kr.co.shineware.nlp.komoran.corpus.parser.model.ProblemAnswerPair;
import kr.co.shineware.nlp.komoran.modeler.builder.ModelBuilder;
import kr.co.shineware.util.common.model.Pair;

public class AccuracyTest {
	public static void main(String[] args) throws Exception{
		buildCorpus();
		buildModel();
		accuracyTest();
	}
	public static void buildModel() {
		ModelBuilder builder = new ModelBuilder();
		builder.buildPath("validation/corpus_build");
		builder.save("validation/model_build");
	}

	public static void buildCorpus() {
		CorpusBuilder builder = new CorpusBuilder();
		builder.setExclusiveIrrRule("resources/irrDic.remove.txt");
		builder.buildPath("validation/sj2003_convert_train", "tag");
		builder.save("validation/corpus_build");
	}
	private static void accuracyTest() throws Exception {
		Komoran komoran = new Komoran("validation/model_build");
//		komoran.setFWDic("user_data/fwd2.user");
		//		long start = System.currentTimeMillis();

		BufferedWriter bw = new BufferedWriter(new FileWriter("validation/incorrection.log.txt"));

		BufferedReader br = new BufferedReader(new FileReader("validation/sj2003_convert_test/sj2003.convert10.tag"));
		String line = null;
		CorpusParser parser = new CorpusParser();

		//for word acc.
		int wordCorrection = 0;
		int wordIncorrection = 0;
		int onlyKoreanwordCorrection = 0;
		int onlyKoreanwordIncorrection = 0;

		//for morph acc.
		int moCorrect = 0;
		int moIncorrect = 0;
		int onlyKoreanMoCorrect = 0;
		int onlyKoreanMoIncorrect = 0;
		boolean hasSpecialLetter = false;
		while((line = br.readLine()) != null){
			line = line.trim();
			if(line.length() == 0)continue;
			ProblemAnswerPair paPair = parser.parse(line);	
			List<Pair<String,String>> komoranResultList = komoran.analyze(paPair.getProblem());
			List<Pair<String,String>> answerList = paPair.getAnswerList();

			hasSpecialLetter = false;
			for (Pair<String, String> pair : answerList) {
				if (pair.getSecond().charAt(0) == 'S') {
					hasSpecialLetter = true;
					break;
				}
			}
			//for word accuracy
			if(komoranResultList != null && komoranResultList.toString().equals(answerList.toString())){
				wordCorrection++;
			}else{
				wordIncorrection++;
			}
			
			
			if(!hasSpecialLetter){				
				if(komoranResultList != null && komoranResultList.toString().equals(answerList.toString())){
					onlyKoreanwordCorrection++;
				}else{
					onlyKoreanwordIncorrection++;
					if(komoranResultList != null){
						if(komoranResultList.toString().contains("있으며")){
							System.out.println("input : "+line);
							System.out.println("output : "+komoranResultList);
							System.out.println("problem : "+paPair.getProblem());
						}
						bw.write(komoranResultList.toString());
					}
					else{
						bw.write("null");
					}

					bw.newLine();
					bw.write(paPair.getProblem()+"\t"+answerList.toString());
					bw.newLine();
					bw.newLine();
				}				
			}

			//for morph accuracy
			if(komoranResultList != null){
				for (Pair<String, String> result : komoranResultList) {
					if (answerList.contains(result)) {
						moCorrect++;
					} else {
						moIncorrect++;
					}
				}
			}else{
				moIncorrect+=answerList.size();
			}
			
			if(!hasSpecialLetter){
				if(komoranResultList != null){
					for (Pair<String, String> result : komoranResultList) {
						if (answerList.contains(result)) {
							onlyKoreanMoCorrect++;
						} else {
							onlyKoreanMoIncorrect++;
						}
					}
				}else{
					onlyKoreanMoIncorrect+=answerList.size();
				}
			}
		}
		bw.close();
		br.close();
		System.out.println("어절(전체) 정답 수 : "+wordCorrection);
		System.out.println("어절(전체) 오답 수 : "+wordIncorrection);
		System.out.println("어절(전체) 정확률  : "+wordCorrection/(double)(wordCorrection+wordIncorrection));		
		System.out.println();
		System.out.println("품사(전체) 정답 수 : "+moCorrect);
		System.out.println("품사(전체) 오답 수 : "+moIncorrect);
		System.out.println("품사(전체) 정확률  : "+moCorrect/(double)(moCorrect+moIncorrect));
		System.out.println();
		System.out.println("어절(한글) 정답 수 : "+onlyKoreanwordCorrection);
		System.out.println("어절(한글) 오답 수 : "+onlyKoreanwordIncorrection);
		System.out.println("어절(한글) 정확률  : "+onlyKoreanwordCorrection/(double)(onlyKoreanwordCorrection+onlyKoreanwordIncorrection));
		System.out.println();
		System.out.println("품사(한글) 정답 수 : "+onlyKoreanMoCorrect);
		System.out.println("품사(한글) 오답 수 : "+onlyKoreanMoIncorrect);
		System.out.println("품사(한글) 정확률  : "+onlyKoreanMoCorrect/(double)(onlyKoreanMoCorrect+onlyKoreanMoIncorrect));		
	}
}

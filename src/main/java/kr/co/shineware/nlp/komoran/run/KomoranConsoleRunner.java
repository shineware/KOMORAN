package kr.co.shineware.nlp.komoran.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.modeler.builder.ModelBuilder;

public class KomoranConsoleRunner {

	public static void main(String[] args) throws Exception {
		if(args.length < 2){
			printUsage();
			return;
		}
		
		Map<String,String> argMap = parseArgs(args);
		if(argMap == null){
			printUsage();
			return;
		}
		
		if(argMap.containsKey("-train") && argMap.containsKey("-model")){
			long begin = System.currentTimeMillis();
			printTrainingInfo(argMap);
			training(argMap);
			long end = System.currentTimeMillis();
			System.out.println("Training elapsed time : "+(end-begin)+"ms");
			
		}else if(argMap.containsKey("-in") && argMap.containsKey("-out") && argMap.containsKey("-model")){
			long begin = System.currentTimeMillis();
			printAnalyzeInfo(argMap);
			analyzing(argMap);
			long end = System.currentTimeMillis();
			System.out.println("Analyze elapsed time : "+(end-begin)+"ms");
		}else{
			printUsage();
			return;
		}
	}

	private static void training(Map<String, String> argMap) {
		ModelBuilder modelBuilder  = new ModelBuilder();
		if(argMap.containsKey("-externalDic")){
			modelBuilder.setExternalDic(argMap.get("-externalDic"));
		}
		modelBuilder.buildPath(argMap.get("-train"));
		modelBuilder.save(argMap.get("-model"));		
	}

	private static void printTrainingInfo(Map<String, String> argMap) {
		System.out.println("training data : "+argMap.get("-train"));
		if(argMap.containsKey("-externalDic")){
			System.out.println("externalDic : "+argMap.get("-externalDic"));
		}
		System.out.println("model path(output dir) : "+argMap.get("-model"));		
	}
	
	private static void analyzing(Map<String, String> argMap) throws Exception {
		Komoran komoran = new Komoran(argMap.get("-model"));
		if(argMap.containsKey("-userDic")){
			komoran.setUserDic(argMap.get("-userDic"));
		}
		if(argMap.containsKey("-fwd")){
			komoran.setFWDic(argMap.get("-fwd"));
		}
		BufferedReader br = new BufferedReader(new FileReader(argMap.get("-in")));
		BufferedWriter bw = new BufferedWriter(new FileWriter(argMap.get("-out")));
		
		String line = null;
		
		while((line = br.readLine()) != null){
			line = line.trim();
			if(line.length() == 0){
				continue;
			}
			bw.write(komoran.analyze(line).getPlainText());
			bw.newLine();
		}
		
		bw.close();
		br.close();
	}

	private static void printAnalyzeInfo(Map<String, String> argMap) {
		System.out.println("input file : "+argMap.get("-in"));
		System.out.println("output file : "+argMap.get("-out"));
		System.out.println("model path : "+argMap.get("-model"));
		if(argMap.containsKey("-userDic")){
			System.out.println("user dic : "+argMap.get("-userDic"));
		}
		if(argMap.containsKey("-fwd")){
			System.out.println("fwd : "+argMap.get("-fwd"));
		}		
	}

	private static Map<String, String> parseArgs(String[] args) {
		try{
			Map<String,String> argMap = new HashMap<>();
			for(int i=0;i<args.length;i++){
				if(args[i].equals("-train")){
					argMap.put(args[i], args[i+1]);
					i++;
				}else if(args[i].equals("-externalDic")){
					argMap.put(args[i], args[i+1]);
					i++;
				}else if(args[i].equals("-model")){
					argMap.put(args[i], args[i+1]);
					i++;
				}else if(args[i].equals("-userDic")){
					argMap.put(args[i], args[i+1]);
					i++;
				}else if(args[i].equals("-fwd")){
					argMap.put(args[i], args[i+1]);
					i++;
				}else if(args[i].equals("-in")){
					argMap.put(args[i], args[i+1]);
					i++;
				}else if(args[i].equals("-out")){
					argMap.put(args[i], args[i+1]);
					i++;
				}else{
					return null;
				}
			}
			return argMap;
		}catch (Exception e){
			return null;
		}
	}

	private static void printUsage() {
		System.out.println("[코모란 트레이닝 방법]");
		System.out.println("\tjava -jar KOMORAN.jar -train '학습 데이터가 포함된 폴더명' [-externalDic '학습 시 추가될 사전'] -model '학습 모델이 저장될 폴더명'");
		System.out.println("[코모란 파일 분석 방법]");
		System.out.println("\tjava -jar KOMORAN.jar -model '학습된 모델이 포함된 폴더명' [-userDic '사용사 사전 파일'] [-fwd '기분석 사전 파일'] -in '분석 대상 파일' -out '분석 결과 파일'");
	}
}

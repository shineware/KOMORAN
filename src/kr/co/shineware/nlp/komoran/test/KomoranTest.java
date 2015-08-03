package kr.co.shineware.nlp.komoran.test;

import java.io.BufferedReader;
import java.io.FileReader;

import kr.co.shineware.nlp.komoran.core.Komoran;

public class KomoranTest {

	public static void main(String[] args) throws Exception {
//		KomoranTest.stressTest();
		KomoranTest.unitTest();
	}
	private static void unitTest() {
		Komoran komoran = new Komoran("models/");
		System.out.println(komoran.analyze("기술로"));
		System.out.println(komoran.analyze("소풍가"));
		System.out.println(komoran.analyze("나은"));
		System.out.println(komoran.analyze("있는"));
		System.out.println(komoran.analyze("780번 손님이 이번에 12일에 Neeyork으로 출국함"));
		
		Komoran komoran2 = new Komoran("validation/model_build/");
		System.out.println(komoran2.analyze("노을"));
	}
	public static void stressTest() throws Exception{
		Komoran komoran = new Komoran("models");
		
		long elapsedTime = 0L;
		long begin = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		BufferedReader br = new BufferedReader(new FileReader("testset/stress.test"));
		String line = null;
		while((line = br.readLine()) != null){
			begin = System.currentTimeMillis();
			komoran.analyze(line);
			end = System.currentTimeMillis();
			elapsedTime += (end-begin);
		}
		br.close();
		
		
		System.out.println(elapsedTime/1000.0);
	}
}

package kr.co.shineware.nlp.komoran.validation;

import java.io.BufferedReader;
import java.io.FileReader;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.util.checker.time.TimeChecker;

public class SpeedValidation {

	public static void main(String[] args) throws Exception {
		Komoran komoran = new Komoran("models");
//		komoran.setUserDic("user_data/wiki_title.user");
//		komoran.setFWDic("user_data/fwd3.user");
		int count = 1000;
		while(true){
			BufferedReader br = new BufferedReader(new FileReader("testset/stress.test"));
			String line = null;
			long begin,end;
			long acc=0l;
			while((line = br.readLine()) != null){
				String[] tmp = line.split(" ");
				for (String t : tmp) {
					begin = System.currentTimeMillis();
					komoran.analyze(t);
					end = System.currentTimeMillis();
					acc += (end-begin);
				}
				tmp = null;
			}
			br.close();
			System.out.println(acc);
//			TimeChecker.printElapsedTime();
			System.out.println();
//			TimeChecker.printElapsedTimeRatio(acc);
			System.out.println();
//			TimeChecker.init();
			if(count-- == 0)break;
		}
	}

}

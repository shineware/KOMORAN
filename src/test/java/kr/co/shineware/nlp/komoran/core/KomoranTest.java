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
package kr.co.shineware.nlp.komoran.core;

import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import kr.co.shineware.util.common.file.FileUtil;
import kr.co.shineware.util.common.model.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class KomoranTest {

	private Komoran komoran;

	@Before
	public void init() throws Exception {
		this.komoran = new Komoran("models_full");
	}

	@Test
	public void threadSafeTest() throws ExecutionException, InterruptedException {
		List<String> lines = FileUtil.load2List("title.txt");


		List<CallableImpl> invokeTargetList = new ArrayList<>();
		List<Future<String>> futureList = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for(int k=0;k<100000;k++) {
			long b = System.currentTimeMillis();
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i).trim();
				executorService.submit(new CallableImpl(komoran, line, i)).get();
			}

			long e = System.currentTimeMillis();
			System.out.println(e - b);
		}

		for(int i=0;i<100;i++){
//			futureList.add(executorService.submit(new CallableImpl(komoran, "감기는 자주 걸리는 병이다.",i)));
		}

		System.out.println("add done");

//		futureList = executorService.invokeAll(invokeTargetList);

		for(int i=0;i<futureList.size();i++){
			System.out.println(futureList.get(i).get());
		}
	}

	@Test
	public void analyze() throws Exception {
		KomoranResult komoranResult = this.komoran.analyze("자주 걸렸던 병이다");
		List<Pair<String,String>> pairList = komoranResult.getList();
		for (Pair<String, String> morphPosPair : pairList) {
			System.out.println(morphPosPair);
		}

		List<String> nounList = komoranResult.getNouns();
		for (String noun : nounList) {
			System.out.println(noun);
		}

		System.out.println(komoranResult.getPlainText());

		List<Token> tokenList = komoranResult.getTokenList();
		for (Token token : tokenList) {
			System.out.println(token);
		}
	}

	@Test
	public void load() throws Exception {
		this.komoran.load("models_full");
	}

	@Test
	public void setFWDic() throws Exception {
		this.komoran.setFWDic("user_data/fwd.user");
		this.komoran.analyze("감사합니다! 바람과 함께 사라지다는 진짜 재밌었어요! nice good!");
	}

	@Test
	public void setUserDic() throws Exception {
		this.komoran.setUserDic("user_data/dic.user");
		System.out.println(this.komoran.analyze("싸이는 가수다").getPlainText());
	}

	private class CallableImpl implements java.util.concurrent.Callable<String>{

		private final Komoran komoran;
		private final String in;
		private final int threadId;

		public CallableImpl(Komoran komoran, String in, int i) {
			this.komoran = komoran;
			this.threadId = i;
			this.in = in;
		}

		@Override
		public String call() throws Exception {
			return threadId+":"+this.in+"->"+komoran.analyze(in).getPlainText();
		}
	}

}
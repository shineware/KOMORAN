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
import kr.co.shineware.nlp.komoran.util.KomoranCallable;
import kr.co.shineware.util.common.file.FileUtil;
import kr.co.shineware.util.common.model.Pair;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class KomoranTest {

	private Komoran komoran;

	@Before
	public void init() throws Exception {
		this.komoran = new Komoran("models_full");
	}

	@Test
	public void singleThreadSpeedTest() throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("analyze_result.txt"));

		List<String> lines = FileUtil.load2List("user_data/wiki.titles");
		List<KomoranResult> komoranList = new ArrayList<>();

		long begin = System.currentTimeMillis();

		int count = 0;

		for (String line : lines) {

			komoranList.add(this.komoran.analyze(line));
			if(komoranList.size() == 1000){
				for (KomoranResult komoranResult : komoranList) {
					bw.write(komoranResult.getPlainText());
					bw.newLine();
				}
				komoranList.clear();
			}
			count++;
			if(count % 10000 == 0){
				System.out.println(count);
			}
		}

		for (KomoranResult komoranResult : komoranList) {
			bw.write(komoranResult.getPlainText());
			bw.newLine();
		}

		long end = System.currentTimeMillis();

		bw.close();

		System.out.println("Elapsed time : "+(end-begin));
	}

	@Test
	public void multiThreadSpeedTest() throws ExecutionException, InterruptedException, IOException {

		BufferedWriter bw = new BufferedWriter(new FileWriter("analyze_result.txt"));

		List<String> lines = FileUtil.load2List("user_data/wiki.titles");

		long begin = System.currentTimeMillis();

		List<Future<KomoranResult>> komoranList = new ArrayList<>();
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
		int count = 0;

		for (String line : lines) {
			KomoranCallable komoranCallable = new KomoranCallable(this.komoran,line);
			komoranList.add(executor.submit(komoranCallable));
			if(komoranList.size() == 1000){
				for (Future<KomoranResult> komoranResultFuture : komoranList) {
					KomoranResult komoranResult = komoranResultFuture.get();
					bw.write(komoranResult.getPlainText());
					bw.newLine();
				}
				komoranList.clear();
			}
			count++;
			if(count % 10000 == 0){
				System.out.println(count);
			}
		}

		for (Future<KomoranResult> komoranResultFuture : komoranList) {
			KomoranResult komoranResult = komoranResultFuture.get();
			bw.write(komoranResult.getPlainText());
			bw.newLine();
		}

		long end = System.currentTimeMillis();

		bw.close();

		System.out.println("Elapsed time : "+(end-begin));
	}

	private void flush(List<Future<KomoranResult>> komoranList, String analyze) throws ExecutionException, InterruptedException {
		for (Future<KomoranResult> komoranResultFuture : komoranList) {
			KomoranResult komoranResult = komoranResultFuture.get();
		}
	}

	@Test
	public void threadSafeTest() throws ExecutionException, InterruptedException {
		List<String> lines = FileUtil.load2List("in.txt");


		List<CallableImpl> invokeTargetList = new ArrayList<>();
		List<Future<String>> futureList = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for(int k=0;k<100000;k++) {
			long b = System.currentTimeMillis();
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i).trim();
				executorService.submit(new CallableImpl(this.komoran, line, i)).get();
			}

			long e = System.currentTimeMillis();
			System.out.println(e - b);
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
		System.out.println(this.komoran.analyze("센트롤이").getPlainText());
		System.out.println(this.komoran.analyze("센트롤이").getTokenList());
		System.out.println(this.komoran.analyze("감싼").getTokenList());
		System.out.println(this.komoran.analyze("싸").getTokenList());
		System.out.println(this.komoran.analyze("난").getTokenList());
		System.out.println(this.komoran.analyze("밀리언 달러 베이비랑").getTokenList());
		System.out.println(this.komoran.analyze("밀리언 달러 베이비랑 바람과 함께 사라지다랑 뭐가 더 재밌었어?").getTokenList());
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
			return threadId+":"+this.in+"->"+komoran.analyze(in).getTokenList();
		}
	}

}
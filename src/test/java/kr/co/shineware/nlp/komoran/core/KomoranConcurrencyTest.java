package kr.co.shineware.nlp.komoran.core;

import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class KomoranConcurrencyTest {

	private static final int THREAD_NUM = 4;
	private static final int QUERY_NUM = 10000;
	private static final String QUERY_TEXT = "바람과 함께 사라지다는 진짜 재밌었어요!";

	private static Komoran komoran;
	private static ExecutorService service;

	@BeforeClass
	public static void setup() {
		komoran = new Komoran("models_full");

		service = Executors.newFixedThreadPool(THREAD_NUM);
	}

	@AfterClass
	public static void teardown() {
		if (service != null) {
			service.shutdown();
		}
	}

	@Test
	public void testSequentialAnalyze() {
		final KomoranResult expectedResult = komoran.analyze(QUERY_TEXT);
		for (int i = 0; i < QUERY_NUM; i++) {
			assertEquals(expectedResult, komoran.analyze(QUERY_TEXT));
		}
	}

	@Test
	public void testConcurrentAnalyze() throws ExecutionException, InterruptedException {
		final KomoranResult expectedResult = komoran.analyze(QUERY_TEXT);
		final List<Future> futures = new ArrayList<>();

		for (int i = 0; i < QUERY_NUM; i++) {
			futures.add(service.submit(new Callable<KomoranResult>() {
				@Override
				public KomoranResult call() throws Exception {
					return komoran.analyze(QUERY_TEXT);
				}
			}));
		}

		for (int i = 0; i < futures.size(); i++) {
			assertEquals(expectedResult, futures.get(i).get());
		}
	}
}

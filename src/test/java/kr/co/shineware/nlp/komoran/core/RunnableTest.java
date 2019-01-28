package kr.co.shineware.nlp.komoran.core;

import kr.co.shineware.nlp.komoran.util.MapRunnable;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunnableTest {
    @Test
    public void collectionRunnableTest() {
        Map<String, Integer> sourceMap = new HashMap<>();
        sourceMap.put("one", 1);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 1; i <= 10; i++) {

            Runnable doThread = new MapRunnable(sourceMap, ""+i, i);
            executorService.execute(doThread);

        }

        executorService.shutdown();

        System.out.println(sourceMap);

    }
}

package kr.co.shineware.nlp.komoran.util;

import java.util.Map;

public class MapRunnable implements Runnable {

    private Map<String, Integer> sourceMap;
    private String key;
    private int value;

    public MapRunnable(Map<String, Integer> sourceMap, String key, int value) {
        this.sourceMap = sourceMap;
        this.key = key;
        this.value = value;
    }

    @Override
    public void run() {
        this.sourceMap.put(this.key, this.value);
    }
}

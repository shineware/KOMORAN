package kr.co.shineware.nlp.komoran.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ElapsedTimeChecker {
    private static final String PREFIX_BEGIN = "START_";
    private static final String PREFIX_END = "END_";
    private static Map<String, Long> timeSnapshotMap = new HashMap<>();
    private static Map<String, Long> accumulatedTimeSnapshotMap = new HashMap<>();
    private static Set<String> timeSnapshotKeys = new HashSet<>();

    public static void checkBeginTime(String key) {
        timeSnapshotKeys.add(key);
        timeSnapshotMap.put(PREFIX_BEGIN + key, System.currentTimeMillis());
    }

    public static void checkEndTime(String key) {
        timeSnapshotMap.put(PREFIX_END + key, System.currentTimeMillis());
        long elapsedTime = accumulatedTimeSnapshotMap.getOrDefault(key,0L);
        long begin = timeSnapshotMap.get(PREFIX_BEGIN+key);
        long end = timeSnapshotMap.get(PREFIX_END+key);
        elapsedTime += (end-begin);
        accumulatedTimeSnapshotMap.put(key, elapsedTime);

    }

    public static void printTimes() {
        for (String timeSnapshotKey : accumulatedTimeSnapshotMap.keySet()) {
            System.out.println(timeSnapshotKey+" : "+accumulatedTimeSnapshotMap.get(timeSnapshotKey));
        }
    }

    public static Long getElapsedTime(String key){
        return accumulatedTimeSnapshotMap.get(key);
    }
}

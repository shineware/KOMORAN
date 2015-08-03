package kr.co.shineware.util.checker.time;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TimeChecker {
	private static Map<String,Long> timeBeginLogMap = new HashMap<>();
	private static Map<String,Long> timeEndLogMap = new HashMap<>();
	private static Map<String,Long> elapsedTimeLogMap = new HashMap<>();
	public static void setBeginFlag(String flagName){
		timeBeginLogMap.put(flagName, System.currentTimeMillis());
	}
	public static void setEndFlag(String flagName){
		timeEndLogMap.put(flagName, System.currentTimeMillis());
	}
	public static long getElapsedTime(String flagName){
		return timeEndLogMap.get(flagName) - timeBeginLogMap.get(flagName);
	}
	public static void appendElapsedTime(String flagName,long elapsedTime){
		Long loggedTime = elapsedTimeLogMap.get(flagName);
		if(loggedTime == null){
			loggedTime = 0L;
		}
		elapsedTimeLogMap.put(flagName, loggedTime+elapsedTime);
	}
	public static long getTotalElapsedTime(String flagName){
		return elapsedTimeLogMap.get(flagName);
	}
	public static void init(){
		timeBeginLogMap.clear();
		timeEndLogMap.clear();
		elapsedTimeLogMap.clear();
	}
	
	public static void printElapsedTime() {
		Set<String> keySet = elapsedTimeLogMap.keySet();
		for (String flagName : keySet) {
			System.out.println(flagName+" time : "+TimeChecker.getTotalElapsedTime(flagName));
		}		
	}
	public static void printElapsedTimeRatio(long totalElapsedTime){
		Set<String> keySet = timeBeginLogMap.keySet();
		for (String flagName : keySet) {
			System.out.println(flagName+" time ratio : "+String.format("%.2f", 100*TimeChecker.getTotalElapsedTime(flagName)/(double)totalElapsedTime)+" %");
		}
	}
}

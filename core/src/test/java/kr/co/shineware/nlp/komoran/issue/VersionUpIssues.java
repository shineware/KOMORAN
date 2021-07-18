package kr.co.shineware.nlp.komoran.issue;

import kr.co.shineware.nlp.komoran.benchmark.PerformanceValidation;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore
public class VersionUpIssues {

    private static PerformanceValidation performanceValidation = new PerformanceValidation();

    @Test
//    https://github.com/shineware/KOMORAN/issues/96
    public void issue96() throws IOException {
        performanceValidation.analyzeSpeedTest();
        performanceValidation.dataStructureSpeedTest();
    }
}

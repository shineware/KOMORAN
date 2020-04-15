package kr.co.shineware.nlp.komoran.run;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.util.ElapsedTimeChecker;
import kr.co.shineware.util.common.file.FileUtil;

import java.util.List;

public class NewKomoranConsoleRunner {
    public static void main(String[] args) throws Exception {

        int thread = Integer.parseInt(args[0]);

        Komoran komoran = new Komoran(DEFAULT_MODEL.STABLE);
        ElapsedTimeChecker.checkBeginTime("FILE_ANALYSIS");
        komoran.analyzeTextFile("wiki.titles", "wiki.titles.out", thread);
        ElapsedTimeChecker.checkEndTime("FILE_ANALYSIS");
        List<String> lines = FileUtil.load2List("wiki.titles");
        ElapsedTimeChecker.checkBeginTime("TEXT_ANALYSIS");
        komoran.analyze(lines, thread);
        ElapsedTimeChecker.checkEndTime("TEXT_ANALYSIS");
        ElapsedTimeChecker.printTimes();

    }
}

package kr.co.shineware.nlp.komoran.admin.service;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceNotFoundException;
import kr.co.shineware.nlp.komoran.admin.exception.ServerErrorException;
import kr.co.shineware.nlp.komoran.admin.util.ModelValidator;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

@Service
@Transactional
public class MorphAnalyzeService {
    private static final Logger logger = LoggerFactory.getLogger(MorphAnalyzeService.class);

    @Value("${models.default.basedir}")
    private String MODELS_BASEDIR;

    @Value("${models.model.dir}")
    private String MODELS_MODELDIR;

    @Value("${files.name.dicuser}")
    private String filenameDicUser;

    @Value("${files.name.fwduser}")
    private String filenameFwdUser;

    @Value("${komoran.options.numThreads}")
    private int numThreads;


    private static Komoran komoran;

    private Komoran userKomoran;


    MorphAnalyzeService() {
        logger.debug("Init Komoran Model...");
        komoran = new Komoran(DEFAULT_MODEL.LIGHT);
        logger.debug("Init Komoran Model... DONE");
    }


    private String analyzeWithLightModel(String strToAnalyze) {
        return komoran.analyze(strToAnalyze).getPlainText();
    }


    private ArrayList<String> analyzeMultipleLinesWithLightModel(String strToAnalyzeWithNewLines) {
        String[] splittedStrs = strToAnalyzeWithNewLines.split("\n");
        List<KomoranResult> analyzedResults = komoran.analyze(Arrays.asList(splittedStrs), numThreads);
        ArrayList<String> results = new ArrayList<String>();

        for (KomoranResult analyzedResult : analyzedResults) {
            results.add(analyzedResult.getPlainText());
        }

        return results;
    }


    private boolean loadUserModel(String modelPathName) {
        ModelValidator.CheckValidUserModel(modelPathName);

        String modelBasePathName = String.join(File.separator, MODELS_BASEDIR, modelPathName);
        File modelPath = new File(modelBasePathName);

        if (!modelPath.exists()) {
            throw new ResourceNotFoundException("존재하지 않는 모델명 [" + modelPathName + "]");
        }

        this.userKomoran = new Komoran(String.join(File.separator, modelBasePathName, MODELS_MODELDIR));

        this.userKomoran.setUserDic(String.join(File.separator, modelBasePathName, filenameDicUser));
        this.userKomoran.setFWDic(String.join(File.separator, modelBasePathName, filenameFwdUser));

        return true;
    }


    public String analyzeWithUserModel(String strToAnalyze, String userModelName) {
        ModelValidator.CheckValidModelName(userModelName);

        String result;

        if ("DEFAULT".equals(userModelName)) {
            result = this.analyzeWithLightModel(strToAnalyze);
            return result;
        }

        try {
            this.loadUserModel(userModelName);
            result = this.userKomoran.analyze(strToAnalyze).getPlainText();
        } catch (NullPointerException e) {
            throw new ServerErrorException("사용자 모델을 이용한 분석 중 에러가 발생하였습니다.\n사전 문제일 수 있습니다.");
        }

        return result;
    }


    public ArrayList<String> analyzeMultipleLinesWithUserModel(String strToAnalyzeWithNewLines, String userModelName) {
        ModelValidator.CheckValidModelName(userModelName);

        ArrayList<String> results;

        if ("DEFAULT".equals(userModelName)) {
            results = this.analyzeMultipleLinesWithLightModel(strToAnalyzeWithNewLines);
            return results;
        }

        try {
            this.loadUserModel(userModelName);
            String[] splittedStrs = strToAnalyzeWithNewLines.split("\n");
            List<KomoranResult> analyzedResults = this.userKomoran.analyze(Arrays.asList(splittedStrs), numThreads);
            results = new ArrayList<String>();

            for (KomoranResult analyzedResult : analyzedResults) {
                results.add(analyzedResult.getPlainText());
            }
        } catch (NullPointerException e) {
            throw new ServerErrorException("사용자 모델을 이용한 분석 중 에러가 발생하였습니다.\n사전 문제일 수 있습니다.");
        }

        return results;
    }


    public Map<String, String> getDiffsFromAnalyzedResults(String strToAnalyze, String modelNameSrc, String modelNameDest) {
        ModelValidator.CheckValidModelName(modelNameSrc);
        ModelValidator.CheckValidModelName(modelNameDest);

        String resultSrc;
        String resultDest;
        Map<String, String> result = new HashMap<>();

        if ("DEFAULT".equals(modelNameSrc)) {
            resultSrc = this.analyzeWithLightModel(strToAnalyze);
        } else {
            resultSrc = this.analyzeWithUserModel(strToAnalyze, modelNameSrc);
        }

        if ("DEFAULT".equals(modelNameDest)) {
            resultDest = this.analyzeWithLightModel(strToAnalyze);
        } else {
            resultDest = this.analyzeWithUserModel(strToAnalyze, modelNameDest);
        }

        StringBuffer resultSrcHtml = new StringBuffer();
        StringBuffer resultDestHtml = new StringBuffer();
        boolean isFirst = true;

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .build();
        try {
            List<DiffRow> rows = generator.generateDiffRows(Arrays.asList(resultSrc.split(" ")), Arrays.asList(resultDest.split(" ")));

            for (DiffRow row : rows) {
                if (!isFirst) {
                    resultSrcHtml.append(" ");
                    resultDestHtml.append(" ");
                } else {
                    isFirst = false;
                }

                resultSrcHtml.append(row.getOldLine());
                resultDestHtml.append(row.getNewLine());
            }
        } catch (DiffException e) {
            throw new ServerErrorException("분석 결과 비교 중 문제가 발생하였습니다.");
        }

        result.put("srcHtml", resultSrcHtml.toString());
        result.put("destHtml", resultDestHtml.toString());

        return result;
    }


    public Map<String, String> getDiffsFromAnalyzedMultipleResults(String strToAnalyzeWithNewLines, String modelNameSrc, String modelNameDest) {
        ModelValidator.CheckValidModelName(modelNameSrc);
        ModelValidator.CheckValidModelName(modelNameDest);

        ArrayList<String> resultSrc;
        ArrayList<String> resultDest;
        Map<String, String> result = new HashMap<>();

        if ("DEFAULT".equals(modelNameSrc)) {
            resultSrc = this.analyzeMultipleLinesWithLightModel(strToAnalyzeWithNewLines);
        } else {
            resultSrc = this.analyzeMultipleLinesWithUserModel(strToAnalyzeWithNewLines, modelNameSrc);
        }

        if ("DEFAULT".equals(modelNameDest)) {
            resultDest = this.analyzeMultipleLinesWithLightModel(strToAnalyzeWithNewLines);
        } else {
            resultDest = this.analyzeMultipleLinesWithUserModel(strToAnalyzeWithNewLines, modelNameDest);
        }

        if (resultSrc.size() != resultDest.size()) {
            throw new ServerErrorException("KOMORAN 오류가 발생하였습니다.");
        }

        StringBuffer resultSrcHtml = new StringBuffer();
        StringBuffer resultDestHtml = new StringBuffer();
        boolean isFirst = true;

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .build();
        try {
            for (int i = 0; i < resultSrc.size(); i++) {
                List<DiffRow> rows = generator.generateDiffRows(Arrays.asList(resultSrc.get(i).split(" ")), Arrays.asList(resultDest.get(i).split(" ")));

                for (DiffRow row : rows) {
                    if (!isFirst) {
                        resultSrcHtml.append(" ");
                        resultDestHtml.append(" ");
                    } else {
                        isFirst = false;
                    }

                    resultSrcHtml.append(row.getOldLine());
                    resultDestHtml.append(row.getNewLine());
                }
                resultSrcHtml.append("<br />");
                resultDestHtml.append("<br />");
            }
        } catch (DiffException e) {
            throw new ServerErrorException("분석 결과 비교 중 문제가 발생하였습니다.");
        }

        result.put("srcHtml", resultSrcHtml.toString());
        result.put("destHtml", resultDestHtml.toString());

        return result;
    }
}

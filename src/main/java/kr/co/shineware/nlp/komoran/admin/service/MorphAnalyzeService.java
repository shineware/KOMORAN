package kr.co.shineware.nlp.komoran.admin.service;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceNotFoundException;
import kr.co.shineware.nlp.komoran.admin.exception.ServerErrorException;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    private static Komoran komoran;

    private Komoran userKomoran;


    MorphAnalyzeService() {
        logger.debug("Init Komoran Model...");
        this.komoran = new Komoran(DEFAULT_MODEL.LIGHT);
        logger.debug("Init Komoran Model... DONE");
    }


    public String analyze(String strToAnalyze) {
        return this.komoran.analyze(strToAnalyze).getPlainText();
    }


    private boolean loadUserModel(String modelPathName) {
        String modelBasePathName = String.join(File.separator, MODELS_BASEDIR, modelPathName);
        File modelPath = new File(modelBasePathName);

        if (!modelPath.exists()) {
            throw new ResourceNotFoundException("존재하지 않는 모델명 ["+ modelPathName +"]");
        }

        this.userKomoran = new Komoran(String.join(File.separator, modelBasePathName, MODELS_MODELDIR));

        this.userKomoran.setUserDic(String.join(File.separator, modelBasePathName, filenameDicUser));
        this.userKomoran.setFWDic(String.join(File.separator, modelBasePathName, filenameFwdUser));

        return true;
    }


    private String analyzeWithUserModel(String modelPathName, String strToAnalyze) {
        if ("".equals(modelPathName)) {
            throw new ResourceNotFoundException("잘못된 모델명 ["+ modelPathName +"]");
        }

        String result;

        try {
            this.loadUserModel(modelPathName);
            result = this.userKomoran.analyze(strToAnalyze).getPlainText();
        }
        catch (NullPointerException e) {
            throw new ServerErrorException("사용자 모델을 이용한 분석 중 에러가 발생하였습니다.\\n사전 문제일 수 있습니다.");
        }

        return result;
    }


    public Map<String, String> getDiffsFromAnalyzedResults(String strToAnalyze, String modelNameSrc, String modelNameDest) {
        if ("".equals(modelNameSrc) || "".equals(modelNameDest)) {
            throw new ParameterInvalidException("잘못된 사전명");
        }

        String resultSrc;
        String resultDest;
        Map<String, String> result = new HashMap<>();

        if ("DEFAULT".equals(modelNameSrc)) {
            resultSrc = this.analyze(strToAnalyze);
        }
        else {
            resultSrc = this.analyzeWithUserModel(modelNameSrc, strToAnalyze);
        }

        if ("DEFAULT".equals(modelNameDest)) {
            resultDest = this.analyze(strToAnalyze);
        }
        else {
            resultDest = this.analyzeWithUserModel(modelNameDest, strToAnalyze);
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
                }
                else {
                    isFirst = false;
                }

                resultSrcHtml.append(row.getOldLine());
                resultDestHtml.append(row.getNewLine());
            }

        }
        catch (DiffException e) {
            throw new ServerErrorException("분석 결과 비교 중 문제가 발생하였습니다.");
        }

//        result.put("src", resultSrc);
//        result.put("dest", resultDest);
        result.put("srcHtml", resultSrcHtml.toString());
        result.put("destHtml", resultDestHtml.toString());

        return result;
    }

}

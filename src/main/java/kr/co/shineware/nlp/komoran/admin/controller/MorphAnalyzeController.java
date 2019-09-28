package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.service.MorphAnalyzeService;
import kr.co.shineware.nlp.komoran.admin.util.ResponseDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/analyze")
public class MorphAnalyzeController {
    private static final Logger logger = LoggerFactory.getLogger(DicUserController.class);


    @Autowired
    private MorphAnalyzeService morphAnalyzeService;


    @PostMapping(value = "/default")
    public ResponseDetail analyzeStr(@RequestParam("strToAnalyze") String strToAnalyze,
                                     @RequestParam("modelName") String modelNameToUse) {
        if (modelNameToUse == null || "".equals(modelNameToUse)) {
            throw new ParameterInvalidException("잘못된 모델명 [" + modelNameToUse + "]");
        }

        ResponseDetail responseDetail = new ResponseDetail();

        String analyzedResult = morphAnalyzeService.analyzeWithUserModel(strToAnalyze, modelNameToUse);

        responseDetail.setData(analyzedResult);

        return responseDetail;
    }


    @PostMapping(value = "/compare")
    public ResponseDetail analyzeStrWithNewModel(@RequestParam("strToAnalyze") String strToAnalyze,
                                                 @RequestParam("modelNameSrc") String modelNameSrc,
                                                 @RequestParam("modelNameDest") String modelNameDest) {
        if ("".equals(modelNameSrc) || "".equals(modelNameDest)) {
            throw new ParameterInvalidException("잘못된 모델명 [" + modelNameSrc + ", " + modelNameDest + "]");
        }

        ResponseDetail responseDetail = new ResponseDetail();

        Map<String, String> result = morphAnalyzeService.getDiffsFromAnalyzedResults(strToAnalyze, modelNameSrc, modelNameDest);
        responseDetail.setData(result);

        return responseDetail;
    }

}

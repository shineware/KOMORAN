package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.service.MorphAnalyzeService;
import kr.co.shineware.nlp.komoran.admin.util.ModelValidator;
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
        ModelValidator.CheckValidModelName(modelNameToUse);

        ResponseDetail responseDetail = new ResponseDetail();

        String analyzedResult = morphAnalyzeService.analyzeWithUserModel(strToAnalyze, modelNameToUse);

        responseDetail.setData(analyzedResult);

        return responseDetail;
    }


    @PostMapping(value = "/compare")
    public ResponseDetail analyzeStrWithNewModel(@RequestParam("strToAnalyze") String strToAnalyze,
                                                 @RequestParam("modelNameSrc") String modelNameSrc,
                                                 @RequestParam("modelNameDest") String modelNameDest) {
        ModelValidator.CheckValidModelName(modelNameSrc);
        ModelValidator.CheckValidModelName(modelNameDest);

        ResponseDetail responseDetail = new ResponseDetail();

        Map<String, String> result = morphAnalyzeService.getDiffsFromAnalyzedResults(strToAnalyze, modelNameSrc, modelNameDest);
        responseDetail.setData(result);

        return responseDetail;
    }

}

package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.service.FileUploadService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/analyze")
public class MorphAnalyzeController {
    private static final Logger logger = LoggerFactory.getLogger(DicUserController.class);


    @Autowired
    private MorphAnalyzeService morphAnalyzeService;

    @Autowired
    private FileUploadService fileUploadService;


    @PostMapping(value = "/default")
    public ResponseDetail analyzeStr(@RequestParam("strToAnalyze") String strToAnalyze,
                                     @RequestParam("modelName") String modelNameToUse) {
        ModelValidator.CheckValidModelName(modelNameToUse);

        ResponseDetail responseDetail = new ResponseDetail();

        String analyzedResult = morphAnalyzeService.analyzeWithUserModel(strToAnalyze, modelNameToUse);

        responseDetail.setData(analyzedResult);

        return responseDetail;
    }


    @PostMapping(value = "/multiple")
    public ResponseDetail analyzeMultipleStrs(@RequestParam("strToAnalyze") String strToAnalyze,
                                              @RequestParam("modelName") String modelNameToUse) {
        ModelValidator.CheckValidModelName(modelNameToUse);

        ResponseDetail responseDetail = new ResponseDetail();

        ArrayList<String> analyzedResults = morphAnalyzeService.analyzeMultipleLinesWithUserModel(strToAnalyze, modelNameToUse);
        String result = String.join("\n", analyzedResults);

        responseDetail.setData(result);

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


    @PostMapping(value = "/multicompare")
    public ResponseDetail analyzeMultipleStrsWithNewModel(@RequestParam("strToAnalyze") String strToAnalyze,
                                                          @RequestParam("modelNameSrc") String modelNameSrc,
                                                          @RequestParam("modelNameDest") String modelNameDest) {
        ModelValidator.CheckValidModelName(modelNameSrc);
        ModelValidator.CheckValidModelName(modelNameDest);

        ResponseDetail responseDetail = new ResponseDetail();

        Map<String, String> result = morphAnalyzeService.getDiffsFromAnalyzedMultipleResultsForHtml(strToAnalyze, modelNameSrc, modelNameDest);
        responseDetail.setData(result);

        return responseDetail;
    }

    @PostMapping(value = "/diff/file")
    public ResponseDetail uploadFile(@RequestParam("modelNameSrc") String modelNameSrc,
                                     @RequestParam("modelNameDest") String modelNameDest,
                                     @RequestParam("file") MultipartFile fileToAnalyze) {

        ModelValidator.CheckValidModelName(modelNameSrc);
        ModelValidator.CheckValidModelName(modelNameDest);

        List<String> diffResultList = morphAnalyzeService.getDiffsFromFiles(fileToAnalyze, modelNameSrc, modelNameDest);
        List<String> toShowDiffRows = diffResultList.subList(0, Math.min(50, diffResultList.size()));
        Map<String, String> result = morphAnalyzeService.generateDiffRows(toShowDiffRows);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(result);
        return responseDetail;
    }
}

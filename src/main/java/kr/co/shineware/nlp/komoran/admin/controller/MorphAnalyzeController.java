package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.service.MorphAnalyzeService;
import kr.co.shineware.nlp.komoran.admin.util.ResponseDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;


@RestController
@RequestMapping("/analyze")
public class MorphAnalyzeController {
    private static final Logger logger = LoggerFactory.getLogger(DicUserController.class);


    @Autowired
    private MorphAnalyzeService morphAnalyzeService;


    @PostMapping(value = "/")
    public ResponseDetail analyzeStr(@RequestParam("strToAnalyze") String strToAnalyze) {
        ResponseDetail responseDetail = new ResponseDetail();

        String analyzedResult = morphAnalyzeService.analyze(strToAnalyze);

        responseDetail.setData(analyzedResult);

        return responseDetail;
    }

}

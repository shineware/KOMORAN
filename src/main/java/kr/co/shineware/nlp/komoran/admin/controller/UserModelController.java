package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.service.UserModelService;
import kr.co.shineware.nlp.komoran.admin.util.ResponseDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/model")
public class UserModelController {
    private static final Logger logger = LoggerFactory.getLogger(UserModelController.class);

    @Autowired
    private UserModelService userModelService;


    @GetMapping(value = "/list")
    public ResponseDetail getModelList() {
        ResponseDetail responseDetail = new ResponseDetail();

        List<String> modelList = userModelService.getModelList();
        Collections.sort(modelList, Collections.reverseOrder());

        responseDetail.setData(modelList);

        return responseDetail;
    }


    @GetMapping(value = "/new")
    public ResponseDetail makeNewModel() {
        userModelService.buildNewModel();

        return getModelList();
    }
}

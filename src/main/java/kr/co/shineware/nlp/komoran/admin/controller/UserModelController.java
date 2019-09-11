package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ServerErrorException;
import kr.co.shineware.nlp.komoran.admin.service.UserModelService;
import kr.co.shineware.nlp.komoran.admin.util.ResponseDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

        responseDetail.setData(modelList);

        return responseDetail;
    }


    @GetMapping(value = "/new")
    public ResponseDetail makeNewUserModel() {
        userModelService.buildNewModel();

        return getModelList();
    }


    @PostMapping(value = "/delete")
    public ResponseDetail deleteUserModel(@RequestParam("modelName") String modelNameInRaw) {
        if (modelNameInRaw == null || "".equals(modelNameInRaw)) {
            throw new ParameterInvalidException("잘못된 모델명 [" + modelNameInRaw + "]");
        }

        ResponseDetail responseDetail = new ResponseDetail();

        boolean isDeletedClearly = userModelService.deleteUserModel(modelNameInRaw);

        if (!isDeletedClearly) {
            throw new ServerErrorException("모델 삭제 중 오류가 발생하였습니다.");
        }

        List<String> modelList = userModelService.getModelList();
        responseDetail.setData(modelList);

        return responseDetail;
    }


    @GetMapping(value = "/download/{modelName}")
    public ResponseEntity<?> downloadFile(@PathVariable("modelName") String modelNameInRaw) {
        if (modelNameInRaw == null || "".equals(modelNameInRaw)) {
            throw new ParameterInvalidException("잘못된 모델명 [" + modelNameInRaw + "]");
        }

        File zipFileToDeploy = userModelService.deployUserModel(modelNameInRaw);
        long zipFileSize = zipFileToDeploy.length();
        ByteArrayResource resource;

        try {
            resource = new ByteArrayResource(Files.readAllBytes(zipFileToDeploy.toPath()));
            zipFileToDeploy.delete();
        } catch (IOException e) {
            throw new ServerErrorException("");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipFileSize)
                .header("Content-Disposition", "attachment; filename=" + zipFileToDeploy.getName())
                .header("Content-Transfer-Encoding", "binary")
                .body(resource);
    }

}

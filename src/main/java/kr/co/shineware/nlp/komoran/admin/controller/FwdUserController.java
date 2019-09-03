package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.domain.FwdUser;
import kr.co.shineware.nlp.komoran.admin.exception.GlobalBaseException;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ServerErrorException;
import kr.co.shineware.nlp.komoran.admin.exception.UnknownException;
import kr.co.shineware.nlp.komoran.admin.service.FileUploadService;
import kr.co.shineware.nlp.komoran.admin.service.FwdUserService;
import kr.co.shineware.nlp.komoran.admin.util.ResponseDetail;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/fwd/user")
public class FwdUserController {
    private static final Logger logger = LoggerFactory.getLogger(FwdUserController.class);

    @Value("${files.name.fwduser}")
    private String fwdUserFilename;

    @Autowired
    private FwdUserService fwdUserService;

    @Autowired
    private FileUploadService fileUploadService;


    @GetMapping(value = "/check/{full}/{analyzed}")
    public ResponseDetail checkItem(@PathVariable("full") String fullInRaw,
                                    @PathVariable("analyzed") String analyzedInRaw) {
        if (fullInRaw == null || "".equals(fullInRaw)) {
            throw new ParameterInvalidException("기분석 어절: " + fullInRaw);
        } else if (analyzedInRaw == null || "".equals(analyzedInRaw)) {
            throw new ParameterInvalidException("기분석 결과: " + analyzedInRaw);
        }

        FwdUser fwdUserToFind = fwdUserService.checkGivenFullAndAnalyzedExist(fullInRaw, analyzedInRaw);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(fwdUserToFind);

        return responseDetail;
    }


    @PutMapping(value = "/{id}/full")
    public ResponseDetail updateFullById(@PathVariable("id") int id,
                                         @RequestParam("full") String fullInRaw) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (fullInRaw == null || "".equals(fullInRaw)) {
            throw new ParameterInvalidException("기분석 어절: " + fullInRaw);
        }

        FwdUser fwdUserToUpdate = fwdUserService.updateFullById(id, fullInRaw);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(fwdUserToUpdate);

        return responseDetail;
    }


    @PutMapping(value = "/{id}/analyzed")
    public ResponseDetail updateAnalyzedById(@PathVariable("id") int id,
                                             @RequestParam("analyzed") String analyzedInRaw) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (analyzedInRaw == null || "".equals(analyzedInRaw)) {
            throw new ParameterInvalidException("기분석 결과: " + analyzedInRaw);
        }

        FwdUser fwdUserToUpdate = fwdUserService.updateAnalyzedById(id, analyzedInRaw);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(fwdUserToUpdate);

        return responseDetail;
    }


    @DeleteMapping(value = "/{id}")
    public ResponseDetail deleteItemById(@PathVariable("id") int id) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        }

        FwdUser fwdUserToDelete = fwdUserService.deleteItemById(id);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(fwdUserToDelete);

        return responseDetail;
    }


    @PostMapping(value = "/item")
    public ResponseDetail addItem(@RequestParam("full") String fullInRaw,
                                  @RequestParam("analyzed") String analyzedInRaw) {
        if (fullInRaw == null || "".equals(fullInRaw)) {
            throw new ParameterInvalidException("기분석 어절: " + fullInRaw);
        } else if (analyzedInRaw == null || "".equals(analyzedInRaw)) {
            throw new ParameterInvalidException("기분석 결과: " + analyzedInRaw);
        }

        FwdUser fwdUserToAdd = fwdUserService.addItem(fullInRaw, analyzedInRaw);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(fwdUserToAdd);

        return responseDetail;
    }


    @GetMapping(value = "/purge")
    public void purgeDB() {
        fwdUserService.purgeAllData();
    }


    @PostMapping(value = "/upload")
    public ResponseDetail uploadFile(@RequestParam("file") MultipartFile fileToUpload) {
        ResponseDetail responseDetail = new ResponseDetail();

        try {
            Path savedFilePath = fileUploadService.saveFwdUser(fileToUpload);

            if (savedFilePath == null) {
                throw new ServerErrorException("업로드 파일을 찾지 못함");
            }

            fwdUserService.importFromFile(savedFilePath);
        } catch (IOException e) {
            throw new ServerErrorException(e.getMessage(), e.getCause());
        } catch (SecurityException e) {
            throw new ServerErrorException(e.getMessage(), e.getCause());
        } catch (GlobalBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new UnknownException(e.getCause());
        }

        return responseDetail;
    }


    @GetMapping(value = "/download")
    public ResponseEntity<?> downloadFile() {
        String fwdUserBody = fwdUserService.exportToString();
        ByteArrayResource resource = new ByteArrayResource(fwdUserBody.getBytes());

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=" + fwdUserFilename)
                .body(resource);
    }


    @GetMapping(value = "/list")
    public JSONObject getGrammarInList(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                       @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                       @RequestParam Map<String, String> allParameters) {
        if (page < 1) {
            throw new ParameterInvalidException("page: " + page);
        } else if (size < 1) {
            throw new ParameterInvalidException("size: " + size);
        }

        return fwdUserService.getFwdUserList(page, size, allParameters);
    }
}

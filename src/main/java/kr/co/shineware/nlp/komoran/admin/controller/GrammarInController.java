package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.domain.GrammarIn;
import kr.co.shineware.nlp.komoran.admin.domain.GrammarType;
import kr.co.shineware.nlp.komoran.admin.exception.GlobalBaseException;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ServerErrorException;
import kr.co.shineware.nlp.komoran.admin.exception.UnknownException;
import kr.co.shineware.nlp.komoran.admin.service.FileUploadService;
import kr.co.shineware.nlp.komoran.admin.service.GrammarInService;
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
@RequestMapping("/grammar/in")
public class GrammarInController {
    private static final Logger logger = LoggerFactory.getLogger(GrammarInController.class);

    @Value("${files.name.grammarin}")
    private String grammarInFilename;

    @Autowired
    private GrammarInService grammarInService;

    @Autowired
    private FileUploadService fileUploadService;


    @GetMapping(value = "/check/{start}/{next}")
    public ResponseDetail checkItem(@PathVariable("start") String startInRaw,
                                    @PathVariable("next") String nextInRaw) {
        if (!GrammarType.contains(startInRaw)) {
            throw new ParameterInvalidException("시작 품사: " + startInRaw);
        } else if (!GrammarType.contains(nextInRaw)) {
            throw new ParameterInvalidException("다음 품사: " + nextInRaw);
        }

        GrammarType start = GrammarType.valueOf(startInRaw);
        GrammarType next = GrammarType.valueOf(nextInRaw);
        GrammarIn grammarInToFind = grammarInService.checkGivenTokenAndPosTypeExist(start, next);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(grammarInToFind);

        return responseDetail;
    }


    @PutMapping(value = "/{id}/tf")
    public ResponseDetail updateTfById(@PathVariable("id") int id,
                                       @RequestParam("tf") int tf) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (tf < 0) {
            throw new ParameterInvalidException("빈도: " + tf);
        }

        GrammarIn grammarInToUpdate = grammarInService.updateTfById(id, tf);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(grammarInToUpdate);

        return responseDetail;
    }


    @PutMapping(value = "/{id}/start")
    public ResponseDetail updateStartById(@PathVariable("id") int id,
                                          @RequestParam("next") String startInRaw) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (!GrammarType.contains(startInRaw)) {
            throw new ParameterInvalidException("시작 품사: " + startInRaw);
        }

        GrammarType start = GrammarType.valueOf(startInRaw);
        GrammarIn grammarInToUpdate = grammarInService.updateStartById(id, start);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(grammarInToUpdate);

        return responseDetail;
    }


    @PutMapping(value = "/{id}/next")
    public ResponseDetail updateNextById(@PathVariable("id") int id,
                                         @RequestParam("next") String nextInRaw) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (!GrammarType.contains(nextInRaw)) {
            throw new ParameterInvalidException("다음 품사: " + nextInRaw);
        }

        GrammarType next = GrammarType.valueOf(nextInRaw);
        GrammarIn grammarInToUpdate = grammarInService.updateNextById(id, next);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(grammarInToUpdate);

        return responseDetail;
    }


    @DeleteMapping(value = "/{id}")
    public ResponseDetail deleteItemById(@PathVariable("id") int id) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        }

        GrammarIn grammarInToDelete = grammarInService.deleteItemById(id);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(grammarInToDelete);

        return responseDetail;
    }


    @PostMapping(value = "/item")
    public ResponseDetail addItem(@PathVariable("start") String startInRaw,
                                  @PathVariable("next") String nextInRaw,
                                  @RequestParam("tf") int tf) {
        if (!GrammarType.contains(startInRaw)) {
            throw new ParameterInvalidException("시작 품사: " + startInRaw);
        } else if (!GrammarType.contains(nextInRaw)) {
            throw new ParameterInvalidException("다음 품사: " + nextInRaw);
        } else if (tf < 0) {
            throw new ParameterInvalidException("빈도: " + tf);
        }

        GrammarType start = GrammarType.valueOf(startInRaw);
        GrammarType next = GrammarType.valueOf(nextInRaw);
        GrammarIn grammarInToAdd = grammarInService.addItem(start, next, tf);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(grammarInToAdd);

        return responseDetail;
    }


    @GetMapping(value = "/purge")
    public void purgeDB() {
        grammarInService.purgeAllData();
    }


    @PostMapping(value = "/upload")
    public ResponseDetail uploadFile(@RequestParam("file") MultipartFile fileToUpload) {
        ResponseDetail responseDetail = new ResponseDetail();

        try {
            Path savedFilePath = fileUploadService.saveGrammarIn(fileToUpload);

            if (savedFilePath == null) {
                throw new ServerErrorException("업로드 파일을 찾지 못함");
            }

            grammarInService.importFromFile(savedFilePath);
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
        String grammarInBody = grammarInService.exportToString();
        ByteArrayResource resource = new ByteArrayResource(grammarInBody.getBytes());

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=" + grammarInFilename)
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

        return grammarInService.getGrammarInList(page, size, allParameters);
    }
}

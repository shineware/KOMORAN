package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.GlobalBaseException;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ServerErrorException;
import kr.co.shineware.nlp.komoran.admin.exception.UnknownException;
import kr.co.shineware.nlp.komoran.admin.service.DicWordService;
import kr.co.shineware.nlp.komoran.admin.service.FileUploadService;
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
@RequestMapping("/dic/word")
public class DicWordController {
    private static final Logger logger = LoggerFactory.getLogger(DicWordController.class);

    @Value("${files.name.dicword}")
    private String dicWordFilename;

    @Autowired
    private DicWordService dicWordService;

    @Autowired
    private FileUploadService fileUploadService;


    @GetMapping(value = "/check/{token}/{pos}")
    public ResponseDetail checkItem(@PathVariable("token") String token,
                                    @PathVariable("pos") String posInRaw) {
        if (token == null || token.equals("")) {
            throw new ParameterInvalidException("token: " + token);
        } else if (!PosType.contains(posInRaw)) {
            throw new ParameterInvalidException("품사: " + posInRaw);
        }

        PosType pos = PosType.valueOf(posInRaw);
        DicWord dicWordToFind = dicWordService.checkGivenTokenAndPosTypeExist(token, pos);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(dicWordToFind);

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

        DicWord dicWordToUpdate = dicWordService.updateTfById(id, tf);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(dicWordToUpdate);

        return responseDetail;
    }


    @PutMapping(value = "/{id}/pos")
    public ResponseDetail updatePosById(@PathVariable("id") int id,
                                        @RequestParam("pos") String posInRaw) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (!PosType.contains(posInRaw)) {
            throw new ParameterInvalidException("품사: " + posInRaw);
        }

        PosType pos = PosType.valueOf(posInRaw);
        DicWord dicWordToUpdate = dicWordService.updatePosById(id, pos);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(dicWordToUpdate);

        return responseDetail;
    }


    @DeleteMapping(value = "/{id}")
    public ResponseDetail deleteItemById(@PathVariable("id") int id) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        }

        DicWord dicWordToDelete = dicWordService.deleteItemById(id);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(dicWordToDelete);

        return responseDetail;
    }


    @PostMapping(value = "/item")
    public ResponseDetail addItem(@RequestParam("token") String token,
                                  @RequestParam("pos") String posInRaw,
                                  @RequestParam("tf") int tf) {
        if (token == null || token.equals("")) {
            throw new ParameterInvalidException("token: " + token);
        } else if (!PosType.contains(posInRaw)) {
            throw new ParameterInvalidException("품사: " + posInRaw);
        } else if (tf < 0) {
            throw new ParameterInvalidException("빈도: " + tf);
        }

        PosType pos = PosType.valueOf(posInRaw);
        DicWord dicWordToAdd = dicWordService.addItem(token, pos, tf);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(dicWordToAdd);

        return responseDetail;
    }


    @GetMapping(value = "/purge")
    public void purgeDB() {
        dicWordService.purgeAllData();
    }


    @PostMapping(value = "/upload")
    public ResponseDetail uploadFile(@RequestParam("file") MultipartFile fileToUpload) {
        ResponseDetail responseDetail = new ResponseDetail();

        try {
            Path savedFilePath = fileUploadService.saveDicWord(fileToUpload);

            if (savedFilePath == null) {
                throw new ServerErrorException("업로드 파일을 찾지 못함");
            }

            dicWordService.importFromFile(savedFilePath);
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
        String dicWordBody = dicWordService.exportToString();
        ByteArrayResource resource = new ByteArrayResource(dicWordBody.getBytes());

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=" + dicWordFilename)
                .body(resource);
    }


    @GetMapping(value = "/list")
    public JSONObject getDicWordList(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                     @RequestParam Map<String, String> allParameters) {
        if (page < 1) {
            throw new ParameterInvalidException("page: "+ page);
        } else if (size < 1) {
            throw new ParameterInvalidException("size: "+ size);
        }

        return dicWordService.getDicWordList(page, size, allParameters);
    }
}

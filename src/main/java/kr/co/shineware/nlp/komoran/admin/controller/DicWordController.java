package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.service.FileUploadService;
import kr.co.shineware.nlp.komoran.admin.service.DicWordService;
import kr.co.shineware.nlp.komoran.admin.util.MessageBuilder;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
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
    public JSONObject checkItem(@PathVariable("token") String token,
                                @PathVariable("pos") PosType pos) {
        return dicWordService.checkGivenTokenAndPosTypeExist(token, pos);
    }


    @PutMapping(value = "/{id}/tf")
    public JSONObject updateTfById(@PathVariable("id") int id,
                                   @RequestParam("tf") int tf) {
        return dicWordService.updateTfById(id, tf);
    }


    @PutMapping(value = "/{id}/pos")
    public JSONObject updatePosById(@PathVariable("id") int id,
                                    @RequestParam("pos") PosType pos) {
        return dicWordService.updatePosById(id, pos);
    }


    @DeleteMapping(value = "/{id}")
    public JSONObject deleteItemById(@PathVariable("id") int id) {
        return dicWordService.deleteItemById(id);
    }


    @PostMapping(value = "/item")
    public JSONObject addItem(@RequestParam("token") String token,
                              @RequestParam("pos") PosType pos,
                              @RequestParam("tf") int tf) {
        return dicWordService.addItem(token, pos, tf);
    }


    @GetMapping(value = "/purge")
    public void purgeDB() {
        dicWordService.purgeAllData();
    }


    @PostMapping(value = "/upload")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile fileToUpload) {
        JSONObject resultMessage = new JSONObject();

        try {
            Path savedFilePath = fileUploadService.saveDicWord(fileToUpload);

            if (savedFilePath == null) {
                MessageBuilder.buildErrorMessage(resultMessage, "업로드된 파일이 없습니다.");
                return new ResponseEntity(resultMessage, HttpStatus.OK);
            }

            dicWordService.importFromFile(savedFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            MessageBuilder.buildErrorMessage(resultMessage, "업로드 중 오류가 발생하였습니다.");
            return new ResponseEntity<>(resultMessage, HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            MessageBuilder.buildErrorMessage(resultMessage, "서버에 파일을 저장할 수 없습니다.");
            return new ResponseEntity<>(resultMessage, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            MessageBuilder.buildErrorMessage(resultMessage, "업로드 중 알 수 없는 오류가 발생하였습니다. (" + e.getClass().getSimpleName() + ")");
            return new ResponseEntity<>(resultMessage, HttpStatus.BAD_REQUEST);
        }

        MessageBuilder.buildSuccessMessage(resultMessage);
        return new ResponseEntity(resultMessage, HttpStatus.OK);
    }


    @GetMapping(value = "/download")
    public ResponseEntity<Resource> downloadFile() {
        String dicWordBody = dicWordService.exportToString();
        ByteArrayResource resource = new ByteArrayResource(dicWordBody.getBytes());

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename="+ dicWordFilename)
                .body(resource);
    }


    @GetMapping(value = "/list")
    public JSONObject getDicWordList(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                     @RequestParam Map<String, String> allParameters) {

        return dicWordService.getDicWordList(page, size, allParameters);
    }
}

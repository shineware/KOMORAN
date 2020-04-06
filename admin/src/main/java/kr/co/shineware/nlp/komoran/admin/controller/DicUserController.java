package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.domain.DicUser;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.GlobalBaseException;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ServerErrorException;
import kr.co.shineware.nlp.komoran.admin.exception.UnknownException;
import kr.co.shineware.nlp.komoran.admin.service.DicUserService;
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
@RequestMapping("/dic/user")
public class DicUserController {
    private static final Logger logger = LoggerFactory.getLogger(DicUserController.class);

    @Value("${files.name.dicuser}")
    private String dicUserFilename;

    @Autowired
    private DicUserService dicUserService;

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
        DicUser dicUserToFind = dicUserService.checkGivenTokenAndPosTypeExist(token, pos);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(dicUserToFind);

        return responseDetail;
    }


    @PutMapping(value = "/{id}/token")
    public ResponseDetail updateTokenById(@PathVariable("id") int id,
                                          @RequestParam("token") String tokenInRaw) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (tokenInRaw == null || "".equals(tokenInRaw)) {
            throw new ParameterInvalidException("단어: " + tokenInRaw);
        }

        DicUser dicUserToUpdate = dicUserService.updateTokenById(id, tokenInRaw);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(dicUserToUpdate);

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
        DicUser dicUserToUpdate = dicUserService.updatePosById(id, pos);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(dicUserToUpdate);

        return responseDetail;
    }


    @DeleteMapping(value = "/{id}")
    public ResponseDetail deleteItemById(@PathVariable("id") int id) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        }

        DicUser dicUserToDelete = dicUserService.deleteItemById(id);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(dicUserToDelete);

        return responseDetail;
    }


    @PostMapping(value = "/item")
    public ResponseDetail addItem(@RequestParam("token") String token,
                                  @RequestParam("pos") String posInRaw) {
        if (token == null || token.equals("")) {
            throw new ParameterInvalidException("token: " + token);
        } else if (!PosType.contains(posInRaw)) {
            throw new ParameterInvalidException("품사: " + posInRaw);
        }

        PosType pos = PosType.valueOf(posInRaw);
        DicUser dicUserToAdd = dicUserService.addItem(token, pos);
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setData(dicUserToAdd);

        return responseDetail;
    }


    @GetMapping(value = "/purge")
    public void purgeDB() {
        dicUserService.purgeAllData();
    }


    @PostMapping(value = "/upload")
    public ResponseDetail uploadFile(@RequestParam("file") MultipartFile fileToUpload) {
        ResponseDetail responseDetail = new ResponseDetail();

        try {
            Path savedFilePath = fileUploadService.saveDicUser(fileToUpload);

            if (savedFilePath == null) {
                throw new ServerErrorException("업로드 파일을 찾지 못함");
            }

            dicUserService.importFromFile(savedFilePath);
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
        String dicUserBody = dicUserService.exportToString();
        ByteArrayResource resource = new ByteArrayResource(dicUserBody.getBytes());

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=" + dicUserFilename)
                .body(resource);
    }


    @GetMapping(value = "/list")
    public JSONObject getDicUserList(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                     @RequestParam Map<String, String> allParameters) {
        if (page < 1) {
            throw new ParameterInvalidException("page: " + page);
        } else if (size < 1) {
            throw new ParameterInvalidException("size: " + size);
        }

        return dicUserService.getDicUserList(page, size, allParameters);
    }
}

package kr.co.shineware.nlp.komoran.admin.service;

import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class FileUploadService {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${files.upload.basedir}")
    private String UPLOAD_BASEDIR;

    @Value("${files.name.dicword}")
    private String filenameDicWord;

    @Value("${files.name.grammarin}")
    private String filenameGrammarIn;

    @Value("${files.name.dicuser}")
    private String filenameDicUser;

    @Value("${files.name.fwduser}")
    private String filenameFwdUser;


    public Path saveDicWord(MultipartFile uploadedFile) throws IOException {
        return saveUploadedFile(uploadedFile, filenameDicWord);
    }


    public Path saveGrammarIn(MultipartFile uploadedFile) throws IOException {
        return saveUploadedFile(uploadedFile, filenameGrammarIn);
    }


    public Path saveDicUser(MultipartFile uploadedFile) throws IOException {
        return saveUploadedFile(uploadedFile, filenameDicUser);
    }


    public Path saveFwdUser(MultipartFile uploadedFile) throws IOException {
        return saveUploadedFile(uploadedFile, filenameFwdUser);
    }


    private Path saveUploadedFile(MultipartFile uploadedFile, String filenameToSave) throws IOException, SecurityException {
        if (uploadedFile.isEmpty()) {
            throw new ParameterInvalidException("파일이 존재하지 않음");
        }

        File pathToSave = new File(UPLOAD_BASEDIR);

        if (!pathToSave.exists()) {
            pathToSave.mkdirs();
        }

        File fileToSave = new File(UPLOAD_BASEDIR + filenameToSave).getAbsoluteFile();

        uploadedFile.transferTo(fileToSave);

        return fileToSave.toPath();
    }
}

package kr.co.shineware.nlp.komoran.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
            return null;
        }

        if (!(new File(UPLOAD_BASEDIR)).exists()) {
            (new File(UPLOAD_BASEDIR)).mkdirs();
        }

        byte[] fileData = uploadedFile.getBytes();
        Path savedPath = Paths.get((new ClassPathResource(UPLOAD_BASEDIR + filenameToSave)).getURI());

        logger.debug(savedPath.toString());
        Files.write(savedPath, fileData);

        logger.debug("File Saved @ "+ savedPath);

        return savedPath;
    }
}

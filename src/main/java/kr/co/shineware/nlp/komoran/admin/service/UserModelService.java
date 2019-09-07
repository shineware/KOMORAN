package kr.co.shineware.nlp.komoran.admin.service;

import kr.co.shineware.nlp.komoran.admin.exception.ServerErrorException;
import kr.co.shineware.nlp.komoran.modeler.builder.ModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserModelService {
    private static final Logger logger = LoggerFactory.getLogger(UserModelService.class);

    @Value("${files.default.basedir}")
    private String DEFAULT_BASEDIR;

    @Value("${models.default.basedir}")
    private String MODELS_BASEDIR;

    @Value("${models.default.corpusdir}")
    private String MODELS_CORPUSDIR;

    @Value("${models.model.dir}")
    private String MODELS_MODELDIR;

    @Value("${files.name.dicword}")
    private String filenameDicWord;

    @Value("${files.name.dicuser}")
    private String filenameDicUser;

    @Value("${files.name.fwduser}")
    private String filenameFwdUser;

    @Value("${files.name.grammarin}")
    private String filenameGrammarIn;


    @Autowired
    private DicUserService dicUserService;

    @Autowired
    private DicWordService dicWordService;

    @Autowired
    private FwdUserService fwdUserService;

    @Autowired
    private GrammarInService grammarInService;


    private String getModelBasePath() {
        SimpleDateFormat dirNameFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String modelPath = MODELS_BASEDIR + dirNameFormat.format(System.currentTimeMillis());

        return modelPath;
    }


    private String prepareModelBasePath() throws IOException {
        String modelPathname = getModelBasePath();
        File modelPathToSave = new File(modelPathname);

        while (modelPathToSave.exists()) {
            try {
                TimeUnit.MICROSECONDS.sleep(5);
            } catch (Exception e) {

            }

            modelPathname = getModelBasePath();
            modelPathToSave = new File(modelPathname);
        }

        // Create model directory to save user model
        modelPathToSave.mkdirs();

        // Copy corpus dir to user model dir
        String corpusDefaultPathname = String.join(File.separator, DEFAULT_BASEDIR, MODELS_CORPUSDIR);
        String corpusCopyToDestPathname = String.join(File.separator, modelPathname, MODELS_CORPUSDIR);
        File corpusDefaultScrPath = (new ClassPathResource(corpusDefaultPathname)).getFile();
        File corpusCopyToDestPath = new File(corpusCopyToDestPathname);

        // Create corpus dir
        corpusCopyToDestPath.mkdir();

        // Copy included files
        File defaultFiles[] = corpusDefaultScrPath.listFiles();
        for (File aDefaultFile : defaultFiles) {
            File tmpFileForCopy = new File(String.join(File.separator, corpusCopyToDestPathname, aDefaultFile.getName()));
            Files.copy(aDefaultFile.toPath(), tmpFileForCopy.toPath());
        }

        // return model directory
        return modelPathname;
    }


    private String prepareFilesForUserModel() throws IOException {
        String modelPath = prepareModelBasePath();

        // Save files for user model to Corpus DIR
        String dicWordFilename = String.join(File.separator, modelPath, MODELS_CORPUSDIR, filenameDicWord);
        String grammarInFilename = String.join(File.separator, modelPath, MODELS_CORPUSDIR, filenameGrammarIn);

        PrintWriter dicWordFile = new PrintWriter(dicWordFilename);
        PrintWriter grammarInFile = new PrintWriter(grammarInFilename);

        String dicWordBody = dicWordService.exportToString();
        String grammarInBody = grammarInService.exportToString();

        dicWordFile.print(dicWordBody);
        grammarInFile.print(grammarInBody);

        dicWordFile.close();
        grammarInFile.close();


        // Save additional files
        String dicUserFilename = String.join(File.separator, modelPath, filenameDicUser);
        String fwdUserFilename = String.join(File.separator, modelPath, filenameFwdUser);

        PrintWriter dicUserFile = new PrintWriter(dicUserFilename);
        PrintWriter fwdUserFile = new PrintWriter(fwdUserFilename);

        String dicUserBody = dicUserService.exportToString();
        String fwdUserBody = fwdUserService.exportToString();

        dicUserFile.print(dicUserBody);
        fwdUserFile.print(fwdUserBody);

        dicUserFile.close();
        fwdUserFile.close();


        return modelPath;
    }


    public boolean buildNewModel() {
        String modelPathName;
        String corpusPathname;
        String modelSavePathName;
        ModelBuilder modelBuilder = new ModelBuilder();

        try {
            modelPathName = prepareFilesForUserModel();
            corpusPathname = String.join(File.separator, modelPathName, MODELS_CORPUSDIR);
            modelSavePathName = String.join(File.separator, modelPathName, MODELS_MODELDIR);

            modelBuilder.buildPath(corpusPathname);
            modelBuilder.save(modelSavePathName);
        } catch (IOException e) {
            throw new ServerErrorException("사용자 모델 생성을 위한 사전 파일 준비 중 실패하였습니다.");
        } catch (NullPointerException e) {
            throw new ServerErrorException("사용자 모델 생성에 실패했습니다. 사전들의 내용이 유효한지 확인해주세요.");
        } catch (Exception e) {
            throw new ServerErrorException("사용자 모델 생성에 실패했습니다. 사전들을 확인해주세요.");
        }

        return true;
    }


    public List<String> getModelList() {
        File modelBasePath = new File(MODELS_BASEDIR);
        List<String> modelList = new ArrayList<>();

        // add default model
        modelList.add("DEFAULT");

        if (!modelBasePath.exists()) {
            return modelList;
        }

        String modelDirs[] = modelBasePath.list();
        modelList.addAll(Arrays.asList(modelDirs));

        return modelList;
    }
}

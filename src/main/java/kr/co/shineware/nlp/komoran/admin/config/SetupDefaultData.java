package kr.co.shineware.nlp.komoran.admin.config;

import kr.co.shineware.nlp.komoran.admin.repository.DicWordRepository;
import kr.co.shineware.nlp.komoran.admin.repository.GrammarInRepository;
import kr.co.shineware.nlp.komoran.admin.service.DicWordService;
import kr.co.shineware.nlp.komoran.admin.service.GrammarInService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class SetupDefaultData implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(SetupDefaultData.class);

    @Value("${files.default.basedir}")
    private String DEFAULT_BASEDIR;

    @Value("${files.name.dicword}")
    private String filenameDicWord;

    @Value("${files.name.grammarin}")
    private String filenameGrammarIn;

    @Value("${files.name.dicuser}")
    private String filenameDicUser;

    @Value("${files.name.fwduser}")
    private String filenameFwdUser;


    @Autowired
    private DicWordRepository dicWordRepository;

    @Autowired
    private DicWordService dicWordService;

    @Autowired
    private GrammarInRepository grammarInRepository;

    @Autowired
    private GrammarInService grammarInService;


    private Path getDefaultFilePath(String type) {
        String filePathStr;
        Path pathForDefaultFile = null;

        switch (type) {
            case "dicword":
                filePathStr = DEFAULT_BASEDIR + "/" + filenameDicWord;
                break;
            case "grammarin":
                filePathStr = DEFAULT_BASEDIR + "/" + filenameGrammarIn;
                break;
            case "dicuser":
                filePathStr = DEFAULT_BASEDIR + "/" + filenameDicUser;
                break;
            case "fwduser":
                filePathStr = DEFAULT_BASEDIR + "/" + filenameFwdUser;
                break;
            default:
                return null;
        }

        try {
            pathForDefaultFile = Paths.get((new ClassPathResource(filePathStr)).getURI());
        } catch (Exception e) {
            // DO NOTHING, JUST FAIL TO LOAD DEFAULT
        }

        return pathForDefaultFile;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.debug("DicWord : " + dicWordRepository.count());
        logger.debug("GrammarIn : " + grammarInRepository.count());

        // Initialize DicWord
        if (dicWordRepository.count() <= 0 && getDefaultFilePath("dicword") != null) {
            logger.debug("Importing DicWord from file named "+ filenameDicWord);
            dicWordService.importFromFile(getDefaultFilePath("dicword"));
        }

        // Initialize GrammarIn
        if (grammarInRepository.count() <= 0 && getDefaultFilePath("grammarin") != null) {
            logger.debug("Importing GrammarIn from file named "+ filenameGrammarIn);
            grammarInService.importFromFile(getDefaultFilePath("grammarin"));
        }
    }
}

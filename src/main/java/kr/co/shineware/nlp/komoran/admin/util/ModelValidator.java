package kr.co.shineware.nlp.komoran.admin.util;

import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.service.UserModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

@Component
public class ModelValidator {
    private static final Logger logger = LoggerFactory.getLogger(UserModelService.class);

    private static String MODELS_BASEDIR;

    @Value("${models.default.basedir}")
    private void setModelsBaseDir(String modelsDefaultBaseDir) {
        MODELS_BASEDIR = modelsDefaultBaseDir;
    }

    private static String MODELS_MODELDIR;

    @Value("${models.model.dir}")
    private void setModelsModeldir(String modelsModelDir) {
        MODELS_MODELDIR = modelsModelDir;
    }

    private static String filenameDicUser;

    @Value("${files.name.dicuser}")
    private void setFilenameDicUser(String filesNameDicUser) {
        filenameDicUser = filesNameDicUser;
    }

    private static String filenameFwdUser;

    @Value("${files.name.fwduser}")
    private void setFilenameFwdUser(String filesNameFwdUser) {
        filenameFwdUser = filesNameFwdUser;
    }


    private static List<String> validFilenamesInMocel = Arrays.asList("irregular.model", "observation.model", "pos.table", "transition.model");

    private static SimpleDateFormat modelNameFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");


    public static String GenerateNewModelName() {
        return modelNameFormat.format(System.currentTimeMillis());
    }


    public static boolean IsValidModelName(String modelName) {
        if (modelName == null || "".equals(modelName.trim())) {
            return false;
        }

        if ("DEFAULT".equals(modelName)) {
            return false;
        }

        try {
            modelNameFormat.parse(modelName);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }


    public static void CheckValidModelName(String modelName) {
        if (!IsValidModelName(modelName)) {
            throw new ParameterInvalidException("잘못된 모델명 [" + modelName + "]");
        }
    }


    public static boolean IsValidUserModel(String modelName) {
        if (!IsValidModelName(modelName)) {
            return false;
        }

        // #1. Check Model Path exists in File System (eg. models/20190912215734342)
        String modelBasePath = String.join(File.separator, MODELS_BASEDIR, modelName);

        if (!(new File(modelBasePath)).exists()) {
            return false;
        }


        // #2. Check Model Path contains MODELDIR (eg. models/20190912215734342/model
        String savedModelPath = String.join(File.separator, modelBasePath, MODELS_MODELDIR);

        if (!(new File(savedModelPath)).exists()) {
            return false;
        }

        // #3. Check Model's MODELDIR contains valid model files (eg. irregular.model, observation.model, ...)
        for (String validFilename : validFilenamesInMocel) {
            String tmpFilepathToCheck = String.join(File.separator, modelBasePath, MODELS_MODELDIR, validFilename);
            if (!(new File(tmpFilepathToCheck)).exists()) {
                return false;
            }
        }

        // #4. Check Model has valid User Dict files (eg. dic.user)
        String userDicPath = String.join(File.separator, modelBasePath, filenameDicUser);

        if (!(new File(userDicPath)).exists()) {
            return false;
        }

        // #5. Check Model has valid User FWD files (eg. fwd.user)
        String userFWDPath = String.join(File.separator, modelBasePath, filenameFwdUser);

        return (new File(userFWDPath)).exists();
    }


    public static void CheckValidUserModel(String modelName) {
        if (!IsValidUserModel(modelName)) {
            throw new ParameterInvalidException("잘못된 모델명 [" + modelName + "]");
        }
    }
}

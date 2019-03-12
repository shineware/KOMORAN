package kr.co.shineware.nlp.komoran.admin.util;

import org.json.simple.JSONObject;

// TODO: Error 발생 시 Custom Exception 및 ResponseEntity 로 변경 필요
public class MessageBuilder {

    @SuppressWarnings("unchecked")
    private static void buildResultMessage(JSONObject objToBuild, boolean isSuccess) {
        if (isSuccess) {
            objToBuild.put("success", true);
            objToBuild.put("error", false);
        } else {
            objToBuild.put("success", false);
            objToBuild.put("error", true);
        }
    }


    public static void buildSuccessMessage(JSONObject objToBuild) {
        buildResultMessage(objToBuild, true);
    }


    @SuppressWarnings("unchecked")
    public static void buildErrorMessage(JSONObject objToBuild, String errMessage) {
        buildResultMessage(objToBuild, false);
        objToBuild.put("message", errMessage);
    }

}

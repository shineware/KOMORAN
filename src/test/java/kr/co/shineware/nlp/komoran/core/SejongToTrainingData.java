package kr.co.shineware.nlp.komoran.core;

import kr.co.shineware.nlp.komoran.constant.SEJONGTAGS;
import kr.co.shineware.nlp.komoran.util.HangulJamoUtil;
import kr.co.shineware.util.common.file.FileUtil;
import kr.co.shineware.util.common.string.StringUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Ignore
public class SejongToTrainingData {

    private Set<String> sejongTagSet;

    @Before
    @Test
    public void setupSejongTags() {
        sejongTagSet = new HashSet<>();
        SEJONGTAGS[] tags = SEJONGTAGS.values();
        for (SEJONGTAGS tag : tags) {
            sejongTagSet.add(tag.name());
        }
        System.out.println(sejongTagSet);
    }

    @Test
    @Ignore
    public void checkConvertCorpus() {

        List<String> filenames = FileUtil.getFileNames("D:\\data", ".refine.txt");
        for (String filename : filenames) {
            System.out.println(filename);
            List<String> lines = FileUtil.load2List(filename);
            for (String line : lines) {
                if (line.contains("미시간대학교") || line.contains("쿠팡")) {
                    System.out.println(line);
                }
            }
        }
    }


    private boolean hasSpecialCharacter(String line) {
        return line.contains("<") || line.contains(">") || line.contains("~") || line.contains("(") || line.contains(")");
    }

    private boolean hasSingleJungSung(String answer) {
        String[] tokens = answer.split(" ");
        for (String token : tokens) {
            String morph = token.split("/")[0];
            String pos = token.split("/")[1];

            String removeJungSungOnly = morph.replaceAll("[ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ]", "");
            if (removeJungSungOnly.length() != morph.length()) {
                return true;
            }

        }
        return false;
    }

    @Test
    public void loadKEBilingualCorpusAndConvertUTF8() throws IOException {
//        봤습니다만,	보/VX /ㅏㅆ 습니다/EF 만/JX ,/SP
        BufferedWriter bw = new BufferedWriter
                (new OutputStreamWriter(new FileOutputStream("D:\\shineware\\data\\komoran_training\\한영병렬_형태분석_말뭉치.refine.txt"), StandardCharsets.UTF_8));
        List<String> filenames = FileUtil.getFileNames("D:\\shineware\\data\\komoran_training\\한영병렬_형태분석_말뭉치", "KK.txt");
        filenames.addAll(FileUtil.getFileNames("D:\\shineware\\data\\komoran_training\\한영병렬_형태분석_말뭉치", "EK.txt"));
        for (String filename : filenames) {
            List<String> lines = FileUtil.load2List(filename, StandardCharsets.UTF_16);
            String problem = "";
            String answer = "";
            int lineCount = 0;
            for (String line : lines) {
                lineCount += 1;
                line = line.trim().replaceAll("[ ]+", "");
                if (line.startsWith("<orth>")) {
                    problem = line.replaceAll("<.+?>", "");
                }
                if (line.startsWith("<seg>")) {
                    answer += line.replaceAll("</mor><pos>", "/").replaceAll("<.+?>", "") + " ";
                }
                if (line.startsWith("</tok>")) {

                    problem = convertUnicodeJamoToJamoCompatibility(problem);
                    answer = convertUnicodeJamoToJamoCompatibility(answer);

                    if (!isValidFormat(problem + "\t" + answer)) {
                        problem = "";
                        answer = "";
                        System.out.println(filename + ":" + lineCount + ":" + line);
                        continue;
                    }
                    if (this.hasSingleJungSung(answer)) {
                        problem = "";
                        answer = "";
                        continue;
                    }

                    bw.write(problem + "\t" + answer.trim());
                    bw.newLine();
                    problem = "";
                    answer = "";
                }

            }
        }
        bw.close();
    }

    private boolean isValidFormat(String convertedPair) {
        try {
            if (convertedPair.trim().length() == 0) {
                return false;
            }
            if (convertedPair.trim().split("\t").length != 2) {
                System.out.println("To convert : " + convertedPair);
                return false;
            }
            String problem = convertedPair.split("\t")[0];
            String answer = convertedPair.split("\t")[1];
            if (problem.trim().length() == 0 || answer.trim().length() == 0) {
                return false;
            }
            String[] tokens = answer.split(" ");
            for (String token : tokens) {
                String[] morphPosToken = token.split("/");
                if (morphPosToken.length < 2) {
                    System.out.println("To convert : " + convertedPair);
                    return false;
                }

                if (morphPosToken.length > 2) {
                    if (token.equals("//SP")) {
                        continue;
                    }
                    return false;
                }

                if (morphPosToken[0].trim().length() == 0) {
                    return false;
                }
                if (morphPosToken[1].trim().length() == 0) {
                    return false;
                }
                String pos = morphPosToken[1].trim();
                if (!sejongTagSet.contains(pos)) {
//                    System.out.println("Wrong POS : (" + pos + ")" + convertedPair);
                    return false;
                }
                for (int i = 0; i < pos.length(); i++) {
                    if (!StringUtil.isEnglish(pos.charAt(i))) {
                        System.out.println("Token : " + token);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(convertedPair);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Test
    public void loadKJBilingualCorpusAndConvertUTF8() throws IOException {
        BufferedWriter bw = new BufferedWriter
                (new OutputStreamWriter(new FileOutputStream("D:\\shineware\\data\\komoran_training\\한일병렬_형태분석_말뭉치.refine.txt"), StandardCharsets.UTF_8));
        List<String> filenames = FileUtil.getFileNames("D:\\shineware\\data\\komoran_training\\한일병렬_형태분석_말뭉치", "KK.txt");
        filenames.addAll(FileUtil.getFileNames("D:\\shineware\\data\\komoran_training\\한일병렬_형태분석_말뭉치", "JK.txt"));
        for (String filename : filenames) {
            List<String> lines = FileUtil.load2List(filename, StandardCharsets.UTF_16);
            boolean isHeadArea = false;
            boolean isSentenceArea = false;
            int lineCount = 0;
            for (String line : lines) {
                lineCount += 1;
                line = line.trim();
                if (line.startsWith("<head")) {
                    isHeadArea = true;
                    continue;
                }
                if (line.endsWith("</head")) {
                    isHeadArea = false;
                    continue;
                }

                if (line.startsWith("<s ")) {
                    isSentenceArea = true;
                    continue;
                }
                if (line.endsWith("</s>")) {
                    isSentenceArea = false;
                    continue;
                }

                if (isHeadArea || isSentenceArea) {
                    line = convertUnicodeJamoToJamoCompatibility(line);
                    String[] entity = line.split("\t");
                    if (entity.length != 2) {
                        continue;
                    }
                    String problem = entity[0];
                    String answer = entity[1];
                    answer = answer.replaceAll("\\+", " ").replaceAll(" {2}", " +");
                    if (!isValidFormat(problem + "\t" + answer)) {
//                        System.out.println(filename + ":" + lineCount + ":" + line);
                        continue;
                    }
                    //구어체의 경우에는 불규칙의 형태가 타 코퍼스와 다르기 때문에 제외시킴
                    if (this.hasSingleJungSung(answer)) {
                        continue;
                    }
                    bw.write(problem + "\t" + answer);
                    bw.newLine();
                }
            }
        }
        bw.close();
    }

    //    @Before
    @Test
    public void loadSejongSpeechTextAndConvertUTF8() throws IOException {
        BufferedWriter bw = new BufferedWriter
                (new OutputStreamWriter(new FileOutputStream("D:\\shineware\\data\\komoran_training\\현대구어_형태분석_말뭉치.refine.txt"), StandardCharsets.UTF_8));
        List<String> filenames = FileUtil.getFileNames("D:\\shineware\\data\\komoran_training\\현대구어_형태분석_말뭉치");
        for (String filename : filenames) {
            List<String> lines = FileUtil.load2List(filename, StandardCharsets.UTF_16);

            boolean isTextArea = false;
            int lineCount = 0;
            for (String line : lines) {
                lineCount += 1;
                if (line.startsWith("<text>")) {
                    isTextArea = true;
                    continue;
                }
                if (line.startsWith("</text>")) {
                    isTextArea = false;
                    bw.newLine();
                    continue;
                }

                if (isTextArea) {
                    line = convertUnicodeJamoToJamoCompatibility(line);
                    String[] entity = line.split("\t");
                    if (entity.length != 3) {
                        continue;
                    }

                    if (this.hasSpecialCharacter(line)) {
                        continue;
                    }

                    String problem = entity[1];
                    String answer = entity[2];
                    //구어체에는 + 기호가 나오지 않기 때문에 바로 치환
                    answer = answer.replaceAll("\\+", " ");

                    if (!isValidFormat(problem + "\t" + answer)) {
//                        System.out.println(filename + ":" + lineCount + ":" + line);
                        continue;
                    }
                    //구어체의 경우에는 불규칙의 형태가 타 코퍼스와 다르기 때문에 제외시킴
                    if (this.hasSingleJungSung(answer)) {
                        continue;
                    }
                    bw.write(problem + "\t" + answer);
                    bw.newLine();
                }

            }
        }
        bw.close();
    }

    //    @Before
    @Test
    public void loadSejongTextAndConvertUTF8() throws IOException {
        BufferedWriter bw = new BufferedWriter
                (new OutputStreamWriter(new FileOutputStream("D:\\shineware\\data\\komoran_training\\현대문어_형태분석_말뭉치.refine.txt"), StandardCharsets.UTF_8));
        List<String> filenames = FileUtil.getFileNames("D:\\shineware\\data\\komoran_training\\현대문어_형태분석_말뭉치");
        for (String filename : filenames) {
            List<String> lines = FileUtil.load2List(filename, StandardCharsets.UTF_16);
            boolean isHeadArea = false;
            boolean isPhraseArea = false;
            int lineCount = 0;
            for (String line : lines) {
                lineCount += 1;
                if (line.startsWith("<head>")) {
                    isHeadArea = true;
                    continue;
                }
                if (line.startsWith("</head>")) {
                    bw.newLine();
                    isHeadArea = false;
                    continue;
                }

                if (line.startsWith("<p>")) {
                    isPhraseArea = true;
                    continue;
                }
                if (line.startsWith("</p>")) {
                    bw.newLine();
                    isPhraseArea = false;
                    continue;
                }

                if (isHeadArea || isPhraseArea) {
                    try {
                        line = convertUnicodeJamoToJamoCompatibility(line);
                        String problem = line.split("\t")[1];
                        String answers = line.split("\t")[2];
                        answers = answers.replaceAll(" \\+ ", " ");
                        if (!isValidFormat(problem + "\t" + answers)) {
//                            System.out.println(filename + ":" + lineCount + ":" + line);
                            continue;
                        }
                        bw.write(problem + "\t" + answers);
                        bw.newLine();
                    } catch (Exception e) {
//                        e.printStackTrace();
//                        System.err.println("File name : " + filename);
//                        System.err.println("Line no. : " + lineCount);
                    }

                }

            }
        }
        bw.close();
    }

    private String convertUnicodeJamoToJamoCompatibility(String line) {
        return HangulJamoUtil.ToHangulCompatibilityJamo(line);
    }
}

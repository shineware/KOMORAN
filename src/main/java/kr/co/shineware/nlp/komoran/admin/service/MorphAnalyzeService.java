package kr.co.shineware.nlp.komoran.admin.service;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.util.common.model.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Async("threadPoolTaskExecutor")
@Transactional
public class MorphAnalyzeService {
    private static final Logger logger = LoggerFactory.getLogger(MorphAnalyzeService.class);

    private static Komoran komoranLight;
    private static Komoran komoranFull;

    public MorphAnalyzeService() {
        logger.debug("Init Komoran Models...");
        this.komoranLight = new Komoran(DEFAULT_MODEL.LIGHT);
        this.komoranFull = new Komoran(DEFAULT_MODEL.FULL);
        logger.debug("Init Komoran Models... DONE");
    }

    public String analyze(String strToAnalyze, boolean useFullModel) {
        Komoran model = (useFullModel) ? this.komoranFull : this.komoranLight;

        return model.analyze(strToAnalyze).getPlainText();
    }

}

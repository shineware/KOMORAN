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
@Transactional
public class MorphAnalyzeService {
    private static final Logger logger = LoggerFactory.getLogger(MorphAnalyzeService.class);

    private static Komoran komoran;

    public MorphAnalyzeService() {
        logger.debug("Init Komoran Model...");
        this.komoran = new Komoran(DEFAULT_MODEL.LIGHT);
        logger.debug("Init Komoran Model... DONE");
    }

    public String analyze(String strToAnalyze) {
        return this.komoran.analyze(strToAnalyze).getPlainText();
    }

}

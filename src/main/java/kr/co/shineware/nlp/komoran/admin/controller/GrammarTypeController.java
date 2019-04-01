package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.domain.GrammarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/grammar/type")
public class GrammarTypeController {
    private static final Logger logger = LoggerFactory.getLogger(GrammarTypeController.class);

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<GrammarType, String> getAllGrammarTypes() {
        Map<GrammarType, String> allTypes = new LinkedHashMap<>();

        for (GrammarType g : GrammarType.values()) {
            allTypes.put(g, g.name() +"("+ g.getNodeName() +")");
        }

        return allTypes;
    }
}

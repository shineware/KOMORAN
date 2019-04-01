package kr.co.shineware.nlp.komoran.admin.controller;

import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/pos")
public class PosController {
    private static final Logger logger = LoggerFactory.getLogger(PosController.class);

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<PosType, String> getAllPosTypes() {
        Map<PosType, String> allTypes = new LinkedHashMap<>();

        for (PosType p : PosType.values()) {
            allTypes.put(p, p.getPosName());
        }

        return allTypes;
    }

}

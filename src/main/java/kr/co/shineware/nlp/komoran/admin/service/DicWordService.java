package kr.co.shineware.nlp.komoran.admin.service;

import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.repository.DicWordRepository;
import kr.co.shineware.nlp.komoran.admin.util.MessageBuilder;
import kr.co.shineware.nlp.komoran.admin.util.parser.query.FilterQueryParser;
import kr.co.shineware.nlp.komoran.admin.util.parser.query.SorterQueryParser;
import kr.co.shineware.nlp.komoran.admin.util.parser.stream.DicWordStreamParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class DicWordService {
    private static final Logger logger = LoggerFactory.getLogger(DicWordService.class);

    @Autowired
    private DicWordRepository dicWordRepository;


    @Transactional
    @SuppressWarnings("unchecked")
    public JSONObject checkGivenTokenAndPosTypeExist(String token, PosType pos) {
        JSONObject result = new JSONObject();
        JSONArray resultRows = new JSONArray();

        DicWord dicWordToFind = dicWordRepository.findByTokenAndPos(token, pos);

        if (dicWordToFind == null) {
            MessageBuilder.buildErrorMessage(result, "해당하는 단어/품사가 존재하지 않습니다.");
        } else {
            MessageBuilder.buildSuccessMessage(result);
            resultRows.add(dicWordToFind);
        }

        result.put("rows", resultRows);

        return result;
    }


    @Transactional
    public JSONObject updateTfById(int id, int tf) {
        JSONObject result = new JSONObject();
        DicWord dicWordToUpdate = dicWordRepository.findById(id);

        if (tf < 0) {
            MessageBuilder.buildErrorMessage(result, "잘못된 빈도입니다.");
            return result;
        }

        if (dicWordToUpdate == null) {
            MessageBuilder.buildErrorMessage(result, "ID가 존재하지 않습니다.");
        } else {
            dicWordToUpdate.setTf(tf);
            dicWordRepository.save(dicWordToUpdate);

            MessageBuilder.buildSuccessMessage(result);
        }

        return result;
    }


    @Transactional
    public JSONObject updatePosById(int id, PosType pos) {
        JSONObject result = new JSONObject();
        DicWord dicWordToUpdate = dicWordRepository.findById(id);
        DicWord duplicateItem = null;

        if (pos == null || pos.equals("")) {
            MessageBuilder.buildErrorMessage(result, "존재하지 않는 품사입니다.");
            return result;
        }

        if (dicWordToUpdate != null) {
            duplicateItem = dicWordRepository.findByTokenAndPos(dicWordToUpdate.getToken(), pos);
        }

        if (dicWordToUpdate == null) {
            MessageBuilder.buildErrorMessage(result, "ID가 존재하지 않습니다.");
        } else if (duplicateItem != null) {
            MessageBuilder.buildErrorMessage(result, "해당 단어/품사는 이미 등록되어 있습니다.");
        } else {
            dicWordToUpdate.setPos(pos);
            dicWordRepository.save(dicWordToUpdate);

            MessageBuilder.buildSuccessMessage(result);
        }

        return result;
    }


    @Transactional
    public JSONObject deleteItemById(int id) {
        JSONObject result = new JSONObject();
        DicWord dicWordToDelete = dicWordRepository.findById(id);

        if (dicWordToDelete == null) {
            MessageBuilder.buildErrorMessage(result, "ID가 존재하지 않습니다.");
        } else {
            dicWordRepository.delete(dicWordToDelete);

            MessageBuilder.buildSuccessMessage(result);
        }

        return result;
    }


    @Transactional
    @SuppressWarnings("unchecked")
    public JSONObject addItem(String token, PosType pos, int tf) {
        JSONObject result = new JSONObject();
        DicWord duplicateItem = dicWordRepository.findByTokenAndPos(token, pos);

        if (duplicateItem != null) {
            MessageBuilder.buildErrorMessage(result, "해당 단어/품사는 이미 등록되어 있습니다.");
        } else if (tf < 0) {
            MessageBuilder.buildErrorMessage(result, "잘못된 빈도입니다.");
        } else {
            DicWord dicWordToAdd = new DicWord(token, pos, tf);
            dicWordRepository.save(dicWordToAdd);
            MessageBuilder.buildSuccessMessage(result);
            result.put("data", dicWordToAdd);
        }

        return result;
    }


    //    @Async("threadPoolTaskExecutor")
    @Transactional
    public void purgeAllData() {
        logger.debug("Purging all data...");
        dicWordRepository.deleteAll();
        logger.debug("Purged");
    }


    //    @Async("threadPoolTaskExecutor")
    @Transactional
    public void importFromFile(Path savedFilePath) throws Exception {
        Stream<String> lines = Files.lines(savedFilePath);

        logger.debug("Purging all data before importing...");
        dicWordRepository.deleteAll();
        dicWordRepository.flush();
        logger.debug("Purged");

        logger.debug("Importing from file - " + savedFilePath);
//        lines.filter(line -> !line.isEmpty())
//                .filter(line -> line.split("\t").length >= 2)
//                .flatMap(DicWordStreamParser::parse)
//                .distinct()
//                .forEach(dicWordRepository::save);

        dicWordRepository.saveAll(
                lines.filter(line -> !line.isEmpty())
                .filter(line -> line.split("\t").length >= 2)
                .flatMap(DicWordStreamParser::parse)
                .distinct()
                .collect(Collectors.toList())
        );

        lines.close();
        dicWordRepository.flush();
        logger.debug("Imported");
    }


    public String exportToString() {
        Stream<String> dicWordExportStream = dicWordRepository.getAllItemsWithExportForm();

        return dicWordExportStream.collect(Collectors.joining("\n"));
    }


    @SuppressWarnings("unchecked")
    public JSONObject getDicWordList(int page, int size, Map<String, String> allParameters) {
        JSONObject result = new JSONObject();
        Page<DicWord> pageResult;

        SorterQueryParser sorterQueryParser = new SorterQueryParser();
        FilterQueryParser filterQueryParser = new FilterQueryParser();

        Sort sortBy = sorterQueryParser.getSorter(allParameters);
        HashMap<String, String> filters = filterQueryParser.getFilter(allParameters);

        logger.debug("Sorters: " + sortBy.toString());
        logger.debug("Filters: " + filters.toString());

        if (filters.containsKey("token") && filters.containsKey("pos") && filters.containsKey("tf")) {
            // token && pos && tf
            pageResult = dicWordRepository.findByTokenContainingAndPosAndTfGreaterThanEqual(
                    filters.get("token"), PosType.valueOf(filters.get("pos")), Integer.valueOf(filters.get("tf")),
                    PageRequest.of(page - 1, size, sortBy));
        } else if (filters.containsKey("token") && filters.containsKey("pos")) {
            // token && pos
            pageResult = dicWordRepository.findByTokenContainingAndPos(
                    filters.get("token"), PosType.valueOf(filters.get("pos")),
                    PageRequest.of(page - 1, size, sortBy));
        } else if (filters.containsKey("token") && filters.containsKey("tf")) {
            // token && tf
            pageResult = dicWordRepository.findByTokenContainingAndTfGreaterThanEqual(
                    filters.get("token"), Integer.valueOf(filters.get("tf")),
                    PageRequest.of(page - 1, size, sortBy));
        } else if (filters.containsKey("pos") && filters.containsKey("tf")) {
            // pos && tf
            pageResult = dicWordRepository.findByPosAndTfGreaterThanEqual(
                    PosType.valueOf(filters.get("pos")), Integer.valueOf(filters.get("tf")),
                    PageRequest.of(page - 1, size, sortBy));
        } else if (filters.containsKey("token")) {
            // token
            pageResult = dicWordRepository.findByTokenContaining(
                    filters.get("token"), PageRequest.of(page - 1, size, sortBy));
        } else if (filters.containsKey("pos")) {
            // pos
            pageResult = dicWordRepository.findByPos(
                    PosType.valueOf(filters.get("pos")), PageRequest.of(page - 1, size, sortBy));
        } else if (filters.containsKey("tf")) {
            // tf
            pageResult = dicWordRepository.findByTfGreaterThanEqual(
                    Integer.valueOf(filters.get("tf")), PageRequest.of(page - 1, size, sortBy));
        } else {
            // NOTHING
            pageResult = dicWordRepository.findAll(PageRequest.of(page - 1, size, sortBy));
        }

        result.put("data", pageResult.getContent());
        result.put("last_page", pageResult.getTotalPages());

        return result;
    }

}

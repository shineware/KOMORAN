package kr.co.shineware.nlp.komoran.admin.service;

import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceDuplicatedException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceNotFoundException;
import kr.co.shineware.nlp.komoran.admin.repository.DicWordRepository;
import kr.co.shineware.nlp.komoran.admin.util.parser.query.FilterQueryParser;
import kr.co.shineware.nlp.komoran.admin.util.parser.query.SorterQueryParser;
import kr.co.shineware.nlp.komoran.admin.util.parser.stream.DicWordStreamParser;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class DicWordService {
    private static final Logger logger = LoggerFactory.getLogger(DicWordService.class);

    @Autowired
    private DicWordRepository dicWordRepository;


    private DicWord getDicWordItem(int id) {
        DicWord dicWord = dicWordRepository.findById(id);

        if (dicWord == null) {
            throw new ResourceNotFoundException("ID: " + id);
        }

        return dicWord;
    }


    @Transactional
    public DicWord checkGivenTokenAndPosTypeExist(String token, PosType pos) {
        if (token == null || token.equals("")) {
            throw new ParameterInvalidException("token: " + token);
        } else if (pos == null || pos.getPosName().equals("")) {
            throw new ParameterInvalidException("품사: []");
        }

        DicWord dicWordToFind = dicWordRepository.findByTokenAndPos(token, pos);

        if (dicWordToFind == null) {
            throw new ResourceNotFoundException(token + "/" + pos.getPosName());
        }

        return dicWordToFind;
    }


    @Transactional
    public DicWord updateTfById(int id, int tf) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (tf < 0) {
            throw new ParameterInvalidException("빈도: " + tf);
        }

        DicWord dicWordToUpdate = getDicWordItem(id);

        dicWordToUpdate.setTf(tf);
        dicWordRepository.save(dicWordToUpdate);

        return dicWordToUpdate;
    }


    @Transactional
    public DicWord updatePosById(int id, PosType pos) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (pos == null || pos.getPosName().equals("")) {
            throw new ParameterInvalidException("품사: []");
        }

        DicWord dicWordToUpdate = getDicWordItem(id);

        DicWord duplicateItem = dicWordRepository.findByTokenAndPos(dicWordToUpdate.getToken(), pos);

        if (duplicateItem != null && duplicateItem.getId() != id) {
            throw new ResourceDuplicatedException(duplicateItem.getToken() + "/" + duplicateItem.getPos());
        }

        dicWordToUpdate.setPos(pos);
        dicWordRepository.save(dicWordToUpdate);

        return dicWordToUpdate;
    }


    @Transactional
    public DicWord deleteItemById(int id) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        }

        DicWord dicWordToDelete = getDicWordItem(id);

        dicWordRepository.delete(dicWordToDelete);

        return dicWordToDelete;
    }


    @Transactional
    public DicWord addItem(String token, PosType pos, int tf) {
        if (token == null || token.equals("")) {
            throw new ParameterInvalidException("token: " + token);
        } else if (pos == null || pos.getPosName().equals("")) {
            throw new ParameterInvalidException("품사: []");
        } else if (tf < 0) {
            throw new ParameterInvalidException("빈도: " + tf);
        }

        DicWord duplicateItem = dicWordRepository.findByTokenAndPos(token, pos);

        if (duplicateItem != null) {
            throw new ResourceDuplicatedException(duplicateItem.getToken() + "/" + duplicateItem.getPos());
        }

        DicWord dicWordToAdd = new DicWord(token, pos, tf);
        dicWordRepository.save(dicWordToAdd);

        return dicWordToAdd;
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

        // @formatter:off
        List<DicWord> dicWordsToInsert = lines.filter(line -> !line.isEmpty())
                                              .flatMap(DicWordStreamParser::parse)
                                              .distinct()
                                              .collect(Collectors.toList());
        // @formatter:on

        dicWordRepository.deleteAll();
        dicWordRepository.flush();

        dicWordRepository.saveAll(dicWordsToInsert);

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

        if (allParameters.isEmpty()) {
            pageResult = dicWordRepository.findAll(PageRequest.of(page - 1, size));
        } else {
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
        }

        result.put("data", pageResult.getContent());
        result.put("last_page", pageResult.getTotalPages());

        return result;
    }

}

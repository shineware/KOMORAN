package kr.co.shineware.nlp.komoran.admin.service;

import kr.co.shineware.nlp.komoran.admin.domain.GrammarIn;
import kr.co.shineware.nlp.komoran.admin.domain.GrammarType;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceDuplicatedException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceNotFoundException;
import kr.co.shineware.nlp.komoran.admin.repository.GrammarInRepository;
import kr.co.shineware.nlp.komoran.admin.util.parser.query.FilterQueryParser;
import kr.co.shineware.nlp.komoran.admin.util.parser.query.SorterQueryParser;
import kr.co.shineware.nlp.komoran.admin.util.parser.stream.GrammarInStreamParser;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class GrammarInService {
    private static final Logger logger = LoggerFactory.getLogger(GrammarInService.class);

    @Autowired
    private GrammarInRepository grammarInRepository;


    private GrammarIn getGrammarInItem(int id) {
        GrammarIn grammarIn = grammarInRepository.findById(id);

        if (grammarIn == null) {
            throw new ResourceNotFoundException("ID: " + id);
        }

        return grammarIn;
    }


    @Transactional
    public GrammarIn checkGivenStartAndNextExist(GrammarType start, GrammarType next) {
        if (start == null || start.getNodeName().equals("")) {
            throw new ParameterInvalidException("시작 품사: []");
        } else if (next == null || next.getNodeName().equals("")) {
            throw new ParameterInvalidException("다음 품사: []");
        }

        GrammarIn grammarInToFind = grammarInRepository.findByStartAndNext(start, next);

        if (grammarInToFind == null) {
            throw new ResourceNotFoundException(start.getNodeName() + "/" + next.getNodeName());
        }

        return grammarInToFind;
    }


    @Transactional
    public GrammarIn updateTfById(int id, int tf) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (tf < 0) {
            throw new ParameterInvalidException("빈도: " + tf);
        }

        GrammarIn grammarInToUpdate = getGrammarInItem(id);

        grammarInToUpdate.setTf(tf);
        grammarInRepository.save(grammarInToUpdate);

        return grammarInToUpdate;
    }


    @Transactional
    public GrammarIn updateStartById(int id, GrammarType start) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (start == null || start.getNodeName().equals("")) {
            throw new ParameterInvalidException("시작 품사: []");
        }

        GrammarIn grammarInToUpdate = getGrammarInItem(id);

        GrammarIn duplicateItem = grammarInRepository.findByStartAndNext(start, grammarInToUpdate.getNext());

        if (duplicateItem != null && duplicateItem.getId() != id) {
            throw new ResourceDuplicatedException(duplicateItem.getStart() + "/" + duplicateItem.getNext());
        }

        grammarInToUpdate.setStart(start);
        grammarInRepository.save(grammarInToUpdate);

        return grammarInToUpdate;
    }


    @Transactional
    public GrammarIn updateNextById(int id, GrammarType next) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (next == null || next.getNodeName().equals("")) {
            throw new ParameterInvalidException("다음 품사: []");
        }

        GrammarIn grammarInToUpdate = getGrammarInItem(id);

        GrammarIn duplicateItem = grammarInRepository.findByStartAndNext(grammarInToUpdate.getStart(), next);

        if (duplicateItem != null && duplicateItem.getId() != id) {
            throw new ResourceDuplicatedException(duplicateItem.getStart() + "/" + duplicateItem.getNext());
        }

        grammarInToUpdate.setNext(next);
        grammarInRepository.save(grammarInToUpdate);

        return grammarInToUpdate;
    }


    @Transactional
    public GrammarIn deleteItemById(int id) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        }

        GrammarIn grammarInToDelete = getGrammarInItem(id);

        grammarInRepository.delete(grammarInToDelete);

        return grammarInToDelete;
    }


    @Transactional
    public GrammarIn addItem(GrammarType start, GrammarType next, int tf) {
        if (start == null || start.getNodeName().equals("")) {
            throw new ParameterInvalidException("시작 품사: []");
        } else if (next == null || next.getNodeName().equals("")) {
            throw new ParameterInvalidException("다음 품사: []");
        } else if (tf < 0) {
            throw new ParameterInvalidException("빈도: " + tf);
        }

        GrammarIn duplicateItem = grammarInRepository.findByStartAndNext(start, next);

        if (duplicateItem != null) {
            throw new ResourceDuplicatedException(duplicateItem.getStart() + "/" + duplicateItem.getNext());
        }

        GrammarIn grammarInToAdd = new GrammarIn(start, next, tf);
        grammarInRepository.save(grammarInToAdd);

        return grammarInToAdd;
    }


    //    @Async("threadPoolTaskExecutor")
    @Transactional
    public void purgeAllData() {
        logger.debug("Purging all data...");
        grammarInRepository.deleteAll();
        logger.debug("Purged");
    }


    //    @Async("threadPoolTaskExecutor")
    @Transactional
    public void importFromFile(Path savedFilePath) throws IOException {
        Stream<String> lines = Files.lines(savedFilePath);

        // @formatter:off
        List<GrammarIn> grammarInsToInsert = lines.filter(line -> !line.isEmpty())
                .flatMap(GrammarInStreamParser::parse)
                .distinct()
                .collect(Collectors.toList());
        // @formatter:on

        grammarInRepository.deleteAll();
        grammarInRepository.flush();

        grammarInRepository.saveAll(grammarInsToInsert);

        lines.close();
        grammarInRepository.flush();
        logger.debug("Imported");
    }


    public String exportToString() {
        Stream<String> grammarInExportStream = grammarInRepository.getAllItemsWithExportForm();

        return grammarInExportStream.collect(Collectors.joining("\n"));
    }


    @SuppressWarnings("unchecked")
    public JSONObject getGrammarInList(int page, int size, Map<String, String> allParameters) {
        JSONObject result = new JSONObject();
        Page<GrammarIn> pageResult;

        if (allParameters.isEmpty()) {
            pageResult = grammarInRepository.findAll(PageRequest.of(page - 1, size));
        } else {
            SorterQueryParser sorterQueryParser = new SorterQueryParser();
            FilterQueryParser filterQueryParser = new FilterQueryParser();

            Sort sortBy = sorterQueryParser.getSorter(allParameters);
            HashMap<String, String> filters = filterQueryParser.getFilter(allParameters);

            logger.debug("Sorters: " + sortBy.toString());
            logger.debug("Filters: " + filters.toString());


            if (filters.containsKey("start") && filters.containsKey("next") && filters.containsKey("tf")) {
                // start && next && tf
                pageResult = grammarInRepository.findByStartAndNextAndTfGreaterThanEqual(
                        GrammarType.valueOf(filters.get("start")), GrammarType.valueOf(filters.get("next")),
                        Integer.valueOf(filters.get("tf")), PageRequest.of(page - 1, size, sortBy));
            } else if (filters.containsKey("start") && filters.containsKey("next")) {
                // start && next
                pageResult = grammarInRepository.findByStartAndNext(
                        GrammarType.valueOf(filters.get("start")), GrammarType.valueOf(filters.get("next")),
                        PageRequest.of(page - 1, size, sortBy));
            } else if (filters.containsKey("pos") && filters.containsKey("tf")) {
                // start && tf
                pageResult = grammarInRepository.findByStartAndTfGreaterThanEqual(
                        GrammarType.valueOf(filters.get("start")), Integer.valueOf(filters.get("tf")),
                        PageRequest.of(page - 1, size, sortBy));
            } else if (filters.containsKey("next") && filters.containsKey("tf")) {
                // next && tf
                pageResult = grammarInRepository.findByNextAndTfGreaterThanEqual(
                        GrammarType.valueOf(filters.get("next")), Integer.valueOf(filters.get("tf")),
                        PageRequest.of(page - 1, size, sortBy));
            } else if (filters.containsKey("start")) {
                // start
                pageResult = grammarInRepository.findByStart(
                        GrammarType.valueOf(filters.get("start")), PageRequest.of(page - 1, size, sortBy));
            } else if (filters.containsKey("next")) {
                // next
                pageResult = grammarInRepository.findByNext(
                        GrammarType.valueOf(filters.get("next")), PageRequest.of(page - 1, size, sortBy));
            } else if (filters.containsKey("tf")) {
                // tf
                pageResult = grammarInRepository.findByTfGreaterThanEqual(
                        Integer.valueOf(filters.get("tf")), PageRequest.of(page - 1, size, sortBy));
            } else {
                // NOTHING
                pageResult = grammarInRepository.findAll(PageRequest.of(page - 1, size, sortBy));
            }
        }

        result.put("data", pageResult.getContent());
        result.put("last_page", pageResult.getTotalPages());

        return result;
    }

}

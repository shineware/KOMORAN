package kr.co.shineware.nlp.komoran.admin.service;

import kr.co.shineware.nlp.komoran.admin.domain.FwdUser;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceDuplicatedException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceNotFoundException;
import kr.co.shineware.nlp.komoran.admin.repository.FwdUserRepository;
import kr.co.shineware.nlp.komoran.admin.util.parser.query.FilterQueryParser;
import kr.co.shineware.nlp.komoran.admin.util.parser.query.SorterQueryParser;
import kr.co.shineware.nlp.komoran.admin.util.parser.stream.FwdUserStreamParser;
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
public class FwdUserService {
    private static final Logger logger = LoggerFactory.getLogger(FwdUserService.class);

    @Autowired
    private FwdUserRepository fwdUserRepository;


    private FwdUser getFwdUserItem(int id) {
        FwdUser fwdUser = fwdUserRepository.findById(id);

        if (fwdUser == null) {
            throw new ResourceNotFoundException("ID: " + id);
        }

        return fwdUser;
    }


    private boolean isValidFwdAnalyzedResult(String analyzed) {
        if (analyzed == null || "".equals(analyzed)) {
            return false;
        }

        String[] analyzedResults = analyzed.split(" ");

        for (String anAnalyzedResult : analyzedResults) {
            String[] tmpAnalyzedResults = anAnalyzedResult.split("/");

            // INVALID FORM
            if (tmpAnalyzedResults.length < 2) {
                return false;
            }

            // FOR VALIDATING ANALYZED RESULT, PARSE IT
            String tmpAnalyzedTerm;
            String tmpAnalyzedPosInRaw;

            // Term includes delimiter '/'
            if (tmpAnalyzedResults.length > 2) {
                int delimiterIdx = anAnalyzedResult.lastIndexOf("/");
                tmpAnalyzedTerm = anAnalyzedResult.substring(0, delimiterIdx);
                tmpAnalyzedPosInRaw = anAnalyzedResult.substring(delimiterIdx+1);
            }
            else {
                tmpAnalyzedTerm = tmpAnalyzedResults[0];
                tmpAnalyzedPosInRaw = tmpAnalyzedResults[1];
            }

            // VALIDATE ANALYZED TERM
            if ("".equals(tmpAnalyzedTerm)) {
                return false;
            }

            // VALIDATE ANALYZED POS
            try {
                PosType.valueOf(tmpAnalyzedPosInRaw);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        return true;
    }


    @Transactional
    public FwdUser checkGivenFullAndAnalyzedExist(String full, String analyzed) {
        if (full == null || "".equals(full)) {
            throw new ParameterInvalidException("기분석 어절: "+ full);
        } else if (!isValidFwdAnalyzedResult(analyzed)) {
            throw new ParameterInvalidException("기분석 결과: "+ analyzed);
        }

        FwdUser fwdUserToFind = fwdUserRepository.findByFullAndAnalyzed(full, analyzed);

        if (fwdUserToFind == null) {
            throw new ResourceNotFoundException(full + "=>" + analyzed);
        }

        return fwdUserToFind;
    }


    @Transactional
    public FwdUser updateFullById(int id, String full) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (full == null || "".equals(full)) {
            throw new ParameterInvalidException("기분석 어절: "+ full);
        }

        FwdUser fwdUserToUpdate = getFwdUserItem(id);

        fwdUserToUpdate.setFull(full);
        fwdUserRepository.save(fwdUserToUpdate);

        return fwdUserToUpdate;
    }


    @Transactional
    public FwdUser updateAnalyzedById(int id, String analyzed) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (!isValidFwdAnalyzedResult(analyzed)) {
            throw new ParameterInvalidException("기분석 결과: "+ analyzed);
        }

        FwdUser fwdUserToUpdate = getFwdUserItem(id);

        fwdUserToUpdate.setAnalyzed(analyzed);
        fwdUserRepository.save(fwdUserToUpdate);

        return fwdUserToUpdate;
    }


    @Transactional
    public FwdUser deleteItemById(int id) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        }

        FwdUser fwdUserToDelete = getFwdUserItem(id);

        fwdUserRepository.delete(fwdUserToDelete);

        return fwdUserToDelete;
    }


    @Transactional
    public FwdUser addItem(String full, String analyzed) {
        if (full == null || "".equals(full)) {
            throw new ParameterInvalidException("기분석 어절: "+ full);
        } else if (!isValidFwdAnalyzedResult(analyzed)) {
            throw new ParameterInvalidException("기분석 결과: "+ analyzed);
        }

        FwdUser duplicateItem = fwdUserRepository.findByFull(full);

        if (duplicateItem != null) {
            throw new ResourceDuplicatedException(duplicateItem.getFull());
        }

        FwdUser fwdUserToAdd = new FwdUser(full, analyzed);
        fwdUserRepository.save(fwdUserToAdd);

        return fwdUserToAdd;
    }


    //    @Async("threadPoolTaskExecutor")
    @Transactional
    public void purgeAllData() {
        logger.debug("Purging all data...");
        fwdUserRepository.deleteAll();
        logger.debug("Purged");
    }


    //    @Async("threadPoolTaskExecutor")
    @Transactional
    public void importFromFile(Path savedFilePath) throws Exception {
        Stream<String> lines = Files.lines(savedFilePath);

        // @formatter:off
        List<FwdUser> fwdUsersToInsert = lines.filter(line -> !line.isEmpty())
                                             .flatMap(FwdUserStreamParser::parse)
                                             .distinct()
                                             .collect(Collectors.toList());
        // @formatter:on

        fwdUserRepository.deleteAll();
        fwdUserRepository.flush();

        fwdUserRepository.saveAll(fwdUsersToInsert);

        lines.close();
        fwdUserRepository.flush();
        logger.debug("Imported");
    }


    public String exportToString() {
        Stream<String> fwdUserExportStream = fwdUserRepository.getAllItemsWithExportForm();

        return fwdUserExportStream.collect(Collectors.joining("\n"));
    }


    @SuppressWarnings("unchecked")
    public JSONObject getFwdUserList(int page, int size, Map<String, String> allParameters) {
        JSONObject result = new JSONObject();
        Page<FwdUser> pageResult;

        if (allParameters.isEmpty()) {
            pageResult = fwdUserRepository.findAll(PageRequest.of(page - 1, size));
        } else {
            SorterQueryParser sorterQueryParser = new SorterQueryParser();
            FilterQueryParser filterQueryParser = new FilterQueryParser();

            Sort sortBy = sorterQueryParser.getSorter(allParameters);
            HashMap<String, String> filters = filterQueryParser.getFilter(allParameters);

            logger.debug("Sorters: " + sortBy.toString());
            logger.debug("Filters: " + filters.toString());


            if (filters.containsKey("full") && filters.containsKey("analyzed")) {
                // full && analyzed
                pageResult = fwdUserRepository.findByFullContainingAndAnalyzedContaining(
                        filters.get("full"), filters.get("analyzed"), PageRequest.of(page - 1, size, sortBy));
            } else if (filters.containsKey("full")) {
                // full
                pageResult = fwdUserRepository.findByFullContaining(
                        filters.get("full"), PageRequest.of(page - 1, size, sortBy));
            } else if (filters.containsKey("analyzed")) {
                // analyzed
                pageResult = fwdUserRepository.findByAnalyzedContaining(
                        filters.get("analyzed"), PageRequest.of(page - 1, size, sortBy));
            } else {
                // NOTHING
                pageResult = fwdUserRepository.findAll(PageRequest.of(page - 1, size, sortBy));
            }
        }

        result.put("data", pageResult.getContent());
        result.put("last_page", pageResult.getTotalPages());

        return result;
    }

}

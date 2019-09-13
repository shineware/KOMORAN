package kr.co.shineware.nlp.komoran.admin.service;

import kr.co.shineware.nlp.komoran.admin.domain.DicUser;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceDuplicatedException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceNotFoundException;
import kr.co.shineware.nlp.komoran.admin.repository.DicUserRepository;
import kr.co.shineware.nlp.komoran.admin.util.parser.query.FilterQueryParser;
import kr.co.shineware.nlp.komoran.admin.util.parser.query.SorterQueryParser;
import kr.co.shineware.nlp.komoran.admin.util.parser.stream.DicUserStreamParser;
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
public class DicUserService {
    private static final Logger logger = LoggerFactory.getLogger(DicUserService.class);

    @Autowired
    private DicUserRepository dicUserRepository;


    private DicUser getDicUserItem(int id) {
        DicUser dicUser = dicUserRepository.findById(id);

        if (dicUser == null) {
            throw new ResourceNotFoundException("ID: " + id);
        }

        return dicUser;
    }


    @Transactional
    public DicUser checkGivenTokenAndPosTypeExist(String token, PosType pos) {
        if (token == null || token.equals("")) {
            throw new ParameterInvalidException("token: " + token);
        } else if (pos == null || pos.getPosName().equals("")) {
            throw new ParameterInvalidException("품사: []");
        }

        DicUser dicUserToFind = dicUserRepository.findByTokenAndPos(token, pos);

        if (dicUserToFind == null) {
            throw new ResourceNotFoundException(token + "/" + pos.getPosName());
        }

        return dicUserToFind;
    }


    @Transactional
    public DicUser updateTokenById(int id, String token) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (token == null || "".equals(token)) {
            throw new ParameterInvalidException("단어: []");
        }

        DicUser dicUserToUpdate = getDicUserItem(id);

        DicUser duplicateItem = dicUserRepository.findByTokenAndPos(token, dicUserToUpdate.getPos());

        if (duplicateItem != null && duplicateItem.getId() != id) {
            throw new ResourceDuplicatedException(duplicateItem.getToken() + "/" + duplicateItem.getPos());
        }

        dicUserToUpdate.setToken(token);
        dicUserRepository.save(dicUserToUpdate);

        return dicUserToUpdate;
    }


    @Transactional
    public DicUser updatePosById(int id, PosType pos) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        } else if (pos == null || pos.getPosName().equals("")) {
            throw new ParameterInvalidException("품사: []");
        }

        DicUser dicUserToUpdate = getDicUserItem(id);

        DicUser duplicateItem = dicUserRepository.findByTokenAndPos(dicUserToUpdate.getToken(), pos);

        if (duplicateItem != null && duplicateItem.getId() != id) {
            throw new ResourceDuplicatedException(duplicateItem.getToken() + "/" + duplicateItem.getPos());
        }

        dicUserToUpdate.setPos(pos);
        dicUserRepository.save(dicUserToUpdate);

        return dicUserToUpdate;
    }


    @Transactional
    public DicUser deleteItemById(int id) {
        if (id <= 0) {
            throw new ParameterInvalidException("ID: " + id);
        }

        DicUser dicUserToDelete = getDicUserItem(id);

        dicUserRepository.delete(dicUserToDelete);

        return dicUserToDelete;
    }


    @Transactional
    public DicUser addItem(String token, PosType pos) {
        if (token == null || token.equals("")) {
            throw new ParameterInvalidException("token: " + token);
        } else if (pos == null || pos.getPosName().equals("")) {
            throw new ParameterInvalidException("품사: []");
        }

        DicUser duplicateItem = dicUserRepository.findByTokenAndPos(token, pos);

        if (duplicateItem != null) {
            throw new ResourceDuplicatedException(duplicateItem.getToken() + "/" + duplicateItem.getPos());
        }

        DicUser dicUserToAdd = new DicUser(token, pos);
        dicUserRepository.save(dicUserToAdd);

        return dicUserToAdd;
    }


    //    @Async("threadPoolTaskExecutor")
    @Transactional
    public void purgeAllData() {
        logger.debug("Purging all data...");
        dicUserRepository.deleteAll();
        logger.debug("Purged");
    }


    //    @Async("threadPoolTaskExecutor")
    @Transactional
    public void importFromFile(Path savedFilePath) throws IOException {
        Stream<String> lines = Files.lines(savedFilePath);

        // @formatter:off
        List<DicUser> dicUsersToInsert = lines.filter(line -> !line.isEmpty())
                                                .filter(line -> !line.startsWith("#"))
                                                .flatMap(DicUserStreamParser::parse)
                                                .distinct()
                                                .collect(Collectors.toList());
        // @formatter:on

        dicUserRepository.deleteAll();
        dicUserRepository.flush();

        dicUserRepository.saveAll(dicUsersToInsert);

        lines.close();
        dicUserRepository.flush();
        logger.debug("Imported");
    }


    public String exportToString() {
        Stream<String> dicUserExportStream = dicUserRepository.getAllItemsWithExportForm();

        return dicUserExportStream.collect(Collectors.joining("\n"));
    }


    @SuppressWarnings("unchecked")
    public JSONObject getDicUserList(int page, int size, Map<String, String> allParameters) {
        JSONObject result = new JSONObject();
        Page<DicUser> pageResult;

        if (allParameters.isEmpty()) {
            pageResult = dicUserRepository.findAll(PageRequest.of(page - 1, size));
        } else {
            SorterQueryParser sorterQueryParser = new SorterQueryParser();
            FilterQueryParser filterQueryParser = new FilterQueryParser();

            Sort sortBy = sorterQueryParser.getSorter(allParameters);
            HashMap<String, String> filters = filterQueryParser.getFilter(allParameters);

            logger.debug("Sorters: " + sortBy.toString());
            logger.debug("Filters: " + filters.toString());


            if (filters.containsKey("token") && filters.containsKey("pos")) {
                // token && pos
                pageResult = dicUserRepository.findByTokenContainingAndPos(
                        filters.get("token"), PosType.valueOf(filters.get("pos")),
                        PageRequest.of(page - 1, size, sortBy));
            } else if (filters.containsKey("token")) {
                // token
                pageResult = dicUserRepository.findByTokenContaining(
                        filters.get("token"), PageRequest.of(page - 1, size, sortBy));
            } else if (filters.containsKey("pos")) {
                // pos
                pageResult = dicUserRepository.findByPos(
                        PosType.valueOf(filters.get("pos")), PageRequest.of(page - 1, size, sortBy));
            } else {
                // NOTHING
                pageResult = dicUserRepository.findAll(PageRequest.of(page - 1, size, sortBy));
            }
        }

        result.put("data", pageResult.getContent());
        result.put("last_page", pageResult.getTotalPages());

        return result;
    }

}

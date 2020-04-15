package kr.co.shineware.nlp.komoran.admin.util.parser.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SorterQueryParser extends QueryParser {
    private static final Logger logger = LoggerFactory.getLogger(SorterQueryParser.class);

    public SorterQueryParser() {
        super("sorters\\[([0-9]+)\\]\\[(field|dir)\\]");
    }


    @Override
    protected boolean validateItem(Map<String, String> itemToCheck) {
        // Check Key validation
        if (itemToCheck.containsKey("field") && itemToCheck.containsKey("dir")) {
            // Check Value validation
            if (itemToCheck.get("dir").equalsIgnoreCase("asc") || itemToCheck.get("dir").equalsIgnoreCase("desc")) {
                return true;
            }
        }

        return false;
    }


    public Sort getSorter(Map<String, String> inputParam) {
        ArrayList<HashMap<String, String>> sorters = this.parse(inputParam);
        Sort sortBy = null;

        if (sorters == null) {
            return null;
        } else if (sorters.isEmpty()) {
            sortBy = Sort.by("token").ascending();
        } else {
            for (HashMap<String, String> sorter : sorters) {
                Sort.Direction dir = sorter.get("dir").equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

                if (sortBy == null) {
                    sortBy = Sort.by(dir, sorter.get("field"));
                } else {
                    sortBy = sortBy.and(Sort.by(dir, sorter.get("field")));
                }
            }
        }

        return sortBy;
    }


}

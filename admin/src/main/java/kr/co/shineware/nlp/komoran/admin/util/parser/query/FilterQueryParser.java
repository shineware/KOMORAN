package kr.co.shineware.nlp.komoran.admin.util.parser.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FilterQueryParser extends QueryParser {
    private static final Logger logger = LoggerFactory.getLogger(FilterQueryParser.class);

    public FilterQueryParser() {
        super("filters\\[([0-9]+)\\]\\[(field|type|value)\\]");
    }


    @Override
    protected boolean validateItem(Map<String, String> itemToCheck) {
        // Check Key validation
        if (itemToCheck.containsKey("field") && itemToCheck.containsKey("type") && itemToCheck.containsKey("value")) {
            return true;
        }

        return false;
    }


    public HashMap<String, String> getFilter(Map<String, String> inputParam) {
        ArrayList<HashMap<String, String>> rawFilters = this.parse(inputParam);
        HashMap<String, String> filters = new HashMap<>();

        for (HashMap<String, String> rawFilter : rawFilters) {
            filters.put(rawFilter.get("field"), rawFilter.get("value"));
        }

        return filters;
    }
}
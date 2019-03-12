package kr.co.shineware.nlp.komoran.admin.util.parser.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {
    private static final Logger logger = LoggerFactory.getLogger(QueryParser.class);

    private Pattern parserPattern;
    private Matcher parserMatcher;
    private ArrayList<HashMap<String, String>> parsedResults;
    private HashMap<String, String> tmpParsedItem;
    private int parseIndex;


    public QueryParser(String patternString) {
        this.parserPattern = Pattern.compile(patternString);
    }


    // Should Override this method
    protected boolean validateItem(Map<String, String> itemToCheck) {
        return false;
    }


    public ArrayList<HashMap<String, String>> parse(Map<String, String> inputParam) {
        if (inputParam.isEmpty()) {
            return null;
        }

        this.parsedResults = new ArrayList<>();
        this.tmpParsedItem = new HashMap<>();
        this.parseIndex = 0;

        for (String paramKey : inputParam.keySet()) {
            this.parserMatcher = parserPattern.matcher(paramKey);

            if (this.parserMatcher.find()) {
                if (this.parseIndex != Integer.valueOf(this.parserMatcher.group(1))) {
                    // Add only valid items
                    if (this.validateItem(this.tmpParsedItem)) {
                        this.tmpParsedItem.put("_order", Integer.toString(this.parseIndex));
                        this.parsedResults.add(this.tmpParsedItem);
                    }

                    this.tmpParsedItem = new HashMap<>();
                    this.parseIndex = Integer.valueOf(this.parserMatcher.group(1));
                }

                this.tmpParsedItem.put(this.parserMatcher.group(2), inputParam.get(paramKey));
            }
        }

        if (this.validateItem(this.tmpParsedItem)) {
            this.tmpParsedItem.put("_order", Integer.toString(this.parseIndex));
            this.parsedResults.add(this.tmpParsedItem);
        }

        // Sort by '_order' key
        Collections.sort(this.parsedResults, new OrderKeyComparator());

        logger.debug("Parsed Result: "+ this.parsedResults.toString());

        return this.parsedResults;
    }

    class OrderKeyComparator implements Comparator<HashMap<String, String>> {
        @Override
        public int compare(HashMap<String, String> objOne, HashMap<String, String> objAnother) {
            return (new Integer(objOne.get("_order"))).compareTo(new Integer(objAnother.get("_order")));
        }
    }
}

package de.rwth.dbis.neologism.recommender.BatchRecommender;

import de.rwth.dbis.neologism.recommender.BatchQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryPreprocessor {

    private static QueryPreprocessor instance;

    private Map<String, String> originalProcessedKeyword;

    private QueryPreprocessor() {
        originalProcessedKeyword = new HashMap<>();
    }


    public static QueryPreprocessor getInstance() {

        if (QueryPreprocessor.instance == null) {
            QueryPreprocessor.instance = new QueryPreprocessor();
        }
        return QueryPreprocessor.instance;
    }

    public BatchQuery preprocess(BatchQuery query) {
        System.out.println(query);
        List<String> classes = preprocessStrings(query.classes);
        List<String> properties = preprocessStrings(query.properties);

        return new BatchQuery(query.domain, classes, properties);

    }

    private List<String> preprocessStrings(List<String> strings) {
        List<String> results = new ArrayList<>();
        System.out.println(strings);


        for (int j = 0; j < strings.size(); j++) {
            String s = strings.get(j);
            String res = s;
            for (int i = 1; i < s.length(); i++) {

                if (Character.isUpperCase(s.charAt(i)) && !Character.isSpaceChar(s.charAt(i - 1))) {
                    res = s.substring(0, i) + " " + s.substring(i);
                    i++;
                }
            }
            res.replace("-", " ");
            results.add(res);
            originalProcessedKeyword.put(res, s);
        }
        ;


        return results;

    }

    public String getOriginalKeyword(String keyword){
        return originalProcessedKeyword.get(keyword);
    }

}

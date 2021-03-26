package de.rwth.dbis.neologism.recommender.BatchRecommender;

import de.rwth.dbis.neologism.recommender.BatchQuery;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueryPreprocessor {

    private static QueryPreprocessor instance;

    public static QueryPreprocessor getInstance() {

        if (QueryPreprocessor.instance == null) {
            QueryPreprocessor.instance = new QueryPreprocessor();
        }
        return QueryPreprocessor.instance;
    }

    public BatchQuery preprocess(BatchQuery query){
        System.out.println(query);
        List<String> classes = preprocessStrings(query.classes);
        List<String> properties = preprocessStrings(query.properties);

        return new BatchQuery(query.domain, classes, properties);

    }
    private List<String> preprocessStrings(List<String> strings) {
        List<String> results = new ArrayList<>();
        System.out.println(strings);
        strings.stream().forEach(s -> {
            for (int i = 1; i < s.length(); i++) {

                if(Character.isUpperCase(s.charAt(i))){
                    s= s.substring(0,i) + " " + s.substring(i);
                    i++;
                }
            }
            s.replace("-", " ");
            results.add(s);
        });
        return results;

    }

}

package de.rwth.dbis.neologism.recommender;

import com.google.common.base.Joiner;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations.Recommendation;
import org.apache.jena.ext.com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendationConsolidator implements Recommender {

    private final List<Recommender> recommenders;
    private final String combinedName;

    public RecommendationConsolidator(Recommender... recommenders) {
        this(Arrays.asList(recommenders));
    }

    public RecommendationConsolidator(Iterable<Recommender> recs) {
        this.recommenders = Lists.newArrayList(recs);
        Joiner j = Joiner.on(" AND ");
        combinedName = j
                .join(this.recommenders.stream().map(Recommender::getRecommenderName).collect(Collectors.toList()));
    }

    @Override
    public Recommendations recommend(Query c) {

        List<Recommendations> results = recommenders.parallelStream().map(rec -> rec.recommend(c))
                .collect(Collectors.toList());
        List<Recommendation> l = new ArrayList<>();
        for (Recommendations recommendations : results) {
            l.addAll(recommendations.list);
        }
        return new Recommendations(l, this.getRecommenderName());
    }

    @Override
    public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
        PropertiesForClass.Builder b = new PropertiesForClass.Builder();

        List<PropertiesForClass> results = recommenders.parallelStream().map(rec -> rec.getPropertiesForClass(q))
                .collect(Collectors.toList());

        for (PropertiesForClass propertiesForClass : results) {
            b.addFromPropertiesForClass(propertiesForClass);
        }

        return b.build();
    }

    @Override
    public String getRecommenderName() {
        return combinedName;
    }

}

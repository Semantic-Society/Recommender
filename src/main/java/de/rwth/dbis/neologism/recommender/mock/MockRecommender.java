package de.rwth.dbis.neologism.recommender.mock;

import de.rwth.dbis.neologism.recommender.*;
import de.rwth.dbis.neologism.recommender.Recommendation.Recommendations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockRecommender implements Recommender {
    public static final String NAME = "Mock.Recommender";
    private static final List<Recommendations.Recommendation> RECOMMENDATIONS;

    static {
        RECOMMENDATIONS = new ArrayList<>();
        RECOMMENDATIONS.add(new Recommendations.Recommendation(
                "http://foo.bar/mock/Banana",
                NAME,
                Collections.singletonList(new Recommendations.StringLiteral(Recommendations.Language.EN, "Banana")),
                Collections.singletonList(new Recommendations.StringLiteral(Recommendations.Language.EN, "A tasty fruit"))
        ));
    }

    @Override
    public Recommendations recommend(Query c) {
        return new Recommendations(
                RECOMMENDATIONS,
                NAME
        );
    }

    @Override
    public PropertiesForClass getPropertiesForClass(PropertiesQuery q) {
        return PropertiesForClass.EMPTY;
    }

    @Override
    public String getRecommenderName() {
        return NAME;
    }
}

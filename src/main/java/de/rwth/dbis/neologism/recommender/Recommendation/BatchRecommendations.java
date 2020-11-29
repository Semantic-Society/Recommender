package de.rwth.dbis.neologism.recommender.Recommendation;

import java.util.List;

public class BatchRecommendations extends Recommendations {

    private final String keyword;


    public BatchRecommendations(List<Recommendation> l, String creator, String keyword) {
        super(l, creator);
        this.keyword = keyword;
    }

    public BatchRecommendations(Recommendations recommendations, String keyword){
        super(recommendations.list, recommendations.creator);
        this.keyword = keyword;
    }

    public BatchRecommendations(BatchRecommendations copyInstance){
        super(copyInstance.list, copyInstance.creator);
        this.keyword = copyInstance.keyword;
    }

    public String getKeyword() {
        return keyword;
    }
}

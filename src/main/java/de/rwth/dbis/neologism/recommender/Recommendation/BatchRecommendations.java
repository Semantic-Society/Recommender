package de.rwth.dbis.neologism.recommender.Recommendation;

import de.rwth.dbis.neologism.recommender.ranking.RatedRecommendation;

import java.util.ArrayList;
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
    @Override
    public BatchRecommendations cleanAllExceptEnglish() {
        List<Recommendation> cleanedList = new ArrayList<>();
        for (Recommendation original : this.list) {
            Recommendation.Builder b = new Recommendation.Builder(original.getOntology(), original.getURI());
            for (StringLiteral originalLabel : original.getLabel()) {
                if (originalLabel.language.equals(Language.EN)) {
                    b.addLabel(originalLabel);
                }
            }
            for (StringLiteral originalComment : original.getComments()) {
                if (originalComment.language.equals(Language.EN)) {
                    b.addComment(originalComment);
                }
            }
            Recommendation cleaned = b.build();
            //RatedRecommendation originalRated = (RatedRecommendation) original;
            //RatedRecommendation cleanedRated = new RatedRecommendation(cleaned,originalRated.getScore());
            cleanedList.add(cleaned);
        }
        return new BatchRecommendations(cleanedList, this.creator,this.keyword);
    }

}

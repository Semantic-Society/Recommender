package de.rwth.dbis.neologism.recommender.recommendation;

import java.util.ArrayList;
import java.util.List;

public class BatchRecommendations extends Recommendations {

    private String keyword;


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
            Recommendation.Builder b = new Recommendation.Builder(original.getOntology(), original.getUri());
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
            cleanedList.add(cleaned);
        }
        return new BatchRecommendations(cleanedList, this.creator,this.keyword);
    }


    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }


}

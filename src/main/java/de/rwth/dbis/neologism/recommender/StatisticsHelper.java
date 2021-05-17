package de.rwth.dbis.neologism.recommender;

import de.rwth.dbis.neologism.recommender.ranking.RatedRecommendation;
import de.rwth.dbis.neologism.recommender.recommendation.BatchRecommendations;
import de.rwth.dbis.neologism.recommender.recommendation.Recommendations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsHelper {

    public static void getStatistics(Map<String, List<de.rwth.dbis.neologism.recommender.recommendation.Recommendations>> recommenderResults,  List<BatchRecommendations> rankingResults ){

        Map<String,String> lovFirstRankKeywordURI = new HashMap<>();
        for(String keyword: recommenderResults.keySet()){
            recommenderResults.get(keyword).forEach((r)-> lovFirstRankKeywordURI.put(keyword, r.list.size()>0?r.list.get(0).getUri():"empty"));
        }
        try (PrintWriter writer = new PrintWriter(new File("test.csv"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Keyword");
            sb.append(",");
            sb.append("RecommendationRank,");
            sb.append(',');
            sb.append("URI");
            sb.append(',');
            sb.append("LOVRANK");
            sb.append(',');
            sb.append("LOV URI First");
            sb.append(',');
            sb.append("Score");
            sb.append('\n');
            for(BatchRecommendations br: rankingResults){
                AtomicInteger count = new AtomicInteger(1);
                System.out.println(br.getKeyword());

                List<Recommendations> lovRecs = recommenderResults.get(br.getKeyword());




                br.list.forEach(( rec)->{

                    RatedRecommendation t = (RatedRecommendation) rec;
                    AtomicInteger count2 = new AtomicInteger(0);

                    lovRecs.forEach((recs) -> {
                        recs.list.stream().peek(x ->  count2.getAndIncrement()).filter((rec2) -> rec2.getUri().equals(t.getUri())).findFirst();
                    });

                    sb.append(br.getKeyword());
                    sb.append(',');
                    sb.append(count);
                    sb.append(',');
                    sb.append(t.getUri());
                    sb.append(',');
                    sb.append(count2);
                    sb.append(',');
                    if(lovFirstRankKeywordURI.get(br.getKeyword()).equals(t.getUri())){
                        sb.append(lovFirstRankKeywordURI.get(br.getKeyword()));
                    }else{
                        sb.append("-");
                    }
                    sb.append(',');
                    sb.append(Math.round(t.getScore()* 100.0) / 100.0);
                    sb.append('\n');
                    System.out.println("RankRec: "+ count + " URI: "+ t.getUri() + " LOVRANK: "+ count2 + " Score: " + Math.round(t.getScore()* 100.0) / 100.0 + " ")  ;
                    count.getAndIncrement();



                });

            }
            writer.write(sb.toString());

            System.out.println("done!");

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());

        }
    }
}

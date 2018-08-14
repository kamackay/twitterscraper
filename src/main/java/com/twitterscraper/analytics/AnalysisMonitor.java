package com.twitterscraper.analytics;

import com.google.inject.Inject;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.monitors.AbstractMonitor;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisMonitor extends AbstractMonitor {

    protected final Logger logger = LoggerFactory.getLogger(AnalysisMonitor.class);

    private static final long ALLOWED_TIME = 10000;

    private static StanfordCoreNLP pipeline;

    @Inject
    public AnalysisMonitor(final DatabaseWrapper db) {
        super(db);
        try {
            pipeline = new StanfordCoreNLP("nlp.properties");
        } catch (Exception e) {
            pipeline = null;
        }
    }

    protected void handleQuery(final String name) {
        final long startTime = System.currentTimeMillis();
        for (long id : db.getAllIds(name, true)) {
            // Stop this if it has been running too long
            if (System.currentTimeMillis() - startTime > ALLOWED_TIME) break;
            db.getById(name, id).ifPresent(this::analyzeTweet);
            break;
        }
    }

    private void analyzeTweet(final Document tweet) {

    }

    private static int getSentiment(final String tweet) {
        if (pipeline == null) return 0;
        int mainSentiment = 0;
        if (tweet != null && tweet.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(tweet);
            for (CoreMap sentence : annotation
                    .get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
            }
        }
        return mainSentiment;
    }
}

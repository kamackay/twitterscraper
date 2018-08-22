package com.twitterscraper.analytics;


import com.google.inject.Inject;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static com.twitterscraper.db.Transforms.ANALYSIS;
import static com.twitterscraper.db.Transforms.TEXT;
import static com.twitterscraper.utils.TweetPrinter.removeEmojis;

public class SentimentAnalyzerImpl implements SentimentAnalyzer {
    protected final Logger logger = LoggerFactory.getLogger(SentimentAnalyzerImpl.class);

    private final StanfordCoreNLP pipeline;

    @Inject
    SentimentAnalyzerImpl() {
        final Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    int findSentiment(final String line) {
        int mainSentiment = 0;
        if (line != null && line.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(line);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
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

    public AnalysisResult analyze(final Document tweet) {
        if (!needsAnalysis(tweet)) return new AnalysisResult(tweet, false);
        try {
            tweet.append(ANALYSIS,
                    new Document("time", System.currentTimeMillis())
                            .append("result", analyzeTweet(tweet)));
            return new AnalysisResult(tweet, true);
        } catch (Exception e) {
            logger.error("Error analyzing tweet", e);
        }
        return new AnalysisResult(tweet, false);
    }

    int analyzeTweet(final Document tweet) {
        return findSentiment(removeEmojis(tweet.getString(TEXT)));
    }

    private boolean needsAnalysis(final Document tweet) {
        return tweet.get(ANALYSIS) == null;
    }

    public class AnalysisResult {
        private final Document tweet;
        private final boolean success;

        public AnalysisResult(final Document tweet, final boolean success) {
            this.tweet = tweet;
            this.success = success;
        }

        public Document getTweet() {
            return tweet;
        }

        public boolean wasSuccessful() {
            return success;
        }
    }
}

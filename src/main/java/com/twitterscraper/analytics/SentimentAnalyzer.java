package com.twitterscraper.analytics;

import org.bson.Document;

public interface SentimentAnalyzer {
    SentimentAnalyzerImpl.AnalysisResult analyze(final Document tweet);
}

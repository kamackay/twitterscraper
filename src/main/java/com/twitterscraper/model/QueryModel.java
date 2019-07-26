package com.twitterscraper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;

import java.util.List;

@lombok.Data
@lombok.Builder(toBuilder = true, builderClassName = "Builder", access = AccessLevel.PUBLIC)
@lombok.NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@lombok.AllArgsConstructor(access = AccessLevel.PUBLIC)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryModel {
  List<String> mentions;
  List<String> quotes;
  List<String> hashtags;
  boolean includeRetweets;
  String queryName;
  boolean updateExisting = true;
}

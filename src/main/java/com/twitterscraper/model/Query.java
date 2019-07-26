package com.twitterscraper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.twitterscraper.utils.Elective;
import lombok.AccessLevel;

@lombok.Data
@lombok.Builder(toBuilder = true, builderClassName = "Builder", access = AccessLevel.PUBLIC)
@lombok.NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@lombok.AllArgsConstructor(access = AccessLevel.PUBLIC)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Query implements ICloneable<Query> {
  private twitter4j.Query query;
  private QueryModel model;

  public String getName() {
    return Elective.of(model)
        .map(QueryModel::getQueryName)
        .orElse(null);
  }

  @Override
  public Query copy() {
    return new Query(new twitter4j.Query(query.getQuery()),
        model);
  }
}

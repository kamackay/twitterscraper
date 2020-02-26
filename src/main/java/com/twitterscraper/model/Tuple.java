package com.twitterscraper.model;

public class Tuple<L, R> {

  @lombok.Getter
  private final L left;
  @lombok.Getter
  private final R right;

  private Tuple(final L l, final R r) {
    this.left = l;
    this.right = r;
  }

  public static <A, B> Tuple<A, B> of(final A a, final B b) {
    return new Tuple<>(a, b);
  }
}

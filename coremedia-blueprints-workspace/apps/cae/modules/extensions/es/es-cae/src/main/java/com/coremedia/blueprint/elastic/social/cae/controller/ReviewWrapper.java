package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.elastic.social.api.reviews.Review;

import java.util.List;


public class ReviewWrapper extends ContributionWrapper<Review, ReviewWrapper> {

  public ReviewWrapper(Review review, List<ReviewWrapper> subReviews) {
    super(review, subReviews);
  }

  public Review getReview() {
    return super.getContribution();
  }

  public void setReview(Review review) {
    super.setContribution(review);
  }
}

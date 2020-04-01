package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.elastic.social.api.comments.Comment;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;

/**
 * @cm.template.api
 */
public class CommentWrapper extends ContributionWrapper<Comment, CommentWrapper> {

  public CommentWrapper(@NonNull Comment comment, @Nullable List<CommentWrapper> subComments) {
    super(comment, subComments);
  }

  /**
   * @cm.template.api
   */
  public Comment getComment() {
    return super.getContribution();
  }

  public void setComment(Comment comment) {
    super.setContribution(comment);
  }

  /**
   * @cm.template.api
   */
  public List<CommentWrapper> getSubComments() {
    return super.getSubContributions();
  }
}

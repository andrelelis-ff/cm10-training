package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.elastic.social.cae.ElasticSocialService;
import com.coremedia.blueprint.elastic.social.cae.WrapperHelper;
import com.coremedia.elastic.social.api.ContributionType;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.api.users.CommunityUser;

import java.util.ArrayList;
import java.util.List;

/**
 * @cm.template.api
 */
public class CommentsResult extends ListContributionResult<CommentWrapper> {

  public CommentsResult(Object target) {
    super(target);
  }

  public CommentsResult(Object target,
                        CommunityUser user,
                        ElasticSocialService service,
                        boolean feedbackEnabled,
                        ContributionType contributionType) {
    super(target, user, service, feedbackEnabled, contributionType);
  }

  public List<CommentWrapper> getComments() {
    return super.getContributions();
  }

  /**
   * @cm.template.api
   */
  public List<CommentWrapper> getRootComments() {
    return super.getRootContributions();
  }

  public int getNumberOfComments() {
    return super.getNumberOfContributions();
  }

  @Override
  protected void load() {
    List<Comment> comments = getElasticSocialService().getComments(target, user);
    setContributions(WrapperHelper.getCommentWrappers(comments));
  }

  @Override
  protected List<CommentWrapper> findRootContributions() {
    List<CommentWrapper> rootComments = new ArrayList<>();
    if (getComments() != null) {
      for (CommentWrapper wrapper : getComments()) {
        Comment replyTo = wrapper.getContribution().getReplyTo();
        if (replyTo == null) {
          rootComments.add(wrapper);
        }
      }
    }
    return rootComments;
  }
}

package com.coremedia.blueprint.elastic.social.cae;

import com.coremedia.blueprint.elastic.social.cae.controller.CommentWrapper;
import com.coremedia.elastic.core.api.SortOrder;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.api.comments.SortHelper;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WrapperHelper {

  private WrapperHelper() {
  }

  @NonNull
  public static List<CommentWrapper> getCommentWrappers(@Nullable List<Comment> comments) {
    if (comments == null || comments.isEmpty()) {
      return Collections.emptyList();
    }

    List<Comment> additionalComments = getAdditionalComments(comments);
    if (!additionalComments.isEmpty()) {
      comments.addAll(additionalComments);
      // we need to sort the list again
      SortHelper.sortThreadedDiscussion(comments, SortOrder.ASCENDING);
    }
    Map<Comment, List<Comment>> commentsMap = getCommentsMap(comments);

    // we need to create wrappers which contain the list of subcomments for each comment
    Map<Comment, CommentWrapper> wrappedComments = new HashMap<>(comments.size());
    List<CommentWrapper> wrappers = new ArrayList<>(comments.size());
    for (Comment aComment : comments) {
      CommentWrapper wrapper = createCommentWrapper(aComment, commentsMap, wrappedComments);
      wrappers.add(wrapper);
    }
    return wrappers;
  }

  /**
   * Find additional comments (without duplicates)
   * @param comments list of comments to extend
   */
  private static List<Comment> getAdditionalComments(List<Comment> comments) {
    List<Comment> additionalComments = new ArrayList<>();
    // first we check if we need additional comments for the wrappers
    for (Comment comment : comments) {
      Comment replyTo = comment.getReplyTo();
      if (replyTo != null && !comments.contains(replyTo)) {
        // we need to add the whole tree to the root now
        addAdditionalComments(additionalComments, replyTo);
      }
    }
    removeDuplicates(additionalComments, comments);
    return additionalComments;
  }

  /**
   * Adds all the missing {@link Comment}s up to the root of the discussion for the given {@link Comment} to the list.
   *
   * @param commentsList a list of {@link Comment}s
   * @param comment      a {@link Comment} for which all reply to targets up to the root of the discussion should be added
   */
  private static void addAdditionalComments(List<Comment> commentsList, Comment comment) {
    List<Comment> listToRoot = comment.getListToRoot();
    for (Comment replyToComment : listToRoot) {
      if (!commentsList.contains(replyToComment)) {
        commentsList.add(replyToComment);
      }
    }
  }

  /**
   * Creates a {@link Map} of all given {@link Comment}s with a {@link List} of all its subcomments as map value.
   *
   * @param comments a list of {@link Comment}s
   * @return a {@link Map} of all given {@link Comment}s with a {@link List} of all its subcomments as map value
   */
  private static Map<Comment, List<Comment>> getCommentsMap(List<Comment> comments) {
    Map<Comment, List<Comment>> commentsMap = new HashMap<>();
    // we need to make a map entry for each comment in the list with all of its subcomments
    for (Comment comment : comments) {
      ensureMapEntry(commentsMap, comment);
      Comment replyTo = comment.getReplyTo();
      if (replyTo != null) {
        ensureMapEntry(commentsMap, replyTo);
        List<Comment> currentSubComments = commentsMap.get(replyTo);
        if (!currentSubComments.contains(comment)) {
          currentSubComments.add(comment);
        }
      }
    }
    return commentsMap;
  }

  private static CommentWrapper createCommentWrapper(Comment comment, Map<Comment, List<Comment>> commentsMap, Map<Comment, CommentWrapper> wrappedComments) {
    CommentWrapper commentWrapper = wrappedComments.get(comment);
    if (commentWrapper == null) {
      List<Comment> subComments = commentsMap.get(comment);
      if (subComments == null || subComments.isEmpty()) {
        commentWrapper = new CommentWrapper(comment, Collections.<CommentWrapper>emptyList());
      } else {
        List<CommentWrapper> commentWrappers = new ArrayList<>(subComments.size());
        for (Comment subComment : subComments) {
          commentWrappers.add(createCommentWrapper(subComment, commentsMap, wrappedComments));
        }
        commentWrapper = new CommentWrapper(comment, commentWrappers);
      }
      wrappedComments.put(comment, commentWrapper);
    }
    return commentWrapper;
  }

  private static void ensureMapEntry(Map<Comment, List<Comment>> commentsMap, Comment comment) {
    if (!commentsMap.containsKey(comment)) {
      commentsMap.put(comment, new ArrayList<Comment>());
    }
  }

  /**
   * The addAdditionalComments list may contain duplicates since each added comment goes up to the root, so some of the
   * nodes are already in the origin comment list.
   *
   * @param additionalComments the list of the missing {@link Comment}s
   * @param comments           the origin list of {@link Comment}s
   */
  private static void removeDuplicates(List<Comment> additionalComments, List<Comment> comments) {
    for (Comment comment : comments) {
      if (additionalComments.contains(comment)) {
        additionalComments.remove(comment);
      }
    }
  }
}

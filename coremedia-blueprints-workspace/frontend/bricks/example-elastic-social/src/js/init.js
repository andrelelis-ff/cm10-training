import $ from "jquery";
import * as timezone from "./es.timezone";
import { determine_timezone } from "./es.timezone";
import * as nodeDecorationService from "@coremedia/brick-node-decoration-service";
import {
  addNotifications,
  clearNotifications,
  EVENT_FORM_CLOSE,
  EVENT_FORM_SUBMIT,
  EVENT_MODEL_INFO,
  EVENT_TOGGLE_AVERAGE_RATING,
  formSubmitEnd,
  formSubmitStart,
} from "./es";
import {
  EVENT_LAYOUT_CHANGED,
  ajax,
  findAndSelf,
} from "@coremedia/brick-utils";
import { refreshFragment } from "@coremedia/brick-dynamic-include";

const ES_AJAX_FORM_IDENTIFIER = "cm-es-ajax-form";
const COMMENTS_IDENTIFIER = "cm-comments";
const NEW_COMMENT_IDENTIFIER = "cm-new-comment";
const REVIEWS_IDENTIFIER = "cm-reviews";
const NEW_REVIEW_IDENTIFIER = "cm-new-review";

const $document = $(document);

/* --- document ready ----------------------------------------------------------------------------------------------- */
$(function() {
  // Sets a timezone string, i.e. "Europe/Berlin", into the element with corresponding id .
  $("#coremedia-blueprint-basic-timezone").val(determine_timezone().name());
  $("#timezone").val(timezone.determine_timezone().name());
});

// apply confirm functionality to all elements rendered with necessary information
nodeDecorationService.addNodeDecoratorByData(
  { message: undefined },
  "cm-button--confirm",
  function($target, config) {
    if (config.message !== undefined) {
      $target.bind("click", function() {
        return confirm(config.message);
      });
    }
  }
);

// activate es ajax forms
nodeDecorationService.addNodeDecoratorByData(
  {},
  ES_AJAX_FORM_IDENTIFIER,
  function($form) {
    $form.on("submit", function(ev) {
      ev.preventDefault();
      if (formSubmitStart($form)) {
        clearNotifications($form);
        $form.trigger(EVENT_FORM_SUBMIT);
        ajax({
          type: $form.attr("method"),
          url: $form.attr("action"),
          data: $form.serialize(),
          dataType: "json",
          global: true, // keep default value after using common ajax function, not sure if this is important
        })
          .done(function(result) {
            result = $.extend(
              { success: false, messages: [], id: undefined },
              result
            );
            if (result.success) {
              $form.trigger(EVENT_MODEL_INFO, [result]);
            } else {
              addNotifications($form, result.messages);
              /*global grecaptcha*/
              if (typeof grecaptcha !== "undefined") {
                //reset recaptcha if recaptcha is enabled and an error is found
                grecaptcha.reset();
              }
            }
            $document.trigger(EVENT_LAYOUT_CHANGED);
          })
          .fail(function() {
            addNotifications($form, [
              {
                type: "error",
                text: "Due to an internal error, comment could not be posted.",
              },
            ]);
          })
          .always(function() {
            formSubmitEnd($form);
          });
        $document.trigger(EVENT_LAYOUT_CHANGED);
      }
    });
  }
);

// activate cancel functionality for es forms
nodeDecorationService.addNodeDecoratorByData({}, "cm-button--cancel", function(
  $button
) {
  $button.on("click", function() {
    $button.trigger(EVENT_FORM_CLOSE);
  });
});

// activate write a comment functionality for buttons (not the submit button, just for displaying the form)
nodeDecorationService.addNodeDecoratorByData(
  {
    replyTo: undefined,
    quote: { author: undefined, date: undefined, text: undefined },
  },
  "cm-button--comment",
  function($commentButton, config) {
    $commentButton.on("click", function() {
      const $comments = $commentButton.closest("." + COMMENTS_IDENTIFIER);
      // deactivate all active buttons due to form element being reused
      $comments
        .find(".cm-toolbar--comments")
        .removeClass("cm-toolbar--inactive");
      const $toolbar = $commentButton.closest(".cm-toolbar--comments");
      $toolbar.addClass("cm-toolbar--inactive");
      const $container = $comments.find(
        "." + COMMENTS_IDENTIFIER + "__new-comment"
      );
      // reset form
      $container.find("." + NEW_COMMENT_IDENTIFIER + "__form").each(function() {
        this.reset();
        clearNotifications(this);
      });
      $container.addClass(NEW_COMMENT_IDENTIFIER + "--active");

      const $replyToField = $container.find("[name='replyTo']");
      const $commentField = $container.find("[name='comment']");
      const commentField = $commentField[0];

      $replyToField.val(config.replyTo || "");
      if (config.quote.text !== undefined) {
        $commentField.val(
          "[quote author='" +
            config.quote.author.replace("'", "\\''") +
            "' date='" +
            config.quote.date.replace("'", "\\''") +
            "']" +
            config.quote.text +
            "[/quote]\n"
        );
      }
      $toolbar.after($container);
      $commentField.focus();
      // function exists in non IE browsers
      if (commentField.setSelectionRange) {
        // non IE
        const len = $commentField.val().length;
        commentField.setSelectionRange(len, len);
      } else {
        // IE
        $commentField.val($commentField.val());
      }
      commentField.scrollTop = commentField.scrollHeight;
      $document.trigger(EVENT_LAYOUT_CHANGED);
    });
  }
);

// activate functionality for new comment form
nodeDecorationService.addNodeDecoratorBySelector(
  "." + NEW_COMMENT_IDENTIFIER,
  function($newCommentWidget) {
    // catch es ajax form events
    findAndSelf(
      $newCommentWidget,
      "form." + NEW_COMMENT_IDENTIFIER + "__form"
    ).each(function() {
      const $newCommentForm = $(this);
      const $commentsWidget = $newCommentForm.closest(
        "." + COMMENTS_IDENTIFIER
      );
      $newCommentForm.on(EVENT_FORM_SUBMIT, function() {
        clearNotifications($commentsWidget);
        $document.trigger(EVENT_LAYOUT_CHANGED);
      });
      $newCommentForm.on(EVENT_MODEL_INFO, function(event, handlerInfo) {
        if (handlerInfo.success) {
          refreshFragment($commentsWidget, function(
            _,
            $commentsWidgetRefreshed
          ) {
            if (handlerInfo.id !== undefined) {
              const $comment = $commentsWidgetRefreshed.find(
                "[data-cm-comment-id='" + handlerInfo.id + "']"
              );
              addNotifications($comment, handlerInfo.messages);
              $("html, body").animate(
                {
                  scrollTop: $comment.offset().top,
                },
                500
              );
            } else {
              // fallback if no id is provided
              addNotifications($commentsWidgetRefreshed, handlerInfo.messages);
            }
            $document.trigger(EVENT_LAYOUT_CHANGED);
          });
        }
      });
    });

    // activate cancel functionality for comment form
    $newCommentWidget.on(EVENT_FORM_CLOSE, function() {
      $newCommentWidget.removeClass(NEW_COMMENT_IDENTIFIER + "--active");
      $newCommentWidget
        .closest("." + COMMENTS_IDENTIFIER)
        .find(".cm-toolbar--comments")
        .removeClass("cm-toolbar--inactive");
      $document.trigger(EVENT_LAYOUT_CHANGED);
    });
  }
);

// activate write a comment functionality for buttons (not the submit button, just for displaying the form)
nodeDecorationService.addNodeDecoratorByData(
  { disabled: false },
  "cm-button--review",
  function($reviewButton, config) {
    if (!config.disabled) {
      $reviewButton.on("click", function() {
        const $reviews = $reviewButton.closest("." + REVIEWS_IDENTIFIER);
        // deactivate all active buttons due to form element being reused
        $reviews
          .find(".cm-toolbar--reviews")
          .removeClass("cm-toolbar--inactive");
        const $toolbar = $reviewButton.closest(".cm-toolbar--reviews");
        $toolbar.addClass("cm-toolbar--inactive");
        const $container = $reviews.find(
          "." + REVIEWS_IDENTIFIER + "__new-review"
        );
        // reset form
        $container
          .find("." + NEW_REVIEW_IDENTIFIER + "__form")
          .each(function() {
            this.reset();
            clearNotifications(this);
          });
        $container.addClass(NEW_REVIEW_IDENTIFIER + "--active");

        const $reviewField = $container.find("[name='review']");

        $toolbar.after($container);
        $reviewField.focus();
        $document.trigger(EVENT_LAYOUT_CHANGED);
      });
    }
  }
);

// activate functionality for new review form
nodeDecorationService.addNodeDecoratorBySelector(
  "." + NEW_REVIEW_IDENTIFIER,
  function($newReviewWidget) {
    // catch form submit for review functionality and replace it with ajax call
    findAndSelf(
      $newReviewWidget,
      "form." + NEW_REVIEW_IDENTIFIER + "__form"
    ).each(function() {
      const $newReviewForm = $(this);
      const $reviewsWidget = $newReviewForm.closest("." + REVIEWS_IDENTIFIER);
      $newReviewForm.on(EVENT_FORM_SUBMIT, function() {
        clearNotifications($reviewsWidget);
        $document.trigger(EVENT_LAYOUT_CHANGED);
      });
      $newReviewForm.on(EVENT_MODEL_INFO, function(event, modelInfo) {
        if (modelInfo.success) {
          refreshFragment($reviewsWidget, function(_, $reviewsWidgetRefreshed) {
            if (modelInfo.id !== undefined) {
              const $review = $reviewsWidgetRefreshed.find(
                "[data-cm-review-id='" + modelInfo.id + "']"
              );
              addNotifications($review, modelInfo.messages);
              $("html, body").animate(
                {
                  scrollTop: $review.offset().top,
                },
                500
              );
            } else {
              // fallback if no id is provided
              addNotifications($reviewsWidgetRefreshed, modelInfo.messages);
            }
            $document.trigger(EVENT_LAYOUT_CHANGED);
          });
        }
      });
    });

    $newReviewWidget.on(EVENT_FORM_CLOSE, function() {
      $newReviewWidget.removeClass(NEW_REVIEW_IDENTIFIER + "--active");
      $newReviewWidget
        .closest("." + REVIEWS_IDENTIFIER)
        .find(".cm-toolbar--reviews")
        .removeClass("cm-toolbar--inactive");
      $document.trigger(EVENT_LAYOUT_CHANGED);
    });
  }
);

// initialize reviews widget
nodeDecorationService.addNodeDecoratorBySelector(
  ".cm-ratings-average",
  function($target) {
    $target.on(EVENT_TOGGLE_AVERAGE_RATING, function() {
      $target.toggleClass("cm-ratings-average--active");
    });
  }
);

// add readmore functionality if text is too long
nodeDecorationService.addNodeDecoratorByData(
  { lines: undefined },
  "cm-readmore",
  function($target, config) {
    const blockReadMore = "cm-readmore";
    // read the line height for the given target
    let lineHeight = $target.css("line-height");
    // only proceed if config is valid and lineHeight could be retrieved
    if (config.lines !== undefined && lineHeight !== undefined) {
      const $wrapper = $target.find("." + blockReadMore + "__wrapper");
      const $buttonbar = $target.find("." + blockReadMore + "__buttonbar");
      const $buttonMore = $buttonbar.find(
        "." + blockReadMore + "__button-more"
      );
      const $buttonLess = $buttonbar.find(
        "." + blockReadMore + "__button-less"
      );

      // calculate line height in px
      if (lineHeight.indexOf("px") > -1) {
        // line height is already in px, just remove the unit
        lineHeight = lineHeight.replace("px", "");
      } else {
        // line height is relative to font-size, calculate line height by multiplying its value with font-size
        lineHeight = lineHeight * $target.css("font-size").replace("px", "");
      }
      const maxHeight = Math.floor(lineHeight * config.lines);
      // enable readmore functionality if text without the readmore button exceeds the maximum height
      // it would make no sense to add a readmore button if it would take more space as rendering the full text
      if ($wrapper.height() - 2 * $buttonbar.height() > maxHeight) {
        $target.addClass(blockReadMore + "--enabled");
        // default without any action by the user ist the non expanded (less) version
        $target.addClass(blockReadMore + "--less");
        $wrapper.css("max-height", maxHeight);
        $buttonMore.on("click", function() {
          $target.removeClass(blockReadMore + "--less");
          $target.addClass(blockReadMore + "--more");
          $wrapper.css("max-height", "");
          $document.trigger(EVENT_LAYOUT_CHANGED);
        });
        $buttonLess.on("click", function() {
          $target.removeClass(blockReadMore + "--more");
          $target.addClass(blockReadMore + "--less");
          $wrapper.css("max-height", maxHeight);
          $document.trigger(EVENT_LAYOUT_CHANGED);
        });
        $buttonLess.on("click", function() {
          $target.removeClass(blockReadMore + "--more");
          $target.addClass(blockReadMore + "--less");
          $wrapper.css("max-height", maxHeight);
          $document.trigger(EVENT_LAYOUT_CHANGED);
        });
        $document.trigger(EVENT_LAYOUT_CHANGED);
      }
    }
  }
);

import $ from "jquery";
import { findAndSelf } from "@coremedia/brick-utils";

const EVENT_PREFIX = "coremedia.es.";
export const EVENT_FORM_CLOSE = EVENT_PREFIX + "formClose";
export const EVENT_FORM_SUBMIT = EVENT_PREFIX + "formSubmit";
export const EVENT_MODEL_INFO = EVENT_PREFIX + "modelInfo";
export const EVENT_TOGGLE_AVERAGE_RATING = EVENT_PREFIX + "toggleAverageRating";

const NOTIFICATION_TYPES = ["info", "error", "warning", "success"];
const NOTIFICATION_IDENTIFIER = "cm-notification";

/**
 * Decorates given container with notifications based on list of messages given.
 * Messages need to have the following structure:
 * {type: {String}, path: {undefined|String}, text: {string}}
 *
 * @param container the node to be decorated
 * @param messages the messages to apply.
 */
export function addNotifications(container, messages) {
  const $container = $(container);
  const $notificationByPath = {};

  // create a list of notification hooks by path
  const selector = "[data-" + NOTIFICATION_IDENTIFIER + "]";
  findAndSelf($container, selector).each(function() {
    const $this = $(this);
    const config = $.extend({ path: "" }, $this.data(NOTIFICATION_IDENTIFIER));
    $notificationByPath[config.path] = $this;
  });

  // iterate over all given messages
  for (let i = 0; i < messages.length; i++) {
    const message = $.extend(
      { type: "info", path: undefined, text: "" },
      messages[i]
    );
    if (message.path === undefined) {
      message.path = "";
    }
    // find notification in map
    const $notification = $notificationByPath[message.path];

    if ($notification !== undefined) {
      // assign information to notification and make it visible
      $notification
        .find("." + NOTIFICATION_IDENTIFIER + "__text")
        .html(message.text);
      if (NOTIFICATION_TYPES.indexOf(message.type) > -1) {
        $notification.addClass(NOTIFICATION_IDENTIFIER + "--" + message.type);
      }
      $notification.removeClass(NOTIFICATION_IDENTIFIER + "--inactive");
    }
  }
}

/**
 * Clears all notifications from the given container
 *
 * @param container the node to be cleared
 */
export function clearNotifications(container) {
  const $container = $(container);
  const $notifications = $container.find(
    "[data-" + NOTIFICATION_IDENTIFIER + "]"
  );
  for (let i = 0; i < NOTIFICATION_TYPES.length - 1; i++) {
    $notifications.removeClass(
      NOTIFICATION_IDENTIFIER + "--" + NOTIFICATION_TYPES[i]
    );
  }
  $notifications.addClass(NOTIFICATION_IDENTIFIER + "--inactive");
}

const FORM_IDENTIFIER = "cm-form";

/**
 * Starts a form submit (prevent double submitting)
 * If submitting is done without page reload (e.g. ajax) formSubmitEnd has to be called once finished.
 *
 * @returns {boolean} TRUE if start was successfull
 */
export function formSubmitStart(form) {
  const $form = $(form);
  const result = $form.hasClass(FORM_IDENTIFIER + "--progress");
  $form.addClass(FORM_IDENTIFIER + "--progress");
  return !result;
}

/**
 * Ends a form submit (prevent double submitting)
 * Only used if submitting is done without page reload (e.g. ajax)
 *
 * @returns {boolean} TRUE if end was successfull
 */
export function formSubmitEnd(form) {
  const $form = $(form);
  const result = $form.hasClass(FORM_IDENTIFIER + "--progress");
  $form.removeClass(FORM_IDENTIFIER + "--progress");
  return result;
}

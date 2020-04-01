import $ from "jquery";
import * as utils from "@coremedia/brick-utils";

import "mediaelement/full";
import "mediaelement/build/renderers/dailymotion";
import "mediaelement/build/renderers/twitch";
import "mediaelement/build/renderers/vimeo";
import "mediaelement/build/renderers/facebook";

/**
 * @event "videoStart" triggered to start the video
 */
export const EVENT_VIDEO_START = "videoStart";

/**
 * @event "videoStop" triggered to stop the video
 */
export const EVENT_VIDEO_STOP = "videoStop";

/**
 * @event "videoEnded" triggered when a video has ended.
 */
export const EVENT_VIDEO_ENDED = "videoEnded";

/**
 * @event "videoTimeUpdated" triggered when the current playtime has been updated.
 * @type {Object}
 * @property {int} position - indicates the current video position in milliseconds
 */
export const EVENT_VIDEO_TIME_UPDATED = "videoTimeUpdated";

/**
 * Generates a mediaElement for the given Video
 * See renderers for supported external videos
 *
 * @param videoElement
 */
function videoAsMediaElement(videoElement) {
  const $video = $(videoElement);
  const $document = $(document);

  // type detection:
  const videoUrl = $video.attr("src");
  if (videoUrl.indexOf("youtube.com") !== -1) {
    $video.attr("type", "video/youtube");
  }
  if (videoUrl.indexOf("vimeo.com") !== -1) {
    $video.attr("type", "video/vimeo");
  }

  // mediaElement object of the video
  // eslint-disable-next-line no-undef
  const me = new MediaElement(
    videoElement,
    {
      renderers: [
        "html5",
        "youtube_iframe",
        "vimeo_iframe",
        //"dailymotion_iframe", // not tested yet
        //"facebook", // not tested yet
      ],
      stretching: "fill",
      fakeNodeName: "cm-mediaelementwrapper",
      useDefaultControls: true,

      // events of videos
      success: function(mediaElement) {
        const $mediaElement = $(mediaElement);
        // attach css class
        $mediaElement.addClass("cm-mediaelementwrapper");
        // video loaded
        mediaElement.addEventListener(
          "loadedmetadata",
          function() {
            utils.log(
              "Video " +
                mediaElement.src +
                " (" +
                (mediaElement.muted ? "muted, " : "") +
                (mediaElement.loop ? "looped, " : "") +
                (mediaElement.autoplay ? "autoplay, " : "") +
                (mediaElement.controls !== false ? "controls" : "no-controls") +
                ") loaded.",
              $video
            );
            $document.trigger(utils.EVENT_LAYOUT_CHANGED);
          },
          false
        );

        // video started
        mediaElement.addEventListener(
          "playing",
          function() {
            utils.log("Video started with duration of " + me.duration + "ms.");
          },
          false
        );

        // video ended
        // delegate to own event, so the implementation does not rely on MediaElement Plugin
        // additionally youtube/vimeo/.. players could be able to trigger videoEnded event
        mediaElement.addEventListener(
          "ended",
          function() {
            utils.log("Video playback ended.");
            $video.trigger(EVENT_VIDEO_ENDED);
          },
          false
        );

        // track position of video and trigger EVENT_VIDEO_TIME_UPDATED
        // used in shoppable video
        mediaElement.addEventListener(
          "timeupdate",
          function(event) {
            let currentTime = event.detail.target.currentTime;
            $video.trigger(EVENT_VIDEO_TIME_UPDATED, {
              position: Math.floor(currentTime) * 1000,
            });
          },
          false
        );
      },
      error: function(mediaElement) {
        utils.error("Error: Could not load video.", mediaElement.src);
      },
    },
    null
  );

  // start video, triggered by EVENT_VIDEO_START from outside, like in shoppable video or pdp
  $video.on(EVENT_VIDEO_START, function() {
    utils.log("Video started by EVENT_VIDEO_START");
    me.play();
  });

  // stop video, triggered by EVENT_VIDEO_STOP from outside, like in popup
  $video.on(EVENT_VIDEO_STOP, function() {
    utils.log("Video stopped by EVENT_VIDEO_STOP");
    me.pause();
  });
}

/**
 * default wrapper function to handle dom elements or jQuery selectors
 * @param domElementOrJQueryResult
 */
export default function(domElementOrJQueryResult) {
  if (domElementOrJQueryResult instanceof $) {
    $.each(domElementOrJQueryResult, function(index, item) {
      videoAsMediaElement(item);
    });
  } else {
    videoAsMediaElement(domElementOrJQueryResult);
  }
}

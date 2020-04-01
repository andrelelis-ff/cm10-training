/*! Shoppable Video Feature | Copyright (c) CoreMedia AG */
import $ from "jquery";
import { addNodeDecorator } from "@coremedia/brick-node-decoration-service";
import * as utils from "@coremedia/brick-utils";
import * as mediaelement from "@coremedia/brick-mediaelement";
import {
  EVENT_VIDEO_START,
  EVENT_VIDEO_ENDED,
} from "@coremedia/brick-mediaelement";

/**
 *  CoreMedia Blueprint Javascript Framework Extension for Shoppable Video
 */
const $document = $(document);

// handle video in teasers for shoppable video
addNodeDecorator(function($target) {
  const baseConfig = {
    preview: undefined,
    play: undefined,
    player: undefined,
    caption: undefined,
    backlightTimeout: 200,
    features: ["backlight"],
  };
  const identifier = "cm-teasable--video";
  const selector = "[data-" + identifier + "]";
  utils.findAndSelf($target, selector).each(function() {
    const $videoTeaser = $(this);
    const config = $.extend(baseConfig, $videoTeaser.data(identifier));
    const $preview = $videoTeaser.find(config.preview);
    const $play = $videoTeaser.find(config.play);
    const $player = $videoTeaser.find(config.player);
    const $caption = $videoTeaser.find(config.caption);

    function replacePlayerWithStillImage() {
      $player.css("display", "none");
      $play.css("display", "");
      $preview.css("display", "");
      $caption.css("display", "none");
      // window might have changed while video player was active, e.g. portrait->landscape
      $(document).trigger(utils.EVENT_LAYOUT_CHANGED);
    }

    // initialize everything
    replacePlayerWithStillImage();

    // all teaser with videos show an image and change on click
    // hiding the image and showing the video. when video has finished
    // show image again
    $play.bind("click", function() {
      $caption.css("display", "");
      $preview.css("display", "none");
      $play.css("display", "none");
      $player.css("display", "");
      const selector = "[data-cm-video]";
      utils.findAndSelf($player, selector).each(function() {
        const $video = $(this);

        // start video
        $video.trigger(EVENT_VIDEO_START);
        // show image again when video ended
        $video.on(EVENT_VIDEO_ENDED, replacePlayerWithStillImage);
      });

      return false;
    });

    // when fullscreen playback is canceled on mobiles, videoEnded is not triggered
    $videoTeaser.on("webkitendfullscreen", function() {
      $videoTeaser.trigger(EVENT_VIDEO_ENDED);
    });
  });
});

// set teaser for shoppable videos to the same height as of the video
// and change teasers according to the timestamps
addNodeDecorator(function($target) {
  utils.findAndSelf($target, ".cm-shoppable").each(function() {
    const $shoppableVideo = $(this);
    const $defaultTeaser = $shoppableVideo.find(".cm-shoppable__default");
    const $allTeasers = $shoppableVideo.find(".cm-shoppable__teaser");
    const $video = $shoppableVideo.find(".cm-shoppable__video");

    if ($allTeasers.length > 0 && $video.length > 0) {
      // initialization
      utils.log("Video is shoppable!");

      const shoppableVideoTeasers = {};
      $allTeasers.each(function() {
        const $teaser = $(this);
        const time = parseInt($teaser.attr("data-cm-video-shoppable-time"));
        if (!isNaN(time)) {
          shoppableVideoTeasers[time] = $teaser;
        }
      });

      $video.on(mediaelement.EVENT_VIDEO_ENDED, function() {
        $allTeasers.hide();
        if ($defaultTeaser.length > 0) {
          $defaultTeaser.show();
        }
      });

      let $lastTeaser = $defaultTeaser || undefined;
      $video.on(mediaelement.EVENT_VIDEO_TIME_UPDATED, function(e, data) {
        const timestamp = data.position;
        const $teaser = shoppableVideoTeasers[timestamp];
        if ($teaser) {
          if ($lastTeaser !== $teaser) {
            utils.log(
              "Change Teaser for shoppable Video at timestamp " +
                timestamp +
                "ms."
            );
            $allTeasers.hide();
            $teaser.show();
            $lastTeaser = $teaser;
            $document.trigger(utils.EVENT_LAYOUT_CHANGED);
          }
        }
      });
    }
  });
});

import $ from "jquery";
import magnificPopup from "@coremedia/brick-magnific-popup";
import { log, error } from "@coremedia/brick-utils";
import { videoAsMediaElement } from "@coremedia/brick-mediaelement";

export default function(
  $popupElements,
  { url, parentSelector, autoplay, hideControls, muted, loop }
) {
  const playerSettings = {
    autoplay: autoplay ? "autoplay" : "",
    hideControls: hideControls ? "" : "controls",
    muted: muted ? "muted" : "",
    loop: loop ? "loop" : "",
  };

  $popupElements.each((index, popupElement) => {
    const $popupElement = $(popupElement);
    log("Video popup found.", $popupElement);

    let $links = $popupElement;
    // if a parentSelector is specified include more links for replacement
    if (parentSelector) {
      const linkToReplace = $popupElement.attr("href");
      // search for all affected links in the given parent
      // links need to point to the same href as the initiator...
      $links = $popupElement
        .closest(parentSelector)
        .find("a")
        .filter((index, element) => $(element).attr("href") === linkToReplace);
    }
    magnificPopup($links, {
      type: "inline",
      midClick: true,

      callbacks: {
        elementParse: function(item) {
          //generate popup on the fly (for lazy loading)
          item.src = `<div class="cm-popup--video">
          <video data-cm-video class="cm-popup__video cm-video" src="${url}" ${playerSettings.autoplay} ${playerSettings.hideControls} ${playerSettings.muted} ${playerSettings.loop}></video>
          </div>`;
        },
        open: function() {
          log("Video popup opened.");
          // find video inside popup
          const $video = $(".mfp-content [data-cm-video]");
          // and initialize mediaElement
          if ($video.length > 0) {
            videoAsMediaElement($video);
          } else {
            error("Error: No video found in popup");
          }
        },
        close: function() {
          log("Video popup closed.");
        },
      },
    });
    $popupElement.removeClass("cm-button--popup-loading");
  });
}

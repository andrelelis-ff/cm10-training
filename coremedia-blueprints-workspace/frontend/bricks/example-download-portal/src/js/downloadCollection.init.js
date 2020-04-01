import $ from "jquery";
import * as downloadCollection from "./downloadCollection";
import * as nodeDecorationService from "@coremedia/brick-node-decoration-service";
import { updateTargetWithAjaxResponse } from "@coremedia/brick-dynamic-include";

$(function() {
  const $window = $(window);

  function updateLeftBadgeIcon($badgeIconLeft, hasRenditions) {
    if (hasRenditions) {
      $badgeIconLeft.addClass("am-icon--rendition-added");
      $badgeIconLeft.removeClass("am-icon--picture-overlay");
    } else {
      $badgeIconLeft.addClass("am-icon--picture-overlay");
      $badgeIconLeft.removeClass("am-icon--rendition-added");
    }
  }

  downloadCollection.initDownloadCollection();

  // show overlay button
  nodeDecorationService.addNodeDecoratorByData(
    {
      assetId: undefined,
    },
    "am-picture-box__badge-icon-left",
    function($badgeIconLeft, config) {
      const hasRenditions = downloadCollection.hasRenditionInDownloadCollection(
        config.assetId
      );
      updateLeftBadgeIcon($badgeIconLeft, hasRenditions);

      $badgeIconLeft.on("click", function(event) {
        event.preventDefault();
        $(this)
          .closest(".am-asset-teaser")
          .find(".am-overlay")
          .show();

        let hasRenditions = downloadCollection.hasRenditionInDownloadCollection(
          config.assetId
        );
        if (!hasRenditions) {
          $(this)
            .closest(".am-asset-teaser")
            .find(".am-overlay__add-to-collection")
            .show();
          $(this)
            .closest(".am-asset-teaser")
            .find(".am-overlay__update-collection")
            .hide();
        } else {
          $(this)
            .closest(".am-asset-teaser")
            .find(".am-overlay__add-to-collection")
            .hide();
          $(this)
            .closest(".am-asset-teaser")
            .find(".am-overlay__update-collection")
            .show();
        }

        $(this)
          .closest(".am-asset-teaser")
          .find(".am-picture-box__badge-icon-right")
          .hide();
        $(this).hide();

        const checkboxes = $(this)
          .closest(".am-asset-teaser")
          .find(".am-overlay--content")
          .find(":checkbox");
        let checkboxSelected = false;
        checkboxes.each(function() {
          const json = this.attributes.getNamedItem("data-am-overlay__checkbox")
            .nodeValue;
          const data = JSON.parse(json);
          if (hasRenditions) {
            this.checked = downloadCollection.isInDownloadCollection(
              config.assetId,
              data.rendition
            );
          } else {
            this.checked = downloadCollection.getDefaultRenditionSelection(
              data.rendition
            );
            if (this.checked) {
              checkboxSelected = this.checked;
            }
          }
        });
        if (!hasRenditions) {
          const $updateButton = $(this)
            .closest(".am-asset-teaser")
            .find(".am-overlay__submit-button");
          if (checkboxSelected) {
            $updateButton.removeAttr("disabled");
          } else {
            $updateButton.attr("disabled", "disabled");
          }
        }
      });
    }
  );

  // overlay close button
  nodeDecorationService.addNodeDecoratorBySelector(
    ".am-overlay__close-button",
    function($closeButton) {
      $closeButton.on("click", function(event) {
        event.preventDefault();
        $(this)
          .closest(".am-asset-teaser")
          .find(".am-overlay")
          .hide();
        $(this)
          .closest(".am-asset-teaser")
          .find(".am-picture-box__badge-icon-left")
          .css("display", "");
        $(this)
          .closest(".am-asset-teaser")
          .find(".am-picture-box__badge-icon-right")
          .show();
      });
    }
  );

  // overlay update button
  nodeDecorationService.addNodeDecoratorByData(
    {
      assetId: undefined,
    },
    "am-overlay__update-button",
    function($addToDownloadCollectionBtn, config) {
      $addToDownloadCollectionBtn.on("click", function(event) {
        event.preventDefault();

        if (!$(this).attr("disabled")) {
          const assetId = config.assetId;
          let hasRenditions = downloadCollection.hasRenditionInDownloadCollection(
            assetId
          );
          if (!hasRenditions) {
            downloadCollection.clearDefaultRenditionSelection();
          }

          const checkboxes = $(this)
            .closest(".am-overlay")
            .find(":checkbox");
          checkboxes.each(function() {
            const json = this.attributes.getNamedItem(
              "data-am-overlay__checkbox"
            ).nodeValue;
            const data = JSON.parse(json);
            if (this.checked) {
              downloadCollection.addRenditionToDownloadCollection(
                assetId,
                data.rendition
              );
              if (!hasRenditions) {
                downloadCollection.addDefaultRenditionSelection(data.rendition);
              }
            } else {
              downloadCollection.removeRenditionFromDownloadCollection(
                assetId,
                data.rendition
              );
              if (!hasRenditions) {
                downloadCollection.removeDefaultRenditionSelection(
                  data.rendition
                );
              }
            }
          });
          $(this)
            .closest(".am-asset-teaser")
            .find(".am-overlay")
            .hide();
          $(this)
            .closest(".am-asset-teaser")
            .find(".am-picture-box__badge-icon-left")
            .css("display", "");
          $(this)
            .closest(".am-asset-teaser")
            .find(".am-picture-box__badge-icon-right")
            .show();

          hasRenditions = downloadCollection.hasRenditionInDownloadCollection(
            assetId
          );
          updateLeftBadgeIcon(
            $(this)
              .closest(".am-asset-teaser")
              .find(".am-picture-box__badge-icon-left"),
            hasRenditions
          );
        }
      });
    }
  );

  // overlay checkboxes
  nodeDecorationService.addNodeDecoratorByData(
    {
      assetId: undefined,
    },
    "am-overlay__checkbox",
    function($checkbox, config) {
      $checkbox.on("click", function() {
        let hasRenditions = downloadCollection.hasRenditionInDownloadCollection(
          config.assetId
        );
        if (!hasRenditions) {
          const checkboxes = $(this)
            .closest(".am-overlay__checkboxes")
            .find(":checkbox");
          let buttonEnabled = false;
          checkboxes.each(function() {
            if (this.checked) {
              buttonEnabled = true;
            }
          });

          const submitButton = $(this)
            .closest(".am-asset-teaser")
            .find(".am-overlay__submit-button");
          if (buttonEnabled) {
            submitButton.removeAttr("disabled");
          } else {
            submitButton.attr("disabled", "disabled");
          }
        }
      });
    }
  );

  nodeDecorationService.addNodeDecoratorByData(
    {
      assetId: undefined,
      rendition: undefined,
    },
    "am-download-collection-rendition-control",
    function($renditionControl, config, state) {
      $.extend(state, {
        windowListener: function() {
          downloadCollection.updateRenditionLinkTextState(
            $renditionControl,
            config
          );
        },
      });

      $window.on(downloadCollection.EVENT_UPDATED, state.windowListener);

      // init
      downloadCollection.updateRenditionLinkTextState(
        $renditionControl,
        config
      );

      $renditionControl.on("click", function() {
        downloadCollection.addOrRemoveRenditionFromDownloadCollection(
          config.assetId,
          config.rendition
        );
        downloadCollection.updateDownloadCollectionButtonState(
          undefined,
          undefined
        ); // TODO: get button and counter
      });
    },
    function($renditionControl, config, state) {
      state.windowListener &&
        $window.off(downloadCollection.EVENT_UPDATED, state.windowListener);
    }
  );

  nodeDecorationService.addNodeDecoratorByData(
    {},
    "am-download-collection-counter",
    function($counter, config, state) {
      $.extend(state, {
        windowListener: function() {
          downloadCollection.updateDownloadCollectionCounterState($counter);
        },
      });
      $window.on(downloadCollection.EVENT_UPDATED, state.windowListener);
      // init
      downloadCollection.updateDownloadCollectionCounterState($counter);
    },
    function($counter, config, state) {
      state.windowListener &&
        $window.off(downloadCollection.EVENT_UPDATED, state.windowListener);
    }
  );

  nodeDecorationService.addNodeDecoratorByData(
    {
      assetId: undefined,
      rendition: undefined,
    },
    "am-rendition-collection-item",
    function($collectionItem, config, state) {
      $.extend(state, {
        windowListener: function() {
          if (
            !downloadCollection.isInDownloadCollection(
              config.assetId,
              config.rendition
            )
          ) {
            $collectionItem.fadeOut(800, function() {
              if ($collectionItem) {
                $collectionItem.remove();
              }
            });
          }
        },
      });
      $window.on(downloadCollection.EVENT_UPDATED, state.windowListener);
    },
    function($collectionItem, config, state) {
      state.windowListener &&
        $window.off(downloadCollection.EVENT_UPDATED, state.windowListener);
    }
  );

  nodeDecorationService.addNodeDecoratorByData(
    {
      prepareUrl: undefined,
      downloadUrl: undefined,
    },
    "am-download-collection-trigger",
    function($downloadCollection, config) {
      const $button = $downloadCollection.find(
        ".am-download-collection__button"
      );
      const $counter = $downloadCollection.find(
        ".am-download-collection__counter"
      );
      const $buttons = $button.add($counter);

      downloadCollection.updateDownloadCollectionButtonState($button, $counter);

      $buttons.on("click", function(event) {
        event.preventDefault();

        const downloadCollectionString = JSON.stringify(
          downloadCollection.getDownloadCollection()
        );

        $downloadCollection.addClass("am-download-collection--loading");

        $buttons.prop("disabled", true);

        $.ajax({
          method: "POST",
          url: config.prepareUrl,
          data: { "download-collection-data": downloadCollectionString },
        })
          .done(function() {
            $downloadCollection.removeClass("am-download-collection--loading");

            const downloadUrl = config.downloadUrl;

            const $form = $("<form></form>");
            const $input = $("<input>");
            const downloadCollectionDataString = JSON.stringify(
              downloadCollection.getDownloadCollection()
            );
            $input.attr("type", "hidden");
            $input.attr("name", "download-collection-data");
            $input.val(downloadCollectionDataString);
            $form.append($input);
            $downloadCollection.append($form);
            $form.attr("action", downloadUrl);
            $form.attr("method", "POST");
            $form.submit();
            $form.remove();

            downloadCollection.clearDownloadCollection();
            downloadCollection.updateDownloadCollectionButtonState(
              $button,
              $counter
            );
          })
          .fail(function(response) {
            console.error(
              "Failed to prepare download: ",
              response.responseText
            );
          });
        return false;
      });
    }
  );

  nodeDecorationService.addNodeDecoratorByData(
    {
      url: undefined,
    },
    "am-download-collection-overview",
    function($collectionOverview, config, state) {
      function refresh() {
        const requestParams = {
          "download-collection-data": JSON.stringify(
            downloadCollection.getDownloadCollection()
          ),
        };
        const requestConfig = {
          url: config.url,
          params: requestParams,
          method: "POST",
        };
        updateTargetWithAjaxResponse(
          $collectionOverview,
          requestConfig,
          false,
          undefined
        );
      }
      $.extend(state, {
        windowListener: function() {
          if (downloadCollection.getDownloadCollectionCount() === 0) {
            refresh();
          }
        },
      });
      $window.on(downloadCollection.EVENT_UPDATED, state.windowListener);
      refresh();
    },
    function($collectionOverview, config, state) {
      state.windowListener &&
        $window.off(downloadCollection.EVENT_UPDATED, state.windowListener);
    }
  );
});

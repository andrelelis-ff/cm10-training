import $ from "jquery";
import {
  addBEMModifier,
  findBEMElement,
  removeBEMModifier,
} from "@coremedia/brick-utils";

/**
 * Asset Management Download Collection functionality
 *
 */
const $window = $(window);

const DOWNLOAD_COLLECTION_MAX_QUANTITY = 999;

const BLOCK_NAME_DLC = "am-download-collection";

const ELEMENT_NAME_DLC_BUTTON = "button";
const ELEMENT_NAME_DLC_COUNTER = "counter";

const BLOCK_NAME_DLC_RENDITION_CONTROL =
  "am-download-collection-rendition-control";

const MODIFIER_NAME_DLC_RENDITION_CONTROL_ADDABLE = "addable";
const MODIFIER_NAME_DLC_RENDITION_CONTROL_REMOVABLE = "removable";

export const DOWNLOAD_COLLECTION_PROPERTY = "downloadCollection";

export const RENDITION_SELECTION_PROPERTY = "renditionSelection";

const EVENT_PREFIX = "coremedia.blueprint.am.downloadCollection.";
export const EVENT_UPDATED = EVENT_PREFIX + "updated";

// PRIVATE

/**
 * Get all renditions for given download collection with given asset id.
 * @param {Array} downloadCollection
 * @param {Number} assetId
 * @returns {Array}
 */
function getRenditionsToDownloadForAsset(downloadCollection, assetId) {
  if (!downloadCollection[assetId]) {
    downloadCollection[assetId] = [];
  }
  return downloadCollection[assetId];
}

// PUBLIC

/**
 * Checks whether the given <b>rendition</b> is set under key <b>assetId</b>.
 * @param {Number} assetId Asset Id
 * @param {String} rendition Name of rendition
 * @returns {boolean}
 */
export function isInDownloadCollection(assetId, rendition) {
  const renditionsToDownloadForAsset = getRenditionsToDownloadForAsset(
    getDownloadCollection(),
    assetId
  );
  return renditionsToDownloadForAsset.indexOf(rendition) >= 0;
}

/**
 * Checks whether an asset with the given <b>assetId</b> has renditions in download collection.
 * @param {Number} assetId Asset Id
 * @returns {boolean}
 */
export function hasRenditionInDownloadCollection(assetId) {
  const renditionsToDownloadForAsset = getRenditionsToDownloadForAsset(
    getDownloadCollection(),
    assetId
  );
  return (
    renditionsToDownloadForAsset && renditionsToDownloadForAsset.length > 0
  );
}

/**
 * Counts all renditions for all asset id in download collection.
 * @returns {number}
 */
export function getDownloadCollectionCount() {
  const downloadCollection = getDownloadCollection();

  let count = 0;

  for (const assetId in downloadCollection) {
    if (downloadCollection.hasOwnProperty(assetId)) {
      count = count + downloadCollection[assetId].length;
    }
  }

  return count;
}

/**
 * Initializes the Asset Download Collection in {@link localStorage} under key {@link DOWNLOAD_COLLECTION_PROPERTY}
 */
export function initDownloadCollection() {
  if (!localStorage.getItem(DOWNLOAD_COLLECTION_PROPERTY)) {
    localStorage.setItem(DOWNLOAD_COLLECTION_PROPERTY, "{}");
  }
  $window.trigger(EVENT_UPDATED);
}

/**
 * Re-initializes download collection
 */
export function clearDownloadCollection() {
  localStorage.removeItem(DOWNLOAD_COLLECTION_PROPERTY);
  initDownloadCollection();
  // init already triggers UPDATED event
}

/**
 * Retrieves a copy of the collection of renditions selected for download.
 * The collection has asset IDs as keys and an array of the selected
 * renditions as value.
 *
 * @returns {Array} the rendition download collection
 */
export function getDownloadCollection() {
  return JSON.parse(localStorage.getItem(DOWNLOAD_COLLECTION_PROPERTY));
}

export function saveDownloadCollection(downloadCollection) {
  const downloadCollectionString = JSON.stringify(downloadCollection);
  localStorage.setItem(DOWNLOAD_COLLECTION_PROPERTY, downloadCollectionString);
  $window.trigger(EVENT_UPDATED);
}

export function getRenditionSelection() {
  return JSON.parse(localStorage.getItem(RENDITION_SELECTION_PROPERTY)) || [];
}

export function updateDownloadCollectionCounterState($counter) {
  const count = getDownloadCollectionCount();
  $counter.text(count);
}

export function saveRenditionSelection(renditionSelection) {
  const renditionSelectionString = JSON.stringify(renditionSelection);
  localStorage.setItem(RENDITION_SELECTION_PROPERTY, renditionSelectionString);
}

export function updateRenditionLinkTextState($control, config) {
  if (isInDownloadCollection(config.assetId, config.rendition)) {
    removeBEMModifier(
      $control,
      BLOCK_NAME_DLC_RENDITION_CONTROL,
      MODIFIER_NAME_DLC_RENDITION_CONTROL_ADDABLE
    );
    addBEMModifier(
      $control,
      BLOCK_NAME_DLC_RENDITION_CONTROL,
      MODIFIER_NAME_DLC_RENDITION_CONTROL_REMOVABLE
    );
  } else {
    removeBEMModifier(
      $control,
      BLOCK_NAME_DLC_RENDITION_CONTROL,
      MODIFIER_NAME_DLC_RENDITION_CONTROL_REMOVABLE
    );
    addBEMModifier(
      $control,
      BLOCK_NAME_DLC_RENDITION_CONTROL,
      MODIFIER_NAME_DLC_RENDITION_CONTROL_ADDABLE
    );
  }
}

/**
 * Checks whether the Download Collection Button is disabled (no contents in local storage) or not (there is at least one rendition in local storage)
 */
export function updateDownloadCollectionButtonState($button, $counter) {
  const $collection = $("." + BLOCK_NAME_DLC);
  $button =
    $button ||
    findBEMElement($collection, BLOCK_NAME_DLC, ELEMENT_NAME_DLC_BUTTON);
  $counter =
    $counter ||
    findBEMElement($collection, BLOCK_NAME_DLC, ELEMENT_NAME_DLC_COUNTER);

  const $buttons = $button.add($counter);

  const downloadCollection = getDownloadCollection();
  const disabled =
    !downloadCollection ||
    downloadCollection.length === 0 ||
    $.isEmptyObject(downloadCollection);
  $buttons.prop("disabled", disabled);
}

/**
 * Adds a rendition name to the download collection's associated asset id array or removes it, if found.
 *
 * @param {Number} assetId
 * @param {String} renditionName
 */
export function addOrRemoveRenditionFromDownloadCollection(
  assetId,
  renditionName
) {
  const downloadCollection = getDownloadCollection();

  const renditionsToDownloadForAsset = getRenditionsToDownloadForAsset(
    downloadCollection,
    assetId
  );

  const indexOfRendition = renditionsToDownloadForAsset.indexOf(renditionName);
  if (indexOfRendition === -1) {
    if (getDownloadCollectionCount() < DOWNLOAD_COLLECTION_MAX_QUANTITY) {
      renditionsToDownloadForAsset.push(renditionName);
    } else {
      console.error(
        "Maximum number of items in Asset Download Collection reached",
        DOWNLOAD_COLLECTION_MAX_QUANTITY
      );
    }
  } else {
    renditionsToDownloadForAsset.splice(indexOfRendition, 1);
    if (renditionsToDownloadForAsset.length === 0) {
      delete downloadCollection[assetId];
    }
  }

  saveDownloadCollection(downloadCollection);
}

export function addRenditionToDownloadCollection(assetId, renditionName) {
  const downloadCollection = getDownloadCollection();

  const renditionsToDownloadForAsset = getRenditionsToDownloadForAsset(
    downloadCollection,
    assetId
  );

  const indexOfRendition = renditionsToDownloadForAsset.indexOf(renditionName);
  if (indexOfRendition === -1) {
    if (getDownloadCollectionCount() < DOWNLOAD_COLLECTION_MAX_QUANTITY) {
      renditionsToDownloadForAsset.push(renditionName);
    } else {
      console.error(
        "Maximum number of items in Asset Download Collection reached",
        DOWNLOAD_COLLECTION_MAX_QUANTITY
      );
    }
  }

  saveDownloadCollection(downloadCollection);
}

export function removeRenditionFromDownloadCollection(assetId, renditionName) {
  const downloadCollection = getDownloadCollection();

  const renditionsToDownloadForAsset = getRenditionsToDownloadForAsset(
    downloadCollection,
    assetId
  );

  const indexOfRendition = renditionsToDownloadForAsset.indexOf(renditionName);
  if (indexOfRendition > -1) {
    renditionsToDownloadForAsset.splice(indexOfRendition, 1);
    if (renditionsToDownloadForAsset.length === 0) {
      delete downloadCollection[assetId];
    }
  }

  saveDownloadCollection(downloadCollection);
}

export function clearDefaultRenditionSelection() {
  saveRenditionSelection([]);
}

export function addDefaultRenditionSelection(rendition) {
  const renditionSelection = getRenditionSelection();
  if (renditionSelection.indexOf(rendition) < 0) {
    renditionSelection.push(rendition);
  }
  saveRenditionSelection(renditionSelection);
}

export function removeDefaultRenditionSelection(rendition) {
  const renditionSelection = getRenditionSelection();
  const pos = renditionSelection.indexOf(rendition);
  if (pos >= 0) {
    renditionSelection.splice(pos, 1);
  }
  saveRenditionSelection(renditionSelection);
}

export function getDefaultRenditionSelection(rendition) {
  const renditionSelection = getRenditionSelection();
  return renditionSelection.indexOf(rendition) >= 0;
}

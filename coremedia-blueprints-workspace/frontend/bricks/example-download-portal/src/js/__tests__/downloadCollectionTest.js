import * as downloadCollection from "../downloadCollection";
import LocalStorageMock from "../__mocks__/LocalStorageMock";

global.localStorage = new LocalStorageMock();

describe("Test initialization of Download Collection", function() {
  afterEach(function() {
    localStorage.removeItem(downloadCollection.DOWNLOAD_COLLECTION_PROPERTY);
  });

  it("There should be an empty download collection", function() {
    expect(downloadCollection.getDownloadCollection()).toBeNull();

    downloadCollection.initDownloadCollection();

    expect(downloadCollection.getDownloadCollection()).toBeDefined();
  });
});

describe("Test count of Download Collection", function() {
  beforeEach(function() {
    downloadCollection.initDownloadCollection();
  });

  afterEach(function() {
    localStorage.removeItem(downloadCollection.DOWNLOAD_COLLECTION_PROPERTY);
  });

  it("The download collection size should be 0", function() {
    expect(downloadCollection.getDownloadCollectionCount()).toEqual(0);
  });

  it("The download collection size should be 3", function() {
    const collection = { 1: ["web", "original"], 2: ["web"] };
    localStorage.setItem(
      downloadCollection.DOWNLOAD_COLLECTION_PROPERTY,
      JSON.stringify(collection)
    );
    expect(downloadCollection.getDownloadCollectionCount()).toEqual(3);
  });
});

describe("Test rendition name is in download collection", function() {
  beforeEach(function() {
    downloadCollection.initDownloadCollection();
    const collection = { 1: ["web", "original"], 2: ["web"] };
    localStorage.setItem(
      downloadCollection.DOWNLOAD_COLLECTION_PROPERTY,
      JSON.stringify(collection)
    );
  });

  afterEach(function() {
    localStorage.removeItem(downloadCollection.DOWNLOAD_COLLECTION_PROPERTY);
  });

  it("The download collection (does not) contains rendition", function() {
    expect(downloadCollection.isInDownloadCollection(2, "web")).toBeTruthy();
    expect(
      downloadCollection.isInDownloadCollection(2, "original")
    ).toBeFalsy();
  });
});

describe("Test get/save/add/remove from/to download collection", function() {
  beforeEach(function() {
    downloadCollection.initDownloadCollection();
    const collection = { 1: ["web", "original"], 2: ["web"] };
    localStorage.setItem(
      downloadCollection.DOWNLOAD_COLLECTION_PROPERTY,
      JSON.stringify(collection)
    );
  });

  afterEach(function() {
    localStorage.removeItem(downloadCollection.DOWNLOAD_COLLECTION_PROPERTY);
  });

  it("The download collection returns its contents", function() {
    expect(downloadCollection.getDownloadCollection()).toEqual({
      1: ["web", "original"],
      2: ["web"],
    });
  });

  it("After clear the download collection should be an empty object", function() {
    downloadCollection.clearDownloadCollection();
    expect(downloadCollection.getDownloadCollection()).toEqual({});
  });

  it("Save should result in an updated download collection ", function() {
    downloadCollection.saveDownloadCollection({
      3: ["web"],
      4: ["web", "original"],
    });
    expect(downloadCollection.getDownloadCollection()).toEqual({
      3: ["web"],
      4: ["web", "original"],
    });
  });
});

describe("Test initialization of default rendition selection", function() {
  beforeEach(function() {
    downloadCollection.clearDefaultRenditionSelection();
  });

  afterEach(function() {
    localStorage.removeItem(downloadCollection.RENDITION_SELECTION_PROPERTY);
  });

  it("There should be an empty default rendition selection", function() {
    expect(downloadCollection.getRenditionSelection()).toEqual([]);
  });

  it("Update default rendition selection", function() {
    downloadCollection.addDefaultRenditionSelection("web");

    expect(downloadCollection.getRenditionSelection()).toEqual(["web"]);

    downloadCollection.addDefaultRenditionSelection("original");

    expect(downloadCollection.getRenditionSelection()).toEqual([
      "web",
      "original",
    ]);

    downloadCollection.addDefaultRenditionSelection("web");

    expect(downloadCollection.getRenditionSelection()).toEqual([
      "web",
      "original",
    ]);
  });

  it("Clear default rendition selection", function() {
    downloadCollection.addDefaultRenditionSelection("web");

    expect(downloadCollection.getRenditionSelection()).toEqual(["web"]);

    downloadCollection.clearDefaultRenditionSelection();

    expect(downloadCollection.getRenditionSelection()).toEqual([]);
  });
});

describe("Add and remove renditions to/from download collection", function() {
  beforeEach(function() {
    downloadCollection.initDownloadCollection();
  });

  afterEach(function() {
    localStorage.removeItem(downloadCollection.DOWNLOAD_COLLECTION_PROPERTY);
  });

  it("Add/remove rendition to download collection", function() {
    downloadCollection.addRenditionToDownloadCollection(1, "web");
    expect(downloadCollection.isInDownloadCollection(1, "web")).toBeTruthy();
    expect(
      downloadCollection.isInDownloadCollection(2, "original")
    ).toBeFalsy();

    downloadCollection.removeRenditionFromDownloadCollection(1, "web");
    expect(downloadCollection.isInDownloadCollection(1, "web")).toBeFalsy();
    expect(
      downloadCollection.isInDownloadCollection(2, "original")
    ).toBeFalsy();
  });
});

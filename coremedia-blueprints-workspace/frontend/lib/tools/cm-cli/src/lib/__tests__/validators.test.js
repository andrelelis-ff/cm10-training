"use strict";

const { isValidURL, isValidStringValue } = require("../validators");

describe("isValidURL()", () => {
  it("returns true", () => {
    expect(isValidURL("http://coremedia.com")).toBe(true);
  });
  it("returns true", () => {
    expect(isValidURL("https://coremedia.com")).toBe(true);
  });
  it("returns false", () => {
    expect(isValidURL("htp://coremedia.com")).toEqual(
      "Please enter a valid URL."
    );
  });
  it("returns false", () => {
    expect(isValidURL("//coremedia.com")).toEqual("Please enter a valid URL.");
  });
  it("returns false", () => {
    expect(isValidURL("coremedia.com")).toEqual("Please enter a valid URL.");
  });
});

describe("isValidStringValue()", () => {
  it("returns true", () => {
    expect(isValidStringValue("test")).toBe(true);
  });
  it("returns false", () => {
    expect(isValidStringValue("")).toEqual("Please enter a string value.");
  });
  it("returns false", () => {
    expect(isValidStringValue(1)).toEqual("Please enter a string value.");
  });
  it("returns false", () => {
    expect(isValidStringValue([])).toEqual("Please enter a string value.");
  });
  it("returns false", () => {
    expect(isValidStringValue({})).toEqual("Please enter a string value.");
  });
  it("returns false", () => {
    expect(isValidStringValue(undefined)).toEqual(
      "Please enter a string value."
    );
  });
});

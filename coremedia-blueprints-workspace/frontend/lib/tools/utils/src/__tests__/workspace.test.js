const path = require("path");
const cmLogger = require("@coremedia/cm-logger");
const { PKG_NAME, parseSettings, mergeSettings } = require("../workspace");

const asLink = link => ({ $Link: link });
const asLinks = links => links.map(asLink);

const asDate = date => ({ $Date: date });
const asDates = dates => dates.map(asDate);

describe("parseSettings()", () => {
  const testSettingsPath = path.join(__dirname, "test-settings.json");
  it("parses empty settings", () => {
    expect(parseSettings(testSettingsPath, JSON.stringify({}))).toEqual({});
  });

  it("parses settings with various valid simple properties", () => {
    const json = {
      string: "hello",
      integer: 1,
      boolean: true,
      date: asDate("2019-01-01"),
      struct: "",
      stringlist: ["a", "b"],
      integerlist: [1, 2, 3],
      booleanlist: [true, false, true],
      datelist: asDates(["2018-09-01", "2019-01-01"]),
    };

    expect(parseSettings(testSettingsPath, JSON.stringify(json))).toEqual(json);
  });

  it("will not parse invalid json", () => {
    const log = cmLogger.getLogger({
      name: PKG_NAME,
    });
    // prevent that warnings are logged
    log.warn = jest.fn(() => {});

    expect(parseSettings(testSettingsPath, "abc")).toEqual({});

    expect(log.warn.mock.calls.length).toEqual(1);
  });

  it("will not parse invalid settings", () => {
    const log = cmLogger.getLogger({
      name: PKG_NAME,
    });
    // noop on "warn"
    log.warn = jest.fn(() => {});

    expect(
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          emptyArray: [],
        })
      )
    ).toEqual({});

    expect(
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          mixedArray: ["a", 1],
        })
      )
    ).toEqual({});

    expect(
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          invalidDate: asDate("some-date"),
        })
      )
    ).toEqual({});

    expect(
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          invalidDate: asDate("12:12"),
        })
      )
    ).toEqual({});

    expect(
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          invalidDate: asDate("2015-14-33"),
        })
      )
    ).toEqual({});

    expect(
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          invalidDates: asDates(["some-date", "some-other-date"]),
        })
      )
    ).toEqual({});

    // leave space for extensions by reserving ":" in property names
    // could for example be used to implement array append ("somearray:append")
    expect(
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          "my:property": "a",
        })
      )
    ).toEqual({});

    expect(log.warn.mock.calls.length).toEqual(7);
  });

  it("throws an error if links cannot be resolved", () => {
    expect(() => {
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          invalidLink: asLink("some-link"),
        })
      );
    }).toThrow();

    expect(() => {
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          invalidLinkArray: asLinks(["some-link", "some-other-link"]),
        })
      );
    }).toThrow();
  });

  it("parses and transforms links correctly", () => {
    // needs to be an existing file, assuming __filename always exists
    const relativeFilename = path.basename(__filename);
    const relativeLink = asLink(relativeFilename);
    const relativeLinkArray = asLinks([relativeFilename, relativeFilename]);
    const absoluteLink = asLink(__filename);
    const absoluteLinkArray = asLinks([__filename, __filename]);

    expect(
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          link: relativeLink,
          linkArray: relativeLinkArray,
          foo: {
            link: relativeLink,
            linkArray: relativeLinkArray,
          },
        })
      )
    )
      // do not use toMatchSnapshot as the absolute path is different on every system
      .toEqual({
        link: absoluteLink,
        linkArray: absoluteLinkArray,
        foo: {
          link: absoluteLink,
          linkArray: absoluteLinkArray,
        },
      });
  });

  it("parses ISO 8601 date formats", () => {
    expect(
      parseSettings(
        testSettingsPath,
        JSON.stringify({
          date: "2018-11-13",
          dateTime: "2018-11-13 20:20:39",
          dateWithTimeZone: "2018-11-13+03:00",
          dateTimeWithTimeZone: "2018-11-13 20:20:39-09:00",
        })
      )
    )
    .toEqual({
      date: "2018-11-13",
      dateTime: "2018-11-13 20:20:39",
      dateWithTimeZone: "2018-11-13+03:00",
      dateTimeWithTimeZone: "2018-11-13 20:20:39-09:00",
    })
  });
});

describe("mergeSettings()", () => {
  it("merges empty settings", () => {
    expect(mergeSettings([{}, {}])).toEqual({});
  });

  it("merges settings of same type", () => {
    expect(mergeSettings([{ a: 1 }, { b: 2 }])).toEqual({ a: 1, b: 2 });
  });

  it("merges settings of different type", () => {
    expect(mergeSettings([{ a: true }, { b: "b" }])).toEqual({
      a: true,
      b: "b",
    });
  });

  it("overrides settings of same type", () => {
    expect(mergeSettings([{ a: 1 }, { a: 2 }])).toEqual({ a: 2 });
  });

  it("overides settings of different type", () => {
    expect(mergeSettings([{ a: true }, { a: "b" }])).toEqual({ a: "b" });
  });

  it("overides array instead of merging them", () => {
    expect(mergeSettings([{ a: ["a"] }, { a: ["b"] }])).toEqual({ a: ["b"] });
  });

  it("merges objects", () => {
    expect(mergeSettings([{ foo: { a: 1 } }, { foo: { b: 2 } }])).toEqual({
      foo: { a: 1, b: 2 },
    });
  });

  it("merges nested objects", () => {
    expect(
      mergeSettings([{ foo: { bar: { a: 1 } } }, { foo: { bar: { b: 2 } } }])
    ).toEqual({
      foo: { bar: { a: 1, b: 2 } },
    });
  });
});

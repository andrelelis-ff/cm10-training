import $ from "jquery";
import { renderFragmentHrefs } from "../fragment";

/**
 * renderFragmentHrefs Test
 */
describe("fragment hrefs", function() {
  beforeEach(function() {
    document.body.innerHTML = `<a href="" data-href="$nextUrl$">ABC</a>
<a href="" data-href="$nextUrl $nextUrl$ nextUrl nextUrl$">ABC</a>
<a href="" data-href="$nextUrl $nextUrl$ nextUrl nextUrl$">ABC</a>
<a href="" data-href="$nextUrl $nextUrl$ nextUrl nextUrl$">ABC</a>`;
    renderFragmentHrefs($(document.body));
  });

  it("should have replaced all occurences of $nextUrl$", function() {
    $("a[data-href]").each(function() {
      const $this = $(this);
      expect($this.attr("href").search(/\$nextUrl\$/g)).toBe(-1);
    });
  });
});

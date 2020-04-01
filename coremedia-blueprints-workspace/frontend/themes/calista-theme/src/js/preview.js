/*! Theme calista: Preview JS */
window.coremedia.blueprint.$(function() {
  function replaceLoginPreviewURL() {
    const $loginBtn = window.coremedia.blueprint.$("#cm-login");
    if ($loginBtn.length > 0) {
      $loginBtn.attr(
        "href",
        $loginBtn.attr("href").replace("newPreviewSession%3Dtrue%26", "")
      );
    }
  }

  /* remove newPreviewSession parameter from next URL in preview to avoid login problems */
  replaceLoginPreviewURL();
  window.coremedia.blueprint
    .$(document)
    .on("coremedia.blueprint.base.loginStatusChecked", replaceLoginPreviewURL);
});

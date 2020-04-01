import $ from "jquery";
import { addNodeDecoratorBySelector } from "@coremedia/brick-node-decoration-service";

addNodeDecoratorBySelector(".cm-hamburger-icon", $hamburgerIcon => {
  const $multilevelDropdown = $(".multilevel-dropdown");
  const $modalBackground = $(".modal-background");

  $hamburgerIcon.on("click touch", () => {
    const isOpenMenu = $multilevelDropdown.hasClass("in");

    if (isOpenMenu) {
      $multilevelDropdown.removeClass("in");
      $modalBackground.css("display", "none");
    }
  });
  // activate button as soon as functionality is applied
  $hamburgerIcon.removeAttr("disabled");
});

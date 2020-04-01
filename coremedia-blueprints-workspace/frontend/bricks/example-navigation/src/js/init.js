import $ from "jquery";

import { addNodeDecoratorBySelector } from "@coremedia/brick-node-decoration-service";
import { getLastDevice, isTouchDevice } from "@coremedia/brick-device-detector";

const BLOCK = "cm-navigation";
const MODIFIER_HOVERED = BLOCK + "--hovered";

const ITEM_BLOCK = "cm-navigation-item";
const ITEM_ELEMENT_TITLE = `${ITEM_BLOCK}__title`;
const ITEM_ELEMENT_TOGGLE = `${ITEM_BLOCK}__toggle`;
const ITEM_ELEMENT_MENU = `${ITEM_BLOCK}__menu`;
const ITEM_ELEMENT_MENU_VISIBILITY_MODIFIER = `${ITEM_ELEMENT_MENU}--hidden`;
const ITEM_ELEMENT_MENU_MODIFIER = `${ITEM_BLOCK}--over`;
const ITEM_ELEMENT_MENU_ACTIVE_MODIFIER = `${ITEM_BLOCK}-menu--active`;
const ITEM_MODIFIER_DEPTH_1 = `${ITEM_BLOCK}--depth-1`;
const ITEM_MODIFIER_DEPTH_1_BORDER_MODIFIER = `${ITEM_BLOCK}--no-border-bottom`;
const ITEM_MODIFIER_OPEN = `${ITEM_BLOCK}--open`;

function isMobileOrTablet() {
  return getLastDevice().type !== "desktop";
}

addNodeDecoratorBySelector(`.${BLOCK}`, $navigationRoot => {
  const $navigationRootList = $navigationRoot.find(`.${ITEM_ELEMENT_MENU}`);
  let $navigationEntries = $navigationRootList.find(
    `.${ITEM_MODIFIER_DEPTH_1}`
  );

  // Previously hovered menus could still be visible since they won't disappear until the end of their transition.
  // To make sure that only one menu is visible, we need to set the opacity of all other menus to 0.
  $navigationEntries.mouseover(function() {
    const $currentNavigationEntry = $(this);
    const $menuVisible = $currentNavigationEntry.find(
      `ul.${ITEM_ELEMENT_MENU}`
    );

    $navigationRootList.addClass(MODIFIER_HOVERED);
    $navigationEntries.not(this).each(function() {
      const $this = $(this);

      $this.removeClass(
        ITEM_ELEMENT_MENU_MODIFIER + " " + ITEM_ELEMENT_MENU_ACTIVE_MODIFIER
      );
      $this.addClass(ITEM_MODIFIER_DEPTH_1_BORDER_MODIFIER);
      $this
        .find(`ul.${ITEM_ELEMENT_MENU}`)
        .addClass(ITEM_ELEMENT_MENU_VISIBILITY_MODIFIER);
    });

    $currentNavigationEntry.removeClass(ITEM_MODIFIER_DEPTH_1_BORDER_MODIFIER);
    $currentNavigationEntry
      .find(`ul.${ITEM_ELEMENT_MENU}`)
      .removeClass(ITEM_ELEMENT_MENU_VISIBILITY_MODIFIER);

    if ($menuVisible.length > 0 && !isMobileOrTablet()) {
      $currentNavigationEntry.addClass(ITEM_ELEMENT_MENU_ACTIVE_MODIFIER);
    }

    if (!isMobileOrTablet()) {
      $currentNavigationEntry.addClass(ITEM_ELEMENT_MENU_MODIFIER);
    }
  });

  $navigationEntries.mouseout(() => {
    $navigationEntries.not(this).each(function() {
      const $this = $(this);
      if (!isMobileOrTablet()) {
        $this.removeClass(
          ITEM_ELEMENT_MENU_MODIFIER + " " + ITEM_ELEMENT_MENU_ACTIVE_MODIFIER
        );
      }
    });

    $navigationRootList.removeClass(MODIFIER_HOVERED);
  });

  $navigationEntries.on("click", function(e) {
    // prevent further code from being executed if a sublist of the list is clicked
    if (e.target.parentNode !== this) return;
    // ignore click on touch devices. we don't want to trigger the link, just display the subnavigation
    if (isTouchDevice() && !isMobileOrTablet()) {
      e.preventDefault();
    }
  });
});

addNodeDecoratorBySelector(
  ".cm-header__mobile-navigation-button.cm-hamburger-icon",
  $hamburgerIcon => {
    const $body = $("body");
    $hamburgerIcon.on("click touch", () => {
      const toBeOpened = !$hamburgerIcon.hasClass("cm-hamburger-icon--toggled");
      if (toBeOpened) {
        $body.addClass("cm-body--navigation-active");
        $hamburgerIcon.addClass("cm-hamburger-icon--toggled");
      } else {
        $hamburgerIcon.removeClass("cm-hamburger-icon--toggled");
        $body.removeClass("cm-body--navigation-active");
      }
    });
    // activate button as soon as functionality is applied
    $hamburgerIcon.removeAttr("disabled");
  }
);

addNodeDecoratorBySelector(".cm-navigation-item", $navigationItem => {
  const $toggle = $navigationItem.find(`> .${ITEM_ELEMENT_TOGGLE}`);
  const $title = $navigationItem.find(`> .${ITEM_ELEMENT_TITLE}`);
  const $menu = $navigationItem.find(`> .${ITEM_ELEMENT_MENU}`);
  if ($menu.length > 0) {
    $toggle.on("click touch", () => {
      const toBeOpened = !$navigationItem.hasClass(ITEM_MODIFIER_OPEN);
      $(`.${ITEM_BLOCK}`).removeClass(ITEM_MODIFIER_OPEN);
      if (toBeOpened) {
        $navigationItem.addClass(ITEM_MODIFIER_OPEN);
      }
    });
    // only make title clickable if not a link
    if (!$title.is("a[href]")) {
      $title.on("click touch", () => {
        const toBeOpened = !$navigationItem.hasClass(ITEM_MODIFIER_OPEN);
        $(`.${ITEM_BLOCK}`).removeClass(ITEM_MODIFIER_OPEN);
        if (toBeOpened) {
          $navigationItem.addClass(ITEM_MODIFIER_OPEN);
        }
      });
    }
    // activate button as soon as functionality is applied
    $toggle.removeAttr("disabled");
  }
});

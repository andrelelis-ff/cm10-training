(fuction () {
  // create coremedia namespace if it doesn't exist
  this.coremedia = window.coremedia || {};

  // create coremedia.blueprint namespace if it doesn't exist
  this.coremedia.blueprint = coremedia.blueprint || {};

  // extend coremedia.blueprint with media object
  this.coremedia.blueprint.media = {
    equalHeights : equalHeights
  };

  // set elements to the same height only once or with reset-flag
  function equalHeights() {
    $(".collection").each(function () {
      $(this).find(".teaserBox .content").css({'height': 'auto'}).equalHeights(); //set content to same height
    });
  }

})();

(function () {
  // create coremedia namespace if it doesn't exist
  this.coremedia = window.coremedia || {};

  // create coremedia.blueprint namespace if it doesn't exist
  this.coremedia.blueprint = coremedia.blueprint || {};

  // extend coremedia.blueprint with externalpreview object
  this.coremedia.blueprint.externalpreview = {
    login: function(params) {
      params.token = encodeURI(params.token);
      $.ajax({
        url: "/blueprint/servlet/service/externalpreview?token=" + encodeURIComponent(params.token) + "&method=login",
        dataType: "json",
        cache: false,
        data:[],
        success: function (data, textStatus, jqXHR) {
          params.callback(data.status === "ok");
        },
        error: function(jdXHR, textStatus, errorThrown) {
          params.callback(false);
        }
      });
    }
  };
})();

$(function() {
  var $inputFormElement = $("#previewToken");
  $("#loginButton").click(function() {
    var token = $inputFormElement.val();
    var errorElement = $("#loginError");
    coremedia.blueprint.externalpreview.login({
      token: token,
      callback: function(result) {
        if (result) {
          errorElement.removeClass("cm-external-preview__error-text--visible");
          window.location = "preview.html#" + token;
        } else {
          errorElement.addClass("cm-external-preview__error-text--visible");
        }
      }
    });
  });

  $(window).click(function() {
    var errorElement = $("#loginError");
    errorElement.removeClass("cm-external-preview__error-text--visible");
    });

  $inputFormElement.keypress(function(event) {
    // look for window.event in case event isn't passed in
    if (event === undefined && window.event !== undefined) {
      event = window.event;
    }
    if (event !== undefined && event.keyCode === 13) {
      $("#loginButton").click();
    }
  });
  
  //supress submit
  $("form.cm-external-preview__form").submit(function(e){
    e.preventDefault();
  });

  $inputFormElement.focus();
});

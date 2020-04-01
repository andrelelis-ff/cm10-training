(function () {
  // create coremedia namespace if it doesn't exist
  this.coremedia = window.coremedia || {};

  // create coremedia.blueprint namespace if it doesn't exist
  this.coremedia.blueprint = coremedia.blueprint || {};

  // extend coremedia.blueprint with externalpreview object
  this.coremedia.blueprint.externalpreview = {
    activeItemId: undefined,
    lastModificationDate: undefined,

    getHash: function() {
      var hash = window.location.hash;
      return hash.substring(1); // remove #
    },
    truncateName: function(name) {
      var result = name;
      if(name.length > 27) {
        name = name.substr(0,27) + "...";
      }
      return name;
    },
    /**
     * Returns preview information for a specified token.
     *
     * If the preview information cannot be retrieved result is
     * false, otherwise the following struct is returned:
     * -previewable is true if the preview has something to preview, otherwise false
     * -name contains the name of the preview
     * -url contains an url to the preview page
     * -updated is true if the preview was updated since the last call
     *
     * @param token The preview-token which information to be retrieved
     * @param callback function retrieving information after ajax call.
     *   param: response: {{previewable: boolean, name: string, url: string},undefined}
     */

    getPreviewInformation: function(params) {

      $.ajax({
        url: "/blueprint/servlet/service/externalpreview?token=" + encodeURIComponent(params.token) + "&method=list",
        dataType: "json",
        cache: false,
        data: [],
        success: function(data, textStatus, jqXHR) {

          var result = undefined;
          // check if an error was send via status element
          if (data.status === undefined) {

            result = {
              previewable: false,
              name: "",
              url: "No preview available"
            };

            for (var i = 0; i < data.length; i++) { //...check the documents to preview.
              var item = data[i];
              if (item.active) {
                result = {
                  previewable: item.preview,
                  name: coremedia.blueprint.externalpreview.truncateName(item.name),
                  url: item.previewUrl,
                  updated: (item.id != coremedia.blueprint.externalpreview.activeItemId)
                            || (item.modificationDate != coremedia.blueprint.externalpreview.lastModificationDate)
                };
                // update mod date.
                coremedia.blueprint.externalpreview.lastModificationDate = item.modificationDate;
                coremedia.blueprint.externalpreview.activeItemId = item.id;
              }
            }
          }
          params.callback(result);
        },
        error: function(jdXHR, textStatus, errorThrown) {
          params.callback(undefined);
        }
      });
    }
  };
})();

$(function() {

  // Init:
  var token = coremedia.blueprint.externalpreview.getHash();

  function loadPreview() {
    var frame = $("#previewFrame");
    coremedia.blueprint.externalpreview.getPreviewInformation({
      token: token,
      callback: function(response) {
        if (response !== undefined) {
          if (response.updated) {
            $("#previewName").html(response.name);
            $("#previewUrl").val(response.url);
            $(frame).attr("src", "empty.html");
            if (response.previewable) {
              $(frame).attr("src", response.url);
            }
          }
          setTimeout(
            function() {
              loadPreview();
            }, 5000
          );
        } else {
          window.location = "index.html";
        }
      }
    });
  }

  $("#reloadButton").click(function() {
    if($("#previewUrl").val().length) {
      var frame = $("#previewFrame");
      $(frame).attr("src", "loading.html");
      $(frame).attr("src", $("#previewUrl").val());
    }
  });

  loadPreview();
});

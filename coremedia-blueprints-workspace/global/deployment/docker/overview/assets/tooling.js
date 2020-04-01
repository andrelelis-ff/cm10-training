var tooling = function() {
          $("a[href*=\"docker.localhost\"]").each(function() {
            $(this).attr("href", $(this).attr("href").replace("docker.localhost", window.location.hostname.replace("overview.", "")));
          });
          checkStatus();
          var interval = setInterval(checkStatus, 20000);
        },
        checkStatus = function() {
          $(".health_check").each(function() {
            var $that = $(this);
            var $status = $that.parents("tr").find("span");
            fetch($that.attr("href")).then(function(response) {
              if (response.status === 200) {
                $status.removeClass("check_unknown").addClass("check_ok");
                $status.text("Healthy");
              } else {
                $status.removeClass("check_unknown").addClass("check_fail");
                $status.text("Unhealthy");
              }
            }).catch(function(error) {
            });
          });
        };

$(function() {
  tooling();
});

<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#assign setGUIDCookieLink=cm.getLink(cmpage.navigation.rootNavigation, "asGuidCookieLink")!"" />

<script type="text/javascript">
  window.onload = function () {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', "${setGUIDCookieLink}", true);
    xhr.send();
  };
</script>

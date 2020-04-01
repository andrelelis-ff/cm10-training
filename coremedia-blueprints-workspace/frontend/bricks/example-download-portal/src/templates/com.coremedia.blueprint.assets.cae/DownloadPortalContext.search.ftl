<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.cae.DownloadPortalContext" -->

<form class="am-search" method="get" role="search" data-hash-based-fragment-form="">
  <label for="searchterm" class="am-search__label">${cm.getMessage("search_label")}</label>
  <div class="am-search__input">
    <input id="searchterm" type="search" class="am-input am-text-scalable" name="search" value="${self.getSearchTerm()}" placeholder="${cm.getMessage("am_search_placeholder")}" minlength="3" required="">
  </div>
  <button type="submit" class="am-search__submit am-icon-button am-icon am-icon--search am-text-scalable"></button>
</form>

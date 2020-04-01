<#-- @ftlvariable name="self" type="com.coremedia.objectserver.web.HttpError" -->

<div class="am-download-portal">

  <div class="am-download-portal__header">
    <div class="am-download-portal__breadcrumb"></div>
    <h1 class="am-download-portal__title am-heading-1">${cm.getMessage("am_download_portal")}</h1>
  </div>

  <div class="am-download-portal__content am-error">
    <h1 class="am-error__title am-heading-2">${cm.getMessage("am_error_not_found_title")}</h1>
    <div class="am-error__message">
      <p >${cm.getMessage("am_error_not_found_message")}</p>
      <p><a class="am-text-link" data-hash-based-fragment-link="">${cm.getMessage("am_back_to_download_portal")}</a></p>
    </div>
  </div>
</div>

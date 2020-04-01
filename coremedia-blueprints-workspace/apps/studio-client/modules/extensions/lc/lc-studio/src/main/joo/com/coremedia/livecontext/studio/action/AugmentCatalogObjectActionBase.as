package com.coremedia.livecontext.studio.action {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.authorization.AccessControl;
import com.coremedia.cap.content.authorization.Right;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.ContentCreationUtil;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ui.data.RemoteBeanUtil;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

/**
 * This action is intended to be used from within EXML, only.
 *
 */
public class AugmentCatalogObjectActionBase extends CreateCatalogObjectDocumentAction {

  /**
   * @param config the configuration object
   */
  public function AugmentCatalogObjectActionBase(config:AugmentCategoryAction = null) {
    super(config);
  }


  override protected function isDisabledFor(catalogObjects:Array):Boolean {
    var disabled:Boolean = super.isDisabledFor(catalogObjects);
    if (!disabled && catalogObjects.length == 1) {
      var catalogObject:CatalogObject = catalogObjects[0] as CatalogObject;
      var siteId:String = catalogObject.getSiteId();
      var site:Site = editorContext.getSitesService().getSite(siteId);
      if (site) {
        var siteRootFolder:Content = site.getSiteRootFolder();
        var repository:ContentRepository = siteRootFolder.getRepository();
        var accessControl:AccessControl = repository.getAccessControl();
        disabled = !RemoteBeanUtil.isAccessible(siteRootFolder) ||
                 !accessControl.mayPerformForType(siteRootFolder, repository.getContentType(getContentType()), Right.WRITE);
      }
    }
    return disabled;
  }

  override protected function myHandler():void {
    var catalogObject:CatalogObject = getCatalogObjects()[0];
    if (isCorrectType(catalogObject)) {
      //call AugmentationService
      var augmentCommerceBeanUri:String = catalogObject.getStore().getUriPath() + "/augment";
      var remoteServiceMethod:RemoteServiceMethod = new RemoteServiceMethod(augmentCommerceBeanUri, 'POST', true);
      remoteServiceMethod.request({$Ref: catalogObject.getUriPath()}, function (response:RemoteServiceMethodResponse):void {
        if (response.success) {
          var content:Content = Content(response.getResponseJSON());
          content.load(function ():void {
            ContentCreationUtil.initialize(content);
            editorContext.getWorkAreaTabManager().replaceTab(catalogObject, content);
          });
        }
      });
    }
  }
}
}

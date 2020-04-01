package com.coremedia.ecommerce.studio.model {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentProxy;
import com.coremedia.ui.data.impl.RemoteBeanImpl;

public class CatalogObjectImpl extends RemoteBeanImpl implements CatalogObject, ContentProxy {

  public function CatalogObjectImpl(uri:String) {
    super(uri);
    //Do not set immideate properties in subclasses.
    //ExternalId and SiteId need to be url en- and decoded.
    //This is done by jersey on the server side.
  }

  override public function get(property:*):* {
    try {
      return super.get(property);
    } catch (e:Error) {
      // catalog objects such as marketing spots do not use stable IDs and may vanish any time :(
      trace("[INFO] ignoring error while accesing property", property, e);
      return null;
    }
  }

  public function getContent():Content {
    return get(CatalogObjectPropertyNames.CONTENT);
  }

  public function getName():String {
    return get(CatalogObjectPropertyNames.NAME);
  }

  public function getShortDescription():String {
    return get(CatalogObjectPropertyNames.SHORT_DESCRIPTION);
  }

  public function getExternalId():String {
    return get(CatalogObjectPropertyNames.EXTERNAL_ID);
  }

  override public function getId():String {
    return get(CatalogObjectPropertyNames.ID);
  }

  public function getExternalTechId():String {
    return get(CatalogObjectPropertyNames.EXTERNAL_TECH_ID);
  }

  public function getStore():Store {
    return get(CatalogObjectPropertyNames.STORE);
  }

  public function getSiteId():String {
    return getUriPath().split('/')[2];
  }

  public function getCustomAttributes():Object {
    return get(CatalogObjectPropertyNames.CUSTOM_ATTRIBUTES);
  }

  public function getCustomAttribute(attribute:String):Object {
    return getCustomAttributes() ? getCustomAttributes()[attribute] : null;
  }

  override public function invalidate(callback:Function = null):void {
    if (!hasAnyListener()) {
      super.invalidate();
      return;
    }

    super.invalidate(function () {
      var content:Content = getContent();
      if (content && content.getIssues()) {
        content.getIssues().invalidate(callback);
      } else if (callback is Function){
        callback();
      }
    });
  }
}
}

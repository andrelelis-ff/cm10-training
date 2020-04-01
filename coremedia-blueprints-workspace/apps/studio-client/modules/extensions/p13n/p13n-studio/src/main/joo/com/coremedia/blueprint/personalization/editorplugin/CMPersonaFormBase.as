package com.coremedia.blueprint.personalization.editorplugin {

import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.common.impl.StructRemoteBeanImpl;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.editor.sdk.premular.DocumentTabPanel;
import com.coremedia.ui.data.ValueExpression;

public class CMPersonaFormBase extends DocumentTabPanel {


  protected static const TAXONOMY_PROPERTY_NAME_EXPLICIT:String = "explicit";
  protected static const TAXONOMY_PROPERTY_NAME_IMPLICIT:String = "subjectTaxonomies";
  protected static const PROFILE_IMAGE_NAME:String = "profileImage";

  private static const PROPERTY_PREFIX_PATH:String = "properties";
  private static const CONTENT_TYPE:String = 'CMTaxonomy';
  private static const PICTURE_TYPE:String = 'CMPicture';

  public function CMPersonaFormBase(config:CMPersonaForm = null) {
    super(config);

    var remoteValue:ValueExpression = config.bindTo.extendBy('properties.profileExtensions');


    // lets load the struct from the remoteValue
    remoteValue.loadValue(function (structRemoteBean:StructRemoteBeanImpl):void {
      // and load the properties from that bean
      structRemoteBean.load(function ():void {
        createPropertiesIfNecessary(structRemoteBean);
      });
    });
  }

  /**
   * Create taxonomy properties if they doesn't exit already. Also, the type of the taxonomy property (Array) is set to
   * its linkListProperty.
   * @param structRemoteBean the bean that holds the struct properties
   * @param contentType the contentType that needs to be added to the LinkListProperty
   */
  private static function createPropertiesIfNecessary(structRemoteBean:StructRemoteBeanImpl):void {
    var properties:Struct = structRemoteBean.get(PROPERTY_PREFIX_PATH);
    var taxonomyContentType:ContentType = SESSION.getConnection().getContentRepository().getContentType(CONTENT_TYPE);
    var pictureContentType:ContentType = SESSION.getConnection().getContentRepository().getContentType(PICTURE_TYPE);

    if (!properties) {
      structRemoteBean.getType().addStructProperty(PROPERTY_PREFIX_PATH);
      properties = structRemoteBean.get(PROPERTY_PREFIX_PATH);
    }

    if (!properties.get(TAXONOMY_PROPERTY_NAME_EXPLICIT)) {
      properties.getType().addLinkListProperty(TAXONOMY_PROPERTY_NAME_EXPLICIT, taxonomyContentType, []);
    }

    if (!properties.get(TAXONOMY_PROPERTY_NAME_IMPLICIT)) {
      properties.getType().addLinkListProperty(TAXONOMY_PROPERTY_NAME_IMPLICIT, taxonomyContentType, []);
    }

    if (!properties.get(PROFILE_IMAGE_NAME)) {
      properties.getType().addLinkListProperty(PROFILE_IMAGE_NAME, pictureContentType, []);
    }
  }


}
}

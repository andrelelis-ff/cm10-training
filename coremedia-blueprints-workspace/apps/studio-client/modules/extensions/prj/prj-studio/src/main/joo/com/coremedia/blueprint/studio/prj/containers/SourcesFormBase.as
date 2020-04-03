package com.coremedia.blueprint.studio.prj.containers {
  import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
  import com.coremedia.cms.editor.sdk.util.PropertyEditorUtil;
  import com.coremedia.cms.editor.sdk.util.StructContentLinkListWrapper;

  public class SourcesFormBase extends PropertyFieldGroup {
    protected static const PROPERTY_NAME:String = "prjExtendedFields";
    protected static const STRUCT_LIST_PROPERTY_NAME:String = "sources";
    protected static const BEAN_PROPERTY_NAME:String = "link";
    protected static const FULL_PROPERTY_NAME:String = PROPERTY_NAME + "." + STRUCT_LIST_PROPERTY_NAME;
    protected static const LINK_TYPE_NAME:String = "CMSymbol";

    public function SourcesFormBase(config:SourcesForm = null) {
      super(config);
    }

    protected static function getStructContentLinkListWrapper(config:SourcesForm):StructContentLinkListWrapper {
      var linkListWrapperCfg:StructContentLinkListWrapper = StructContentLinkListWrapper({});
      linkListWrapperCfg.bindTo = config.bindTo;
      linkListWrapperCfg.linkTypeName = LINK_TYPE_NAME;
      linkListWrapperCfg.propertyName = PROPERTY_NAME;
      linkListWrapperCfg.structListPropertyName = STRUCT_LIST_PROPERTY_NAME;
      linkListWrapperCfg.beanPropertyName = BEAN_PROPERTY_NAME;
      linkListWrapperCfg.readOnlyVE = PropertyEditorUtil.createReadOnlyValueExpression(config.bindTo, config.forceReadOnlyValueExpression);
      return new StructContentLinkListWrapper(linkListWrapperCfg);
    }
  }
}

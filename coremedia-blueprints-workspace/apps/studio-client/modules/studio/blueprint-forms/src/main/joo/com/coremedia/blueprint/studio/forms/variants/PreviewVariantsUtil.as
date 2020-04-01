package com.coremedia.blueprint.studio.forms.variants {
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.preview.PreviewUrlServiceConfig;
import com.coremedia.cms.editor.sdk.preview.PreviewUrlServiceUtil;

import mx.resources.ResourceManager;

public class PreviewVariantsUtil {

  public static const DEFAULT_VARIANT:String = "default";

  [ArrayElementType("String")]
  public static const PREVIEW_VARIANTS:Array = PreviewUrlServiceUtil.isPreviewVariantsEnabled() && getPreviewVariants();

  public static function calculateDisplayNameForVariant(variant:String):String {
    variant = variant || DEFAULT_VARIANT;
    return ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.forms.variants.PreviewVariants', 'Variant_' + variant + '_displayName');
  }

  [ArrayElementType("String")]
  public static function getPreviewVariants():Array {
    var previewVariants:Array = [];
    PreviewUrlServiceUtil.getPreviewUrlServiceConfigs().forEach(function (previewUrlServiceConfig:PreviewUrlServiceConfig):void {
      previewVariants.push(previewUrlServiceConfig.id)
    });
    return previewVariants;
  }

  public static function getDefaultPreviewVariant():String {
    if (PreviewUrlServiceUtil.isPreviewVariantsEnabled()) {
      var previewVariants:Array = getPreviewVariants();
      return previewVariants.length > 0 ? previewVariants[0] : DEFAULT_VARIANT;
    }
  }

  public static function canHaveVariants(content:Content):Boolean {
    if (!(content is Content)) {
      return false;
    }
    return content.getType().isSubtypeOf(SESSION.getConnection().getContentRepository().getContentType("CMLinkable"));
  }
}
}

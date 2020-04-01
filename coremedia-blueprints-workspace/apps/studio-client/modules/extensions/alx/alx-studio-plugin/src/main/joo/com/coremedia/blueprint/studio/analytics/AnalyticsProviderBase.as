package com.coremedia.blueprint.studio.analytics {

import com.coremedia.cms.editor.sdk.EditorPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;

public class AnalyticsProviderBase implements EditorPlugin {

  private var config:AnalyticsProvider;

  /**
   * Register Analytics Providers as [providerName, localizedProviderName] two-element array;
   */
  public static const ANALYTICS_PROVIDERS:Array = [];

  public function AnalyticsProviderBase(config:AnalyticsProvider = null) {
    this.config = config;
  }

  public function init(editorContext:IEditorContext):void {
    ANALYTICS_PROVIDERS.push([config.providerName, config.localizedProviderName]);
  }

}
}
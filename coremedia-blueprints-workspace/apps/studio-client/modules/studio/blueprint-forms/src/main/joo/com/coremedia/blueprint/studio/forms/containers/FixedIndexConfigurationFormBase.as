package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.components.IAnnotatedLinkListForm;

public class FixedIndexConfigurationFormBase extends PropertyFieldGroup implements IAnnotatedLinkListForm {

  private var _settingsVE:ValueExpression;

  private var _indexSettings:FixedIndexSettings;

  private var _indexViewModel:FixedIndexViewModel;

  public function FixedIndexConfigurationFormBase(config:FixedIndexConfigurationForm = null) {
    super(config);
  }

  [Bindable]
  internal function get indexSettings():FixedIndexSettings {
    if (!_indexSettings) {
      _indexSettings = new FixedIndexSettings(settingsVE);
    }
    return _indexSettings;
  }

  [Bindable]
  internal function get indexViewModel():FixedIndexViewModel {
    if (!_indexViewModel) {
      _indexViewModel = new FixedIndexViewModel();
    }
    return _indexViewModel;
  }

  override protected function onDestroy():void {
    indexViewModel.destroy();
    indexSettings.destroy();
    super.onDestroy();
  }

  [Bindable]
  public function set settingsVE(settingsVE:ValueExpression):void {
    _settingsVE = settingsVE;
  }

  [Bindable]
  public function get settingsVE():ValueExpression {
    return _settingsVE;
  }
}
}

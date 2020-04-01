package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.components.IAnnotatedLinkListForm;

public class TimelineConfigurationFormBase extends PropertyFieldGroup implements IAnnotatedLinkListForm {

  private var _settingsVE:ValueExpression;

  private var _timelineSettings:TimelineSettings;

  private var _timelineViewModel:FixedIndexViewModel;

  public function TimelineConfigurationFormBase(config:TimelineConfigurationForm = null) {
    super(config);
  }

  [Bindable]
  internal function get timelineSettings():TimelineSettings {
    if (!_timelineSettings) {
      _timelineSettings = new TimelineSettings(settingsVE);
    }
    return _timelineSettings;
  }

  [Bindable]
  internal function get timelineViewModel():FixedIndexViewModel {
    if (!_timelineViewModel) {
      _timelineViewModel = new FixedIndexViewModel();
    }
    return _timelineViewModel;
  }

  override protected function onDestroy():void {
    timelineViewModel.destroy();
    timelineSettings.destroy();
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

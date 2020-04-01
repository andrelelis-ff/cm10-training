package com.coremedia.blueprint.studio.topicpages.administration {

import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.ValueExpression;

import ext.data.Model;
import ext.form.Label;
import ext.panel.Panel;

/**
 * Base class of the topic pages preview panel that access an iframe for applying the preview URL.
 */
public class TopicPreviewPanelBase extends Panel {
  /**
   * The value expression that contains the selected topic record.
   */
  [Bindable]
  public var selectionExpression:ValueExpression;

  protected static const PREVIEW_FRAME:String = "topicPagesPreviewFrame";

  private var frameLabel:Label;
  private var lastUrl:String;

  public function TopicPreviewPanelBase(config:TopicPreviewPanelBase = null) {
    super(config);
    frameLabel = this.getComponent(PREVIEW_FRAME) as Label;
    selectionExpression.addChangeListener(selectionChanged);
  }

  /**
   * Fired when a new entry has been selected on the topic list.
   * The url is only updated when the selection has not changed for 2 seconds.
   */
  private function selectionChanged():void {
    var record:Model = selectionExpression.getValue();
    if(record) {
      var topic:Content = record.data.topic;
      topic.load(function():void {
        var url:String = topic.getPreviewUrl();
        url = url + '&site=' + editorContext.getSitesService().getPreferredSiteId();
        //recheck URL after 2 seconds
        window.setTimeout(function():void {
          if(lastUrl !== url) {
            lastUrl = url;
            trace('[INFO]', 'Updating topic page preview URL: ' + url);
            frameLabel.setText(getFrameHTML(url), false);
          }
        }, 2000);
      });
    }
    else {
      lastUrl = undefined;
    }
  }

  private static function getFrameHTML(url:String):String {
    return '<iframe frameborder="0" src="' +url + '" height="100%" width="100%"></iframe>';
  }
}
}
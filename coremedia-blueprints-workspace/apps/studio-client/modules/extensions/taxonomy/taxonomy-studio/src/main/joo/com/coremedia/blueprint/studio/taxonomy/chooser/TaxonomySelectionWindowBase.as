package com.coremedia.blueprint.studio.taxonomy.chooser {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.components.StudioDialog;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

/**
 * The base class of the taxonomy selection window.
 */
[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class TaxonomySelectionWindowBase extends StudioDialog {

  private var selectionExpression:ValueExpression;
  private var propertyValueExpression:ValueExpression;
  private var singleSelection:Boolean;

  public function TaxonomySelectionWindowBase(config:TaxonomySelectionWindow = null) {
    super(config);
    singleSelection = config.singleSelection;
    propertyValueExpression = config.propertyValueExpression;

    var selection:Array = [];
    var value:* = propertyValueExpression.getValue();
    if (value) {
      if (singleSelection && value as Content) {
        selection.push(value as Content);
      }
      else {
        selection = value as Array;
      }
    }

    getSelectionExpression().setValue(selection);
  }

  /**
   * Ok button handler.
   */
  protected function okPressed():void {
    var selection:Array = selectionExpression.getValue();
    if (!singleSelection) {
      propertyValueExpression.setValue(selection);
    }
    else {
      if (selection && selection.length > 0) {
        propertyValueExpression.setValue(selection[0]);
      }
      else {
        propertyValueExpression.setValue(null);
      }
    }
    close();
  }

  /**
   * Cancel button handler.
   */
  protected function cancelPressed():void {
    close();
  }

  //noinspection JSMethodCanBeStatic
  /**
   * Depending on single selection mode, a different link list title is displayed
   * for the active selection.
   * @param singleSelection
   * @return
   */
  protected function resolveSelectionTitle(singleSelection:Boolean):String {
    if (singleSelection) {
      return resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyLinkList_singleSelection_title');
    }
    return resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'TaxonomyLinkList_title');
  }

  /**
   * Contains the entries selected by the user.
   * @return
   */
  protected function getSelectionExpression():ValueExpression {
    if (!selectionExpression) {
      selectionExpression = ValueExpressionFactory.createFromValue([]);
    }
    return selectionExpression;
  }
}
}
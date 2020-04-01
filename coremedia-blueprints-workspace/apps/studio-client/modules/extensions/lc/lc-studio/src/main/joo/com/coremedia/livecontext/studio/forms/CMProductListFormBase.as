package com.coremedia.livecontext.studio.forms {
import com.coremedia.cms.editor.sdk.premular.DocumentTabPanel;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class CMProductListFormBase extends DocumentTabPanel {

  private var selectedfacetValueExpression:ValueExpression;

  public function CMProductListFormBase(config:CMProductListForm = null) {
    super(config);
  }

  public function getSelectedfacetValueExpression(){
    if (!selectedfacetValueExpression){
      selectedfacetValueExpression =  ValueExpressionFactory.createFromValue([]);
    }
    return selectedfacetValueExpression;
  }

  public function validatePosition(entry){
    return !isNaN(entry);
  }

}
}
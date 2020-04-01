package com.coremedia.livecontext.studio.forms {
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Facets;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.util.EventUtil;

import ext.data.Model;

import ext.form.field.ComboBox;

public class CategoryFacetsFieldGroupBase extends PropertyFieldGroup {

  private var categoryExpression:ValueExpression;

  private var facetsExpression:ValueExpression;

  private var facetNameExpression:ValueExpression;
  private var facetValueExpression:ValueExpression;

  private var facetCombo:ComboBox;
  private var facetValueCombo:ComboBox;

  public function CategoryFacetsFieldGroupBase(config:CategoryFacetsFieldGroup = null) {
    categoryExpression = config.bindTo.extendBy("properties." + config.externalIdPropertyName);

    facetNameExpression = config.bindTo.extendBy("properties." + config.facetNamePropertyName);
    facetValueExpression = config.bindTo.extendBy("properties." + config.facetValuePropertyName);

    super(config);
    facetCombo = itemCollection.get("facetCombo") as ComboBox;
    facetValueCombo = itemCollection.get("facetValueCombo") as ComboBox;

    //when the list of possible facets changed select the facet anew
    facetCombo.getStore().on('datachanged', function():void {
      facetNameExpression.loadValue(function():void {
        //unfortunately the combobox gets the list of possible values to late.
        //we have to give it more time so that when reverting the changes are correctly selected in the combo.
        EventUtil.invokeLater(setFacet);
      });
    });

    facetNameExpression.addChangeListener(setFacet);

    facetCombo.on('change', function():void {
      var value:String = facetCombo.getValue() || '';
      facetNameExpression.setValue(value);
    });

    //when the list of possible facet values changed select the value anew
    facetValueCombo.getStore().on('datachanged', function():void {
      facetValueExpression.loadValue(function():void {
        //unfortunately the combobox gets the list of possible values to late.
        //we have to give it more time so that when reverting the changes are correctly selected in the combo.
        EventUtil.invokeLater(function():void {
          var value:* = facetValueExpression.getValue();
          var model:Model = facetValueCombo.findRecord("id", value);
          if (model) {
            facetValueCombo.select(model);
          } else {
            facetValueCombo.clearValue();
          }
        });
      });
    });

    facetValueCombo.on('change', function():void {
      var value:String = facetValueCombo.getValue() || '';
      facetValueExpression.setValue(value);
    });

    facetValueExpression.addChangeListener(setFacetValue);

    on("destroy", function():void {
      facetNameExpression.removeChangeListener(setFacet);
      facetValueExpression.removeChangeListener(setFacetValue);

    })

  }

  private function setFacet():void {
    var value:* = facetNameExpression.getValue();
    var model:Model = facetCombo.findRecord("id", value);
    if (model) {
      facetCombo.select(model);
    } else {
      facetCombo.clearValue();
    }
  }

  private function setFacetValue():void {
    var value:* = facetValueExpression.getValue();
    var model:Model = facetValueCombo.findRecord("id", value);
    if (model) {
      facetValueCombo.select(model);
    } else {
      facetValueCombo.clearValue();
    }
  }

  private function getFacetsExpression():ValueExpression {
    if (!facetsExpression) {
      facetsExpression = ValueExpressionFactory.createFromFunction(
              function ():* {
                var link:String = categoryExpression.getValue();
                if (!link) {
                  return link;
                }
                var category:Category = CatalogHelper.getInstance().getCatalogObject(link, bindTo) as Category;
                return category && category.getFacets();
              }
      );
    }
    return facetsExpression;
  }

  protected function getFacetNamesExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(
            function ():* {
              var facets:Facets = getFacetsExpression().getValue();
              if (!facets) {
                return facets;
              }
              var facetsFacets:Object = facets.getFacets();
              if (!facetsFacets) {
                return facetsFacets;
              }

              var valuesToChoose:Array = [];
              for (var key:String in facetsFacets) {
                valuesToChoose.push({id: key, value: key});
              }
              return valuesToChoose;
            }
    );
  }

  protected function getFacetValuesExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(
            function ():* {
              var facets:Facets = getFacetsExpression().getValue();
              if (!facets) {
                return facets;
              }
              var possibleFacetSelector:Array = [];
              var selectedFacet:String = facetNameExpression.getValue();
              if (!selectedFacet) {
                return selectedFacet;
              }
              var facets2:* = facets.getFacets();
              if (facets2 === undefined) {
                //wait for facets2 to be loaded
                return undefined;
              }
              if (facets2 === null) {
                // facets2 loaded but empty.
                return [];
              }
              var facetValues:Array = facets2[selectedFacet];
              if (facetValues.length > 0) {
                for (var i:Number = 0; i < facetValues.length; i++) {
                  possibleFacetSelector.push({id: facetValues[i].id, value: facetValues[i].value});
                }
              }
              return possibleFacetSelector;
            }
    );
  }
}
}

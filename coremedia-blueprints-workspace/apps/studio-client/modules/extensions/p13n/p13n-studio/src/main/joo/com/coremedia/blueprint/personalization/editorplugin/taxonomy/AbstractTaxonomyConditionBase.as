package com.coremedia.blueprint.personalization.editorplugin.taxonomy {

import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeList;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyUtil;
import com.coremedia.blueprint.studio.taxonomy.action.OpenTaxonomyChooserAction;
import com.coremedia.blueprint.studio.taxonomy.selection.TaxonomySearchField;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.undoc.content.ContentUtil;
import com.coremedia.personalization.ui.condition.AbstractCondition;
import com.coremedia.personalization.ui.util.SelectionRuleHelper;
import com.coremedia.ui.components.IconButton;
import com.coremedia.ui.components.StatefulNumberField;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.plugins.HorizontalSpacingPlugin;

import ext.Ext;
import ext.form.FieldContainer;
import ext.form.Label;
import ext.form.field.TextField;
import ext.form.field.VTypes;
import ext.layout.container.HBoxLayout;

import mx.resources.ResourceManager;

/**
 * A Condition specialized for editing <i>taxonomy conditions</i>. A taxonomy condition consists of a linked taxonomy
 * (as Keyword), a comparison operator, and a value field (percentage).
 *
 * @xtype com.coremedia.blueprint.studio.taxonomy.condition.AbstractTaxonomyCondition
 */
[ResourceBundle('com.coremedia.personalization.ui.Personalization')]
[ResourceBundle('com.coremedia.icons.CoreIcons')]
[ResourceBundle('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin')]
public class AbstractTaxonomyConditionBase extends AbstractCondition {

  {
    // introduce new vtypes for keywords and values
    VTypes['keywordConditionKeywordVal'] = /^[a-zA-Z_][a-zA-Z_0-9.]*$/;
    VTypes['keywordConditionKeywordMask'] = /^[a-zA-Z_0-9.]/;
    VTypes['keywordConditionKeywordText'] = ResourceManager.getInstance().getString('com.coremedia.personalization.ui.Personalization', 'p13n_error_keywordText');
    VTypes['keywordConditionKeyword'] = function (v:*):* {
      return VTypes['keywordConditionKeywordVal'].test(v);
    };
    VTypes['keywordConditionValueVal'] = /^\d+(\.\d+)?$/;
    VTypes['keywordConditionValueMask'] = /^[0-9.]/;
    VTypes['keywordConditionValueText'] = ResourceManager.getInstance().getString('com.coremedia.personalization.ui.Personalization', 'p13n_error_valueText');
    VTypes['keywordConditionValue'] = function (v:*):* {
      return VTypes['keywordConditionValueVal'].test(v);
    };
  }

  private static const VALUE_EMPTY_TEXT:String = ResourceManager.getInstance().getString('com.coremedia.personalization.ui.Personalization', 'p13n_op_value');

  //
  // ui components
  //
  private var keywordField:TextField;
  private var valueField:StatefulNumberField;

  // the internal prefix used for keywords. See class comment.
  private var propertyPrefix:String;


  //active selection
  private var taxonomySelectionExpr:ValueExpression = ValueExpressionFactory.create('taxonomy', beanFactory.createLocalBean({taxonomy:[]}));

  /**
   * Creates a new TaxonomyCondition.
   *
   * @cfg {String} conditionName name to be used for this condition instance in the condition combox
   * @cfg {String} propertyPrefix prefix of context properties mapped to this condition instance. The characters
   * following the prefix in a property name are assumed to represent the keyword
   * @cfg {Boolean} isDefault set to true if this condition is to be the default condition of the condition panel. The
   * first condition in the list of the registered conditions with the default flag set is used as the default
   * @cfg {String} keywordEmptyText the text to be shown in the keyword field if it is empty. Defaults to <i>keyword</i>
   * @cfg {String} keywordText the text to place into the keyword field. Defaults to <i>null</i>
   * @cfg {String} keywordVType the validation type of the keyword field. See below
   * @cfg {String} operatorEmptyText the text to be shown if no operator is selected. Default to <i>operator</i>
   * @cfg {Object} operatorNames user-presentable names of the operators. See below
   * @cfg {String} operator the operator to select initially. See below
   * @cfg {String} valueEmptyText the text to be shown in the value field if it is empty. Defaults to <i>value</i>
   * @cfg {String} valueText the text to place into the value field
   * @cfg {String} valueVType the validation type of the value field. See below
   * @cfg {String} suffixText the text to be shown after the value field. Defaults to <i>null</i>
   *
   * The property prefix is used to transform keyword properties to and from a user-presentable form. In a typical scenario,
   * keyword properties in a profile will use a common prefix to identify them as keywords, e.g. 'keyword'. This prefix
   * shouldn't be shown to users of the UI. If the propertyPrefix property is set to the internally used prefix, this condition
   * component will remove the prefix (including the '.' separator) from the keyword property before it is displayed, and
   * add it to the value in the keyword field when it is read via getPropertyName.
   *
   * The default validation types are:
   * <ul>
   * <li>For the keyword field: <code>/^[a-zA-Z_][a-zA-Z_0-9\.]*$/</code>.</li>
   * <li>For the value field: <code>/^\d+(\.\d+)?$/</code>.</li>
   * </ul>
   *
   * The <b>operators</b> offered by this component are:
   *
   * <ul>
   * <li>'lt'</li> less than
   * <li>'le'</li> less than or equals
   * <li>'eq'</li> equals
   * <li>'ge'</li> greater than or equals
   * <li>'gt'</li> greater than
   * </ul>
   *
   * The names used for the available operators can be overridden by a dictionary supplied via
   * the <b>operatorNames</b> property. The available operators and their default names are:
   *
   * <ul>
   * <li>'lt': 'less'</li>
   * <li>'le': 'less or equal'</li>
   * <li>'eq': 'equal'</li>
   * <li>'ge': 'greater or equal'</li>
   * <li>'gt': 'greater'</li>
   * </ul>
   *
   * You may override an arbitrary subset of these values.
   *
   * @param config configuration of this instance
   */
  public function AbstractTaxonomyConditionBase(config:AbstractTaxonomyCondition = null) {
    super(AbstractCondition(Ext.apply(config, {
      /* obligatory configuration. overrides supplied properties */
      layout:"hbox",
      layoutConfig:{
        flex:1
      }
    }, {
      autoWidth:true
    })));

    // store the keyword prefix
    propertyPrefix = config.propertyPrefix;
    if (propertyPrefix === null) {
      throw new Error(resourceManager.getString('com.coremedia.personalization.ui.Personalization', 'p13n_error_propertyPrefix'));
    }

    if (propertyPrefix !== null && propertyPrefix.charAt(propertyPrefix.length - 1) === '.') {
      // remove the '.' at the end of the prefix
      propertyPrefix = propertyPrefix.substring(0, propertyPrefix.length - 1);
    }
    taxonomySelectionExpr.addChangeListener(taxonomiesSelected);
  }

  /**
   * Adds the field that contains the keyword.
   */
  public function addKeywordField():void {
    keywordField = new TaxonomySearchField(TaxonomySearchField({
      allowBlank: false,
      searchResultExpression:taxonomySelectionExpr,
      taxonomyId:TaxonomyConditionUtil.getTaxonomyId4Chooser(propertyPrefix),
      flex: 40,
      cls:'force-ellipsis combo-text-field'
    }));
    add(keywordField);
  }

  /**
   * Adds the button that opens the TaxonomyChooser.
   */
  public function addTaxonomyButton():void {
    var openChooserAction:OpenTaxonomyChooserAction = new OpenTaxonomyChooserAction(OpenTaxonomyChooserAction({
      taxonomyId:TaxonomyConditionUtil.getTaxonomyId4Chooser(propertyPrefix),
      singleSelection:true,
      tooltip:resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'Taxonomy_action_tooltip'),
      propertyValueExpression:taxonomySelectionExpr
    }));

    var btnCfg:IconButton = IconButton({});
    btnCfg.iconCls = resourceManager.getString('com.coremedia.icons.CoreIcons', 'add_tag');
    btnCfg.tooltip = resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', 'Taxonomy_action_tooltip');
    btnCfg.ariaLabel = btnCfg.tooltip;
    btnCfg.baseAction = openChooserAction;
    var btn:IconButton = new IconButton(btnCfg);
    add(btn);
  }

  /**
   * Adds the input field that contains the taxonomy value (1 or 0 at BooleanTaxonomyCondition, oder 0 - 100 at
   * PercentageTaxonomyCondition).
   * @param config
   * @param visible <code>false</code> to render this value field hidden
   */
  public function addValueField(config:AbstractTaxonomyCondition, visible:Boolean = true):void {
    var layoutConfig:HBoxLayout = HBoxLayout({});
    layoutConfig.align = "middle";

    var containerConfig:FieldContainer = FieldContainer({});
    containerConfig.flex = 30;
    containerConfig.layout = layoutConfig;
    containerConfig.plugins = [HorizontalSpacingPlugin({})];

    var fieldContainer:FieldContainer = new FieldContainer(containerConfig);
    valueField = new StatefulNumberField(StatefulNumberField({
      emptyText:config['valueEmptyText'] !== null ? config['valueEmptyText'] : VALUE_EMPTY_TEXT,
      allowBlank:false,
      flex: 1,
      minValue: 0,
      maxValue: 100,
      vtype:config['valueVType'] !== null ? config['valueVType'] : 'keywordConditionValue',
      enableKeyEvents:true,
      hidden:!visible
    }));
    valueField.addListener('keyup', function ():void {
      fireEvent('modified');
    });

    valueField.setValue(config.valueText);

    fieldContainer.add(valueField);
    if (config.suffixText !== null) {
      var labelConfig:Label = Label({});
      labelConfig.text = config.suffixText;
      fieldContainer.add(new Label(labelConfig));
    }
    add(fieldContainer);
  }

  /**
   * Invoked after the taxonomy chooser has been closed.
   * @param expr The value expression that contains the selection.
   */
  private function taxonomiesSelected(expr:ValueExpression):void {
    if (expr.getValue()) {
      var selection:TaxonomyNodeList = expr.getValue() as TaxonomyNodeList;
      if (selection) {
        var leafRef:String = selection.getLeafRef();
        var taxonomy:Content = ContentUtil.getContent(leafRef);
        taxonomy.load(function ():void {
          taxonomySelectionExpr.setValue(taxonomy); //this will trigger it self, but a different cast will apply then!
        });
      }
      else if (expr.getValue() as Content) {//will be executed after the second trigger event and finally sets the value.
        var taxonomy1:Content = expr.getValue() as Content;
        fireEvent('modified', this);
        keywordField.setValue(TaxonomyUtil.getTaxonomyName(taxonomy1));
      }
    }
    else {
      keywordField.setValue("");
      fireEvent('modified', this);
    }
  }

  public override function getPropertyName():String {
    var taxonomy:Content = this.taxonomySelectionExpr.getValue() as Content;
    return TaxonomyConditionUtil.formatPropertyName(propertyPrefix + '.', taxonomy);
  }

  public override function setPropertyName(name:String):void {
    var taxonomy:Content = TaxonomyConditionUtil.getTaxonomyContent(name);
    if (taxonomy) {
      taxonomy.load(function ():void {
        taxonomySelectionExpr.removeChangeListener(taxonomiesSelected);
        taxonomySelectionExpr.setValue(taxonomy);
        keywordField.setValue(TaxonomyUtil.getTaxonomyName(taxonomy));
        taxonomySelectionExpr.addChangeListener(taxonomiesSelected);
      });
    }
  }

  public override function getPropertyValue():String {
    const v:String = valueField.getValue();
    return v ? TaxonomyConditionUtil.formatPropertyValue4Store(v) : SelectionRuleHelper.EMPTY_VALUE;
  }

  public override function setPropertyValue(value:String):void {
    valueField.setValue(value === SelectionRuleHelper.EMPTY_VALUE ? null : TaxonomyConditionUtil.formatPropertyValue4Textfield(value));
  }
}
}

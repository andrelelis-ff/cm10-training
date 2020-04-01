package com.coremedia.blueprint.studio.taxonomy.chooser {
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNode;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeFactory;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyNodeList;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.desktop.WorkArea;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.components.IconDisplayField;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.skins.IconDisplayFieldSkin;
import com.coremedia.ui.util.EventUtil;

import ext.button.Button;
import ext.container.Container;

import js.Event;

/**
 * The taxonomy selector panel that steps through the hierarchy of a taxonomy type.
 */
[ResourceBundle('com.coremedia.icons.CoreIcons')]
public class TaxonomySelectorBase extends Container {
  private var ALPHABET:Array;

  private var activeLettersVE:ValueExpression;
  private var selectedLetterVE:ValueExpression;
  private var selectedNodeIdVE:ValueExpression;
  private var selectedNodeListVE:ValueExpression;

  private var taxonomyId:String;
  private var activePathList:TaxonomyNodeList;

  private var buttonCache:Array;

  public function TaxonomySelectorBase(config:TaxonomySelector = null) {
    ALPHABET = [];
    ALPHABET[0] = 'A';
    ALPHABET[1] = 'B';
    ALPHABET[2] = 'C';
    ALPHABET[3] = 'D';
    ALPHABET[4] = 'E';
    ALPHABET[5] = 'F';
    ALPHABET[6] = 'G';
    ALPHABET[7] = 'H';
    ALPHABET[8] = 'I';
    ALPHABET[9] = 'J';
    ALPHABET[10] = 'K';
    ALPHABET[11] = 'L';
    ALPHABET[12] = 'M';
    ALPHABET[13] = 'N';
    ALPHABET[14] = 'O';
    ALPHABET[15] = 'P';
    ALPHABET[16] = 'Q';
    ALPHABET[17] = 'R';
    ALPHABET[18] = 'S';
    ALPHABET[19] = 'T';
    ALPHABET[20] = 'U';
    ALPHABET[21] = 'V';
    ALPHABET[22] = 'W';
    ALPHABET[23] = 'X';
    ALPHABET[24] = 'Y';
    ALPHABET[25] = 'Z';
    super(config);

    taxonomyId = config.taxonomyId;
  }

  /**
   * Adds missing components to the container like the button list.
   */
  override protected function initComponent():void {
    super.initComponent();
    buttonCache = [];

    var alphabetPanel:Container = queryById('alphabetPanel') as Container;
    for (var i:int = 0; i < ALPHABET.length; i++) {
      var letter:String = ALPHABET[i];
      var alphaButton:LetterButton = new LetterButton(Button({
        text: letter,
        disabled: true,
        flex: 1,
        handler: buttonClicked
      }));
      alphabetPanel.add(alphaButton);
      buttonCache.push(alphaButton);
    }
  }

  /**
   * Updates the status of the letter buttons, depending on the active selection.
   */
  private function updateLetters():void {
    var activeLetters:Array = activeLettersVE.getValue();

    for (var i:int = 0; i < buttonCache.length; i++) {
      var alphaButton:LetterButton = buttonCache[i];
      alphaButton.setDisabled(true);
      var letter:String = alphaButton.getText().toLowerCase();
      for (var j:int = 0; j < activeLetters.length; j++) {
        var activeLetter:String = activeLetters[j];
        if (activeLetter === letter) {
          alphaButton.setDisabled(false);
          break;
        }
      }
    }
  }

  /**
   * Creates the value expression that contains an array with the active letters.
   * @return
   */
  protected function getActiveLettersExpression():ValueExpression {
    if (!activeLettersVE) {
      activeLettersVE = ValueExpressionFactory.create("letters", beanFactory.createLocalBean());
      activeLettersVE.addChangeListener(updateLetters);
    }
    return activeLettersVE;
  }

  /**
   * Creates the value expression that contains the taxonomy node that should be added to the selection link list.
   * @return
   */
  protected function getSelectedNodeIdValueExpression():ValueExpression {
    if (!selectedNodeIdVE) {
      selectedNodeIdVE = ValueExpressionFactory.create("nodeRef", beanFactory.createLocalBean());
      selectedNodeIdVE.addChangeListener(updateLevel);
    }
    return selectedNodeIdVE;
  }

  /**
   * Creates the value expression that contains the taxonomy node list
   * @return
   */
  protected function getSelectedNodeListValueExpression():ValueExpression {
    if (!selectedNodeListVE) {
      selectedNodeListVE = ValueExpressionFactory.create("nodeList", beanFactory.createLocalBean());
    }
    return selectedNodeListVE;
  }

  /**
   * Creates the value expression that contains the active selected button, if user clicked one.
   * @return
   */
  protected function getSelectedLetterExpression():ValueExpression {
    if (!selectedLetterVE) {
      selectedLetterVE = ValueExpressionFactory.create("letter", beanFactory.createLocalBean());
    }
    return selectedLetterVE;
  }

  /**
   * Fire when the user double clicked a node, so that the next sub-level/children are shown.
   */
  private function updateLevel():void {
    var ref:String = getSelectedNodeIdValueExpression().getValue();
    if (ref) {
      var content:Content = WorkArea.ACTIVE_CONTENT_VALUE_EXPRESSION.getValue();
      var siteId:String = editorContext.getSitesService().getSiteIdFor(content);
      if (ref === taxonomyId) {
        //update the list with the root children
        TaxonomyNodeFactory.loadTaxonomyRoot(siteId, taxonomyId, function (parent:TaxonomyNode):void {
          parent.loadChildren(false, function (list:TaxonomyNodeList):void {
            getSelectedNodeListValueExpression().setValue(list);
          });
        });
        //we do not have to build the path for the root taxonomy, since this only exists of the taxonomy id value.
        updatePathPanel(null);
      }
      else {
        //update the list with a regular child
        var currentList:TaxonomyNodeList = selectedNodeListVE.getValue();
        var newSelection:TaxonomyNode = currentList.getNode(ref);
        if (!newSelection) {
          /**
           * the update was triggered by a path item, so the selection node list (contains children of a path node),
           * so the active ref won't be found in the list.
           * Instead we look up the ref in the path list, this an item of this list was selected.
           */
          newSelection = activePathList.getNode(ref);
        }
        if (!newSelection.isLeaf()) {//do not show children of leafs, which are empty of course).
          newSelection.loadChildren( false,function (list:TaxonomyNodeList):void {
            getSelectedNodeListValueExpression().setValue(list);
          });
        }
        //update the path of the current selection, this will build the path above the list
        TaxonomyNodeFactory.loadPath(taxonomyId, ref, siteId,
                function (list:TaxonomyNodeList):void {
                  updatePathPanel(list);
                });
      }
    }
  }

  /**
   * Displays the current path selection.
   * @param list
   */
  private function updatePathPanel(list:TaxonomyNodeList):void {
    activePathList = list;
    var pathPanel:Container = getPathPanel();
    pathPanel.removeAll(true);

    //Add root
    var text:String = taxonomyId;
    var rootName:String = resourceManager.getString('com.coremedia.blueprint.studio.taxonomy.TaxonomyStudioPlugin', taxonomyId);
    if(rootName) {
      text = rootName;
    }
    
    var root:TextLinkButton = new TextLinkButton(TextLinkButton({
      text: text,
      itemId: taxonomyId.replace(/\s+/g, ''),
      handler: doSetLevel,
      node: new TaxonomyNode({name: text, ref: taxonomyId, taxonomyId:taxonomyId})
    }));

    //add each level incl. root
    if (activePathList && activePathList.getNodes()) {
      activePathList.getNodes().forEach(function (node:TaxonomyNode, index:int):void {
        if (index > 0) {
          var ref:String = node.getRef().replace("/", "-").replace(/\s+/g, '');//format valid itemId
          if(index == activePathList.getNodes().length-1) {
            var label:IconDisplayField = new IconDisplayField(IconDisplayField({
              value:node.getDisplayName(),
              iconCls: resourceManager.getString('com.coremedia.icons.CoreIcons', 'arrow_right')
            }));
            label.setUI(IconDisplayFieldSkin.DEFAULT.getSkin());
            pathPanel.add(label);
          }
          else {
            var pathItem:TextLinkButton = new TextLinkButton(TextLinkButton({
              node: node,
              itemId: ref,
              handler: doSetLevel
            }));
            pathItem.setIconCls(resourceManager.getString('com.coremedia.icons.CoreIcons', 'arrow_right'));
            pathPanel.add(pathItem);
          }
        }
        else {
          // add root
          pathPanel.add(root);
        }
      });
    }
    else {
      // add root
      pathPanel.add(root);
    }

    //refresh the layout
    pathPanel.updateLayout();
    //scroll right
    pathPanel.layout.overflowHandler.scrollTo(4000,true)
  }

  //noinspection JSUnusedLocalSymbols
  /**
   * The path selector item handler.
   */
  private function doSetLevel(button:TextLinkButton, e:Event):void {
    var nodeRef:String = button.node.getRef();
    getSelectedNodeIdValueExpression().setValue(nodeRef);
  }

  /**
   * Returns the path panel that contains the path and level selectors.
   * @return
   */
  private function getPathPanel():Container {
    return queryById('levelSelector') as Container;
  }


  //noinspection JSUnusedLocalSymbols
  /**
   * Applies the active button to the selected letter expression, so that the link list is updated.
   * @param b The button component.
   * @param e The js event.
   */
  private function buttonClicked(b:Button, e:Event):void {
    var letter:String = b.getText();
    getSelectedLetterExpression().setValue(letter.toLowerCase());
  }
}
}

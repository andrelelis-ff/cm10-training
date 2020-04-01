package com.coremedia.livecontext.p13n.studio {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.impl.ContentImpl;
import com.coremedia.cap.content.impl.ContentStructRemoteBeanImpl;
import com.coremedia.cap.content.impl.ContentTypeImpl;
import com.coremedia.cms.editor.sdk.util.PropertyEditorUtil;
import com.coremedia.ecommerce.studio.AbstractCatalogTest;
import com.coremedia.ecommerce.studio.components.CommerceObjectField;
import com.coremedia.ecommerce.studio.components.CommerceObjectSelector;
import com.coremedia.ecommerce.studio.forms.CommerceCatalogObjectsSelectForm;
import com.coremedia.ecommerce.studio.model.CategoryImpl;
import com.coremedia.ecommerce.studio.model.ContractImpl;
import com.coremedia.ecommerce.studio.model.ContractsImpl;
import com.coremedia.ecommerce.studio.model.ProductImpl;
import com.coremedia.ecommerce.studio.model.StoreImpl;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.BeanFactoryImpl;
import com.coremedia.ui.data.test.Step;
import com.coremedia.ui.util.createComponentSelector;

import ext.StringUtil;
import ext.button.Button;
import ext.container.Viewport;
import ext.data.Model;
import ext.data.Store;
import ext.form.field.DisplayField;

public class CommerceCatalogObjectsSelectFormTest extends AbstractCatalogTest {

  private var persona:Content;
  private var createReadOnlyValueExpression:Function;

  private var viewport:Viewport;
  private var selector:CommerceObjectSelector;

  override public function setUp():void {
    super.setUp();
    resetCatalogHelper();
    BeanFactoryImpl(beanFactory).registerRemoteBeanClasses(ContentTypeImpl, ContentImpl, ContentStructRemoteBeanImpl,
            StoreImpl, CategoryImpl, ContractsImpl, ContractImpl, ProductImpl);


    persona = beanFactory.getRemoteBean('content/300') as Content;
    //we need to mock the write access
    persona.getRepository().getAccessControl().mayWrite = function():Boolean {return true;};
  }

  private function createTestling(catalogObjectIds:Array):void {
    var bindTo:ValueExpression = ValueExpressionFactory.createFromValue(persona);
    var forceReadOnlyValueExpression:ValueExpression = ValueExpressionFactory.createFromValue(false);
    var catalogObjectIdsExpression:ValueExpression = ValueExpressionFactory.createFromValue(catalogObjectIds);

    //Mock PropertyEditorUtil#createReadOnlyValueExpression
    createReadOnlyValueExpression = PropertyEditorUtil.createReadOnlyValueExpression;
    PropertyEditorUtil.createReadOnlyValueExpression = function (contentValueExpression:ValueExpression, forceReadOnlyValueExpression:ValueExpression = undefined):ValueExpression {
      return ValueExpressionFactory.createFromFunction(function ():Boolean {
        if (forceReadOnlyValueExpression && forceReadOnlyValueExpression.getValue()) {
          return true;
        }
        if (!contentValueExpression) {
          return false;
        }
        var mayWrite:* = true;
        return mayWrite === undefined ? undefined : !mayWrite;
      });

    };

    var conf:CommerceCatalogObjectsSelectFormTestView = CommerceCatalogObjectsSelectFormTestView({});
    conf.content = persona;
    conf.bindTo = bindTo;
    conf.forceReadOnlyValueExpression = forceReadOnlyValueExpression;
    conf.catalogObjectIdsExpression = catalogObjectIdsExpression;

    viewport = new CommerceCatalogObjectsSelectFormTestView(conf);
    selector = viewport.queryById(CommerceCatalogObjectsSelectForm.SELECTOR_ITEM_ID) as CommerceObjectSelector;
  }

  override public function tearDown():void {
    super.tearDown();
    viewport.destroy();
    PropertyEditorUtil.createReadOnlyValueExpression = createReadOnlyValueExpression;
  }

  public function testSelectAndRemove():void {
    createTestling([]);
    chain(
            waitForTheSelectorLoadTheContracts(),
            selectTheExteriorContract(),
            waitForTheSelectorHasOnlyInteriorContract(),
            waitForOnlyOneContractFieldLoaded(),
            waitForTheExteriorContractFieldDisplayed(),
            selectTheInteriorContract(),
            waitForTheSelectorHasNoContract(),
            waitForTheContractFieldsLoaded(),
            waitForTheContractFieldsDisplayed(),
            removeTheExteriorContract(),
            waitForOnlyOneContractFieldLoaded(),
            waitForTheInteriorContractFieldDisplayed(),
            waitForTheSelectorHasOnlyExteriorContract()
    );
  }

  public function testInvalidCatalogObjectId():void {
    createTestling(["ibm:///catalog/contract/invalidid"]);
    chain(
            waitForOnlyOneContractFieldLoaded(),
            waitForContractFieldDisplaysInvalidMessage()
    );
  }

  private function waitForTheSelectorLoadTheContracts():Step {
    return new Step("Wait for the selector to load the contracts",
            function ():Boolean {
              var store:Store = Store(selector.getStore());
              return store.getCount() === 2 &&
              store.getAt(0).data.name === 'Contract for CoreMedia Preview Exterior' &&
              store.getAt(1).data.name === 'Contract for CoreMedia Preview Interior';
            }
    );
  }

  private function selectTheExteriorContract():Step {
    return new Step("Select the exterior contract",
            function ():Boolean {return true},
            function ():void {
              var model:Model = selector.findRecordByDisplay('Contract for CoreMedia Preview Exterior');
              var exteriorContractID:String = "ibm:///catalog/contract/4000000000000000507";
              selector.setValue(exteriorContractID);
              selector.fireEvent('select', selector, model);

            }
    );
  }

  private function selectTheInteriorContract():Step {
    return new Step("Select the interior contract",
            function ():Boolean {return true},
            function ():void {
              var model:Model = selector.findRecordByDisplay('Contract for CoreMedia Preview Interior');
              var interiorContractID:String = "ibm:///catalog/contract/4000000000000000508";
              selector.setValue(interiorContractID);
              selector.fireEvent('select', selector, model);
            }
    );
  }

  private function waitForTheSelectorHasOnlyInteriorContract():Step {
    return new Step("Wait for the selector to have only the interior contracts",
            function ():Boolean {
              var store:Store = Store(selector.getStore());
              return store.getCount() === 1 &&
                      store.getAt(0).data.name === 'Contract for CoreMedia Preview Interior';
            }
    );
  }

  private function waitForTheSelectorHasOnlyExteriorContract():Step {
    return new Step("Wait for the selector to have only the exterior contracts",
            function ():Boolean {
              var store:Store = Store(selector.getStore());
              return store.getCount() === 1 &&
                      store.getAt(0).data.name === 'Contract for CoreMedia Preview Exterior';
            }
    );
  }

  private function waitForTheSelectorHasNoContract():Step {
    return new Step("Wait for the selector to have no contracts",
            function ():Boolean {
              var store:Store = Store(selector.getStore());
              return store.getCount() === 0;
            }
    );
  }

  private function waitForOnlyOneContractFieldLoaded():Step {
    return new Step("Wait for only one contract field to be loaded",
            function ():Boolean {
              var commerceObjectFields:Array = getCommerceObjectFields();
              return commerceObjectFields && commerceObjectFields.length === 1;
            }
    );
  }

  private function getCommerceObjectFields():Array {
    return viewport.query(createComponentSelector()._xtype(CommerceObjectField.xtype).build());
  }

  private function waitForTheContractFieldsLoaded():Step {
    return new Step("Wait for the contract fields to be loaded",
            function ():Boolean {
              var commerceObjectFields:Array = getCommerceObjectFields();
              return commerceObjectFields && commerceObjectFields.length === 2;
            }
    );
  }

  private function waitForTheExteriorContractFieldDisplayed():Step {
    return new Step("Wait for the exterior contract field to be displayed",
            function ():Boolean {
              var commerceObjectFields:Array = getCommerceObjectFields();
              var commerceDisplayField:DisplayField = CommerceObjectField(commerceObjectFields[0])
                      .getComponent(CommerceObjectField.DISPLAYFIELD_ITEM_ID) as DisplayField;
              return commerceDisplayField.getValue() === 'Contract for CoreMedia Preview Exterior';
            }
    );
  }

  private function waitForTheInteriorContractFieldDisplayed():Step {
    return new Step("Wait for the interior contract field to be displayed",
            function ():Boolean {
              var commerceObjectFields:Array = getCommerceObjectFields();
              var commerceDisplayField:DisplayField = CommerceObjectField(commerceObjectFields[0])
                              .getComponent(CommerceObjectField.DISPLAYFIELD_ITEM_ID) as DisplayField;
              return commerceDisplayField.getValue() === 'Contract for CoreMedia Preview Interior';
            }
    );
  }

  private function waitForContractFieldDisplaysInvalidMessage():Step {
    return new Step("Wait for the contract field to show invalid message.",
            function ():Boolean {
              var commerceObjectFields:Array = getCommerceObjectFields();
              var commerceDisplayField:DisplayField = CommerceObjectField(commerceObjectFields[0])
                              .getComponent(CommerceObjectField.DISPLAYFIELD_ITEM_ID) as DisplayField;
              return commerceDisplayField.getValue() === StringUtil.format('Invalid e-Commerce user contract ID: {0}', "invalidid");
            }
    );
  }

  private function waitForTheContractFieldsDisplayed():Step {
    return new Step("Wait for the contract fields to be displayed",
            function ():Boolean {
              var commerceObjectFields:Array = getCommerceObjectFields();
              var commerceDisplayField:DisplayField = CommerceObjectField(commerceObjectFields[0])
                              .getComponent(CommerceObjectField.DISPLAYFIELD_ITEM_ID) as DisplayField;
              var commerceDisplayField2:DisplayField = CommerceObjectField(commerceObjectFields[1])
                              .getComponent(CommerceObjectField.DISPLAYFIELD_ITEM_ID) as DisplayField;
              return commerceDisplayField.getValue() === 'Contract for CoreMedia Preview Exterior'
                      && commerceDisplayField2.getValue() === 'Contract for CoreMedia Preview Interior';
            }
    );
  }

  private function removeTheExteriorContract():Step {
    return new Step("Remove the exterior contract",
            function ():Boolean {return true},
            function ():void {
              var commerceObjectFields:Array = getCommerceObjectFields();
              var commerceObjectRemoveButton1:Button = CommerceObjectField(commerceObjectFields[0])
                              .getComponent(CommerceObjectField.REMOVE_BUTTON_ITEM_ID) as Button;
              commerceObjectRemoveButton1.baseAction.execute(); // simulate click
            }
    );
  }

}
}
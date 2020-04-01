
package com.coremedia.ecommerce.studio {
import com.coremedia.cap.common.CapSession;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.common.descriptors.impl.CapPropertyDescriptorUtil;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.authorization.Right;
import com.coremedia.cap.content.impl.ContentImpl;
import com.coremedia.cap.content.impl.ContentRepositoryImpl;
import com.coremedia.cap.user.Group;
import com.coremedia.cap.user.User;
import com.coremedia.cap.workflow.WorkflowContentService;
import com.coremedia.cap.workflow.WorkflowRepository;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ui.data.Locale;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.BeanFactoryImpl;
import com.coremedia.ui.data.impl.RemoteBeanCache;
import com.coremedia.ui.data.test.AbstractRemoteTest;
import com.coremedia.ui.data.test.MockAjax;
import com.coremedia.ui.data.test.Step;
import com.coremedia.ui.util.EventUtil;
import com.coremedia.ui.util.RequestCounter;

import joo.getQualifiedObject;

public class AbstractCatalogTest extends AbstractRemoteTest {

  public static const ORANGES_EXTERNAL_ID:String = "GFR033_3301";
  public static const ORANGES_SKU_EXTERNAL_ID:String = "GFR033_330101";
  public static const BABY_SHOES_EXTERNAL_ID:String = "BSH016_1605";
  public static const MARKETING_SPOT_EXTERNAL_ID:String = "spot1";
  public static const MARKETING_SPOT_ID:String = "ibm:///catalog/marketingspot/" + MARKETING_SPOT_EXTERNAL_ID;
  public static const ORANGES_ID:String = "ibm:///catalog/product/" + ORANGES_EXTERNAL_ID;
  public static const SKU_ID_PREFIX:String = "ibm:///catalog/sku/";
  public static const ORANGES_SKU_ID:String = SKU_ID_PREFIX + ORANGES_SKU_EXTERNAL_ID;
  public static const ORANGES_NAME:String = "Oranges";
  public static const ORANGES_SHORT_DESC:String = "Organic and full of flavor oranges";
  public static const ORANGES_SKU_NAME:String = "Oranges SKU";
  public static const ORANGES_SKU_SHORT_DESC:String = "Organic and full of flavor oranges SKU";
  public static const ORANGES_IMAGE_URI:String = "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/homefurnishings/hta029_tableware/200x310/hta029_2932.jpg";
  public static const HERMITAGE_RUCHED_BODICE_COCKTAIL_DRESS:String = "Hermitage Ruched Bodice Cocktail Dress";

  public static const PRODUCT1_FROM_XMP_ID:String = "ibm:///catalog/product/xmp1";
  public static const PRODUCT2_FROM_XMP_ID:String = "ibm:///catalog/product/xmp2";

  private var contentRepository:ContentRepositoryImpl;
  private var preferredSiteExpression:ValueExpression;

  {
    EditorContextImpl.initEditorContext();
  }

  protected function resetCatalogHelper():void {
    var clazz:Class = getQualifiedObject("com.coremedia.ecommerce.studio.helper.CatalogHelper");
    getQualifiedObject("com.coremedia.ecommerce.studio").catalogHelper = new clazz();
  }

  override public function setUp():void {
    super.setUp();
    const CONTENT_ROOT:Content = new ContentImpl("content/1", {id: 1});
    RequestCounter.reset();
    BeanFactoryImpl.initBeanFactory();
    CapPropertyDescriptorUtil.registerResolver();
    BeanFactoryImpl(beanFactory).registerRemoteBeanClasses(ContentRepositoryImpl);
    contentRepository = beanFactory.getRemoteBean("content") as ContentRepositoryImpl;
    contentRepository.getRoot = function ():Content {
      return CONTENT_ROOT;
    };

    var preferredSite:Object = {
      getName: function ():String {
        return preferredSiteExpression.getValue();
      },
      getSiteRootFolder: function ():Content {
        return contentRepository.getRoot();
      },
      getLocale: function ():Locale {
        return new Locale({'displayName': 'English'});
      },
      getMasterSite: function ():Site {
        return null;
      },
      getDerivedSites: function ():Array {
        return [];
      },
      getId: function():String {
        return preferredSiteExpression.getValue();
      }
    };

    contentRepository.getAccessControl = function ():Object {
      return {
        mayPerform: function (content:Content, right:Right):* {
          return true;
        },
        mayPerformForType: function (content:Content, contentType:ContentType, right:Right):* {
          return true;
        },
        mayCreate: function (content:Content, right:Right):* {
          return true;
        },
        filterReadableContents: function (contents:Array):* {
          return contents;
        },
        mayCopy: function (contents:Array, target:Content):* {
          return true;
        },
        mayMove: function (contents:Array, target:Content):* {
          return true;
        },
        mayWrite: function (contents:Array, target:Content):* {
          return true;
        },
        mayRename: function (contents:Array, target:Content):* {
          return true;
        },
        isWritable: function (content:Content):* {
          return true;
        }
      };
    };

    SESSION = CapSession({
      getConnection: function ():Object {
        return {
          getContentRepository: function ():ContentRepository {
            return contentRepository;
          },
          getWorkflowRepository: function ():WorkflowRepository {
            return WorkflowRepository({
              getWorkflowContentService: function ():WorkflowContentService {
                return WorkflowContentService({
                  isLockedForUser: function (content:Content):Boolean {
                    return false;
                  }
                });
              }
            });
          }
        };
      },
      getUser: function ():User {
        //noinspection JSUnusedGlobalSymbols
        return User({
          isMemberOf: function (group:Group, callback:Function):void {
            EventUtil.invokeLater(callback, true);
          },
          getHomeFolder: function ():Content {
            return new ContentImpl("content/5", {id: 5});
          },
          getUriPath: function ():String {
            return "user/1";
          },
          isAdministrative: function ():Boolean {
            return true;
          }
        });
      }
    });
    preferredSiteExpression = ValueExpressionFactory.createFromValue('HeliosSiteId'); //HELIOS

    editorContext['getSitesService'] = function ():Object {
      return {
        getPreferredSiteIdExpression: function ():ValueExpression {
          return preferredSiteExpression;
        },
        getPreferredSiteId: function ():String {
          return preferredSiteExpression.getValue();
        },
        getPreferredSiteName: function ():String {
          return preferredSiteExpression.getValue();
        },
        getPreferredSite: function ():Object {
          return preferredSite;
        },
        getSiteIdFor: function (content:Content):String {
          return preferredSiteExpression.getValue();
        },
        getSiteFor: function (content:Content):Object {
          return preferredSite;
        },
        getSites: function ():Array {
          return [preferredSite];
        },
        getSite: function (siteId:String):Object {
          return preferredSite;
        }
      };
    };
    //Reset the collection view model
    EditorContextImpl.getInstance().getCollectionViewModel(true);
    RemoteBeanCache.disposeAll();


    var workArea:Object = {};
    workArea['getEntityTabTypes'] = function ():Array {
      return [];
    };
    editorContext['getWorkArea'] = function ():* {
      return workArea;
    };
  }

  override protected function getMockCalls():Array {
    return MOCK_RESPONSES;
  }

  override public function tearDown():void {
    super.tearDown();
    MockAjax.destroyMock();
    RequestCounter.reset();
    RemoteBeanCache.disposeAll();
  }

  protected function makeShopInvalid():Step {
    return new Step("make shop invalid",
            function ():Boolean {
              return true;
            },
            function ():void {
              preferredSiteExpression.setValue("Media");
            });
  }

  protected function makeShopValid():Step {
    return new Step("make shop valid",
            function ():Boolean {
              return true;
            },
            function ():void {
              preferredSiteExpression.setValue("HeliosSiteId");
            });
  }

  protected function loadContentRepository():Step {
    return new Step("load content repository",
            function ():Boolean {
              return true;
            },
            function ():void {
              contentRepository.load();
            });
  }

  protected function waitForContentRepositoryLoaded():Step {
    return new Step("Wait for content repository to be loaded",
            function ():Boolean {
              return contentRepository.isLoaded();
            }
    );
  }

  protected function loadContentTypes():Step {
    return new Step("load content types",
            function ():Boolean {
              return true;
            },
            function ():void {
              contentRepository.getContentTypes().forEach(function (contentType:RemoteBean):void {
                contentType.load();
              });
            });
  }

  protected function waitForContentTypesLoaded():Step {
    return new Step("wait for the content types to be loaded",
            function ():Boolean {
              return  contentRepository.getContentTypes().every(function (contentType:RemoteBean):Boolean {
                        return contentType.isLoaded();
                      }
              );
            });
  }

  public static const MOCK_RESPONSES:Array = [
    {
      "request": { "uri": "upload/config?site=HeliosSiteId", "method": "GET" },
      "response": { "body": {
        }}
    },
    {
      "request": { "uri": "livecontext/store/HeliosSiteId/NO_WS", "method": "GET" },
      "response": { "body": {
        "name": "PerfectChefESite",
        "id": "ibm:///catalog/store/10851",
        "topLevel": [
          {"$Ref": "livecontext/marketing/HeliosSiteId/NO_WS"},
          {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/ROOT"}
        ],
        "vendorName" : "IBM",
        "childrenByName": {
          "root-category": {"displayName":"Product Catalog", "child": {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/ROOT"}},
          "store-marketing": {"displayName":"store-marketing", "child": {"$Ref": "livecontext/marketing/HeliosSiteId/NO_WS"}}
        },
        "storeId": "10851",
        "contracts": {"$Ref": "livecontext/contracts/HeliosSiteId/NO_WS"}
      }}
    },
    {
      "request": { "uri": "livecontext/store/TestSiteId/NO_WS", "method": "GET" },
      "response": { "body": {
        "name": "TestSite",
        "id": "test:///catalog/store/10851",
        "topLevel": [
          {"$Ref": "livecontext/marketing/TestSiteId/NO_WS"},
          {"$Ref": "livecontext/category/TestSiteId/NO_WS/ROOT"}
        ],
        "vendorName" : "test",
        "storeId": "10851"
      }}
    },
    {
      "request": { "uri": "livecontext/marketing/HeliosSiteId/NO_WS", "method": "GET"},
      "response": { "body": {
        "name": "Marketing spots",
        "id": "marketing-perfectchefesite",
        "store": {"$Ref": "livecontext/store/HeliosSiteId/NO_WS"},
        "storeId": "10851",
        "marketingSpots": [
          {
            "$Ref": "livecontext/marketingspot/HeliosSiteId/NO_WS/spot1"
          },
          {
            "$Ref": "livecontext/marketingspot/HeliosSiteId/NO_WS/spot2"
          },
          {
            "$Ref": "livecontext/marketingspot/HeliosSiteId/NO_WS/spot3"
          }
        ],
        "childrenByName": {
          "spot1": {"displayName":"Spot1", "child": {"$Ref": "livecontext/marketingspot/HeliosSiteId/NO_WS/spot1"}},
          "spot2": {"displayName":"Spot2", "child": {"$Ref": "livecontext/marketingspot/HeliosSiteId/NO_WS/spot2"}},
          "spot3": {"displayName":"Spot3", "child": {"$Ref": "livecontext/marketingspot/HeliosSiteId/NO_WS/spot3"}}
        }
      }}
    },
    {
      "request": { "uri": "livecontext/category/HeliosSiteId/catalog/NO_WS/ROOT", "method": "GET"},
      "response": { "body": {
        "id": "ibm:///catalog/category/ROOT",
        "externalId": "ROOT",
        "store": {"$Ref": "livecontext/store/HeliosSiteId/NO_WS"},
        "storeId": "10851",
        "children": [
          {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Apparel"
          },
          {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Grocery"
          },
          {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Home%20Furnishings"
          }
        ],
        "childrenByName": {
          "Grocery": {"displayName":"Grocery", "child": {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Grocery"}},
          "Home%20Furnishings": {"displayName":"Home & Furnishing", "child": {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Home%20Furnishings"}},
          "Apparel": {"displayName":"Apparel", "child": {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Apparel"}}
        },
        "subCategories": [
          {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Apparel"
          },
          {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Grocery"
          },
          {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Home%20Furnishings"
          }
        ],
        "displayName" : "Product Catalog",
        "name" : "Product Catalog",
        "parent" : null,
        "content" : {
          "$Ref": "content/500"
        }
      }}
    },
    {
      "request": { "uri": "livecontext/contracts/HeliosSiteId/NO_WS", "method": "GET"},
      "response": { "body": {
        "id":"contracts-HeliosSiteId",
        "contracts":[
          {"$Ref":"livecontext/contract/HeliosSiteId/NO_WS/4000000000000000507"},
          {"$Ref":"livecontext/contract/HeliosSiteId/NO_WS/4000000000000000508"}
        ]
      }
      }
    },
    {
      "request": { "uri": "livecontext/contract/HeliosSiteId/NO_WS/4000000000000000507", "method": "GET"},
      "response": { "body": {
        "id":"ibm:///catalog/contract/4000000000000000507",
        "name":"Contract for CoreMedia Preview Exterior",
        "externalId":"4000000000000000507",
        "externalTechId":"4000000000000000507"
      }
      }
    },
    {
      "request": { "uri": "livecontext/contract/HeliosSiteId/NO_WS/4000000000000000508", "method": "GET"},
      "response": { "body": {
        "id":"ibm:///catalog/contract/4000000000000000508",
        "name":"Contract for CoreMedia Preview Interior",
        "externalId":"4000000000000000508",
        "externalTechId":"4000000000000000508"
      }
      }
    },
    {
      "request": { "uri": "livecontext/contract/HeliosSiteId/NO_WS/invalidid", "method": "GET"},
      "response": { "code": 404}
    },

    {
      "request": { "uri": "livecontext/store/Media/NO_WS", "method": "GET" },
      "response": { "code": 404}
    },

    {
      "request": { "uri": "livecontext/marketingspot/HeliosSiteId/NO_WS/spot1", "method": "GET" },
      "response": { "body": {
        "name": "spot1",
        "id": "ibm:///catalog/marketingspot/spot1",
        "description": "spot1",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "marketing": {
          "$Ref": "livecontext/marketing/HeliosSiteId/NO_WS"
        }
      }}
    },


    {
      "request": { "uri": "livecontext/marketingspot/HeliosSiteId/NO_WS/spot2", "method": "GET" },
      "response": { "body": {
        "name": "spot2",
        "id": "ibm:///catalog/marketingspot/spot2",
        "description": "spot2",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "marketing": {
          "$Ref": "livecontext/marketing/HeliosSiteId/NO_WS"
        }
      }}
    },


    {
      "request": { "uri": "livecontext/marketingspot/HeliosSiteId/NO_WS/spot3", "method": "GET" },
      "response": { "body": {
        "name": "spot3",
        "id": "ibm:///catalog/marketingspot/spot3",
        "description": "spot3",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "marketing": {
          "$Ref": "livecontext/marketing/HeliosSiteId/NO_WS"
        }
      }}
    },


    {
      "request": { "uri": "livecontext/category/HeliosSiteId/catalog/NO_WS/Grocery", "method": "GET" },
      "response": { "body": {
        "name": "Grocery",
        "id": "ibm:///catalog/category/Grocery",
        "children": [
          {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit"
          },
          {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Vegetables"
          }
        ],
        "childrenByName": {
          "Vegetables": {"displayName":"Vegetables", "child": {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Vegetables"
          }},
          "Fruit": {"displayName":"Fruit", "child": {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit"
          }}
        },
        "externalId": "Grocery",
        "displayName": "Grocery",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "subCategories": [
          {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit"
          },
          {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Vegetables"
          }
        ],
        "parent": {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/ROOT"},
        "content" : null
      }
      }
    },
    {
      "request": { "uri": "livecontext/category/HeliosSiteId/catalog/NO_WS/Vegetables", "method": "GET" },
      "response": { "body": {
        "name": "Vegetables",
        "id": "ibm:///catalog/category/Vegetables",
        "children": [],
        "childrenByName": {}
        },
        "externalId": "Vegetables",
        "displayName": "Vegetables",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "subCategories": [],
        "parent": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Grocery"
        }
      }
    },
    {
      "request": { "uri": "livecontext/category/HeliosSiteId/catalog/NO_WS/Apparel", "method": "GET" },
      "response": { "body": {
        "name": "Apparel",
        "id": "ibm:///catalog/category/Apparel",
        "children": [
          {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Women"}
        ],
        "childrenByName": {
          "Women": {"displayName":"Women", "child": {
                "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Women"
              }}
        },
        "externalId": "Apparel",
        "displayName": "Apparel",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "subCategories": [
          {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Women"}
        ],
        "parent": {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/ROOT"}
      }
      }
    },

    {
      "request":{ "uri":"livecontext/category/HeliosSiteId/catalog/NO_WS/Women", "method":"GET" },
      "response":{ "body":{
        "name": "Women",
        "id": "ibm:///catalog/category/Women",
        "children": [
          {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Dresses"}
        ],
        "childrenByName": {
          "Dresses": {"displayName":"Dresses", "child": {
            "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Dresses"
          }}
        },
        "externalId": "Women",
        "displayName": "Women",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "subCategories": [
          {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Dresses"}
        ],
        "parent": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Apparel"
        }
      }
      }
    },

    {
      "request":{ "uri":"livecontext/category/HeliosSiteId/catalog/NO_WS/Dresses", "method":"GET" },
      "response":{ "body":{
        "name": "Dresses",
        "id": "ibm:///catalog/category/Dresses",
        "children": [
          {"$Ref": "livecontext/product/HeliosSiteId/catalog/NO_WS/AuroraWMDRS-1"}
        ],
        "childrenByName": {
          "AuroraWMDRS-1": {"displayName":"AuroraWMDRS-1", "child": {
            "$Ref": "livecontext/product/HeliosSiteId/catalog/NO_WS/AuroraWMDRS-1"
          }}
        },
        "externalId": "Dresses",
        "displayName": "Dresses",
        "externalTechId": "10006",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "subCategories": [],
        "parent": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Women"
        }
      }
      }
    },

    {
      "request":{ "uri":"livecontext/product/HeliosSiteId/catalog/NO_WS/AuroraWMDRS-1", "method":"GET" },
      "response":{ "body":{
        "name": "Hermitage Ruched Bodice Cocktail Dress",
        "id": "ibm:///catalog/product/AuroraWMDRS-1",
        "externalId": "AuroraWMDRS-1",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "category": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Dresses"
        },
        "thumbnailUrl": "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/women/wcl000_dresses/200x310/wcl000_0028_a_red.jpg"
      }
      }
    },

    {
      "request":{ "uri":"livecontext/sku/HeliosSiteId/catalog/NO_WS/AuroraWMDRS-001", "method":"GET" },
      "response":{ "body":{
        "name": HERMITAGE_RUCHED_BODICE_COCKTAIL_DRESS,
        "id": "ibm:///catalog/sku/AuroraWMDRS-001",
        "externalId": "AuroraWMDRS-001",
        "externalTechId": "10040",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "category": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Dresses"
        },
        "thumbnailUrl": "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/women/wcl000_dresses/200x310/wcl000_0028_a_red.jpg"
      }
      }
    },

    {
      "request":{ "uri":"livecontext/sku/HeliosSiteId/catalog/NO_WS/AuroraWMDRS-002", "method":"GET" },
      "response":{ "body":{
        "name": "Hermitage Ruched Bodice Cocktail Dress",
        "id": "ibm:///catalog/sku/AuroraWMDRS-002",
        "externalId": "AuroraWMDRS-002",
        "externalTechId": "10041",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "category": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Dresses"
        },
        "thumbnailUrl": "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/women/wcl000_dresses/200x310/wcl000_0028_a_red.jpg"
      }
      }
    },

    {
      "request":{ "uri":"livecontext/category/HeliosSiteId/catalog/NO_WS/Home%20Furnishings", "method":"GET" },
      "response":{ "body":{
        "name": "Home & Furnishing",
        "id": "ibm:///catalog/category/Home%20Furnishings",
        "children": [],
        "childrenByName": {},
        "externalId": "Home & Furnishing",
        "displayName": "Home & Furnishing",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "subCategories": [],
        "parent": {"$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/ROOT"}
      }
      }
    },


    {
      "request": { "uri": "livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit", "method": "GET" },
      "response": { "body": {
        "name": "Fruit",
        "id": "ibm:///catalog/category/Fruit",
        "children": [
          {
            "$Ref": "livecontext/product/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID
          },
          {
            "$Ref": "livecontext/product/HeliosSiteId/catalog/NO_WS/GFR033_3302"
          },
          {
            "$Ref": "livecontext/product/HeliosSiteId/catalog/NO_WS/GFR033_3303"
          }
        ],
        "childrenByName": {
          "Oranges": {"displayName":"Oranges", "child": {
            "$Ref": "livecontext/product/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID
          }},
          "Blackberries": {"displayName":"Blackberries", "child": {
            "$Ref": "livecontext/product/HeliosSiteId/catalog/NO_WS/GFR033_3302"
          }},
          "Mangoes": {"displayName":"Mangoes", "child": {
            "$Ref": "livecontext/product/HeliosSiteId/catalog/NO_WS/GFR033_3303"
          }}
        },
        "externalId": "Grocery Fruit",
        "displayName": "Grocery Fruit",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "subCategories": [],
        "parent": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Grocery"
        },
        "content" : {
          "$Ref": "content/700"
        }
      }
      }
    },


    {
      "request": { "uri": "livecontext/product/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID, "method": "GET" },
      "response": { "body": {
        "name": ORANGES_NAME,
        "shortDescription": ORANGES_SHORT_DESC,
        "id": ORANGES_ID,
        "externalId": ORANGES_EXTERNAL_ID,
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "category": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit"
        },
        "thumbnailUrl": ORANGES_IMAGE_URI
      }
      }
    },


    {
      "request": { "uri": "livecontext/sku/HeliosSiteId/catalog/NO_WS/" + ORANGES_SKU_EXTERNAL_ID, "method": "GET" },
      "response": { "body": {
        "name": ORANGES_SKU_NAME,
        "shortDescription": ORANGES_SKU_SHORT_DESC,
        "id": ORANGES_SKU_ID,
        "externalId": ORANGES_SKU_EXTERNAL_ID,
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "category": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit"
        },
        "thumbnailUrl": ORANGES_IMAGE_URI
      }
      }
    },

    {
      "request": { "uri": "livecontext/sku/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID + "02", "method": "GET" },
      "response": { "body": {
        "name": ORANGES_NAME,
        "id": SKU_ID_PREFIX + ORANGES_EXTERNAL_ID + "02",
        "externalId": ORANGES_EXTERNAL_ID + "02",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "category": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit"
        },
        "thumbnailUrl": ORANGES_IMAGE_URI
      }
      }
    },

    {
      "request": { "uri": "livecontext/sku/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID + "03", "method": "GET" },
      "response": { "body": {
        "name": ORANGES_NAME,
        "id": SKU_ID_PREFIX + ORANGES_EXTERNAL_ID + "03",
        "externalId": ORANGES_EXTERNAL_ID + "03",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "category": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit"
        }
      }
      }
    },

    {
      "request": { "uri": "livecontext/product/HeliosSiteId/catalog/NO_WS/" + BABY_SHOES_EXTERNAL_ID, "method": "GET" },
      "response": { "body": {
        "name": "Borsati Orange Baby Shoes",
        "externalId": "BSH016_1605",
        "category": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Boys%20Shoes"
        },
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "externalTechId": "12595",
        "id": "ibm:///catalog/product/BSH016_1605"
      }
      }
    },
    {
      "request":{ "uri":"livecontext/product/HeliosSiteId/catalog/NO_WS/xmp1", "method":"GET" },
      "response":{ "body":{
        "name": "XMP 1",
        "id": "ibm:///catalog/product/xmp1",
        "externalId": "xmp1",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "category": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Dresses"
        },
        "thumbnailUrl": "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/women/wcl000_dresses/200x310/wcl000_0028_a_red.jpg"
      }
      }
    },
    {
      "request":{ "uri":"livecontext/product/HeliosSiteId/catalog/NO_WS/xmp2", "method":"GET" },
      "response":{ "body":{
        "name": "XMP 2",
        "id": "ibm:///catalog/product/xmp2",
        "externalId": "xmp2",
        "store": {
          "$Ref": "livecontext/store/HeliosSiteId/NO_WS"
        },
        "category": {
          "$Ref": "livecontext/category/HeliosSiteId/catalog/NO_WS/Dresses"
        },
        "thumbnailUrl": "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/women/wcl000_dresses/200x310/wcl000_0028_a_red.jpg"
      }
      }
    },
    {
      "request": { "uri": "livecontext/product/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID + 404, "method": "GET" },
      "response": { "code": 404 }
    },

    {
      "request": { "uri": "livecontext/product/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID + 503, "method": "GET" },
      "response": { "code": 503 }
    },

    {
      "request": { "uri": "livecontext/search/HeliosSiteId?query=*&searchType=Product&siteId=HeliosSiteId&workspaceId=NO_WS&limit=-1&includeSubfolders=true&includeSubtypes=true", "method": "GET" },
      "response": { "body": {
        "hits": [],
        "total": 0
      }
      }
    },

    {
      "request": { "uri": "livecontext/search/HeliosSiteId?query=Oranges&searchType=Product&siteId=HeliosSiteId&workspaceId=NO_WS&limit=-1", "method": "GET" },
      "response": { "body": {
        "hits": [
          {
            "$Ref": "livecontext/product/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID
          },
          {
            "$Ref": "livecontext/product/HeliosSiteId/catalog/NO_WS/BSH016_1605"
          }
        ],
        "total": 2
      }
      }
    },

    {
      "request":{ "uri":"livecontext/search/HeliosSiteId?workspaceId=NO_WS&siteId=HeliosSiteId&category=10006&catalogAlias=catalog&query=AuroraWMDRS-1&searchType=ProductVariant&limit=-1", "method":"GET" },
      "response":{ "body":{
        "hits": [
          {
            "$Ref": "livecontext/sku/HeliosSiteId/catalog/NO_WS/AuroraWMDRS-001"
          },
          {
            "$Ref": "livecontext/sku/HeliosSiteId/catalog/NO_WS/AuroraWMDRS-002"
          }
        ],
        "total": 2
      }
      }
    },

    {
      "request": { "uri": "livecontext/search/HeliosSiteId?query=Oranges&searchType=ProductVariant&siteId=HeliosSiteId&workspaceId=NO_WS&limit=-1", "method": "GET" },
      "response": { "body": {
        "hits": [
          {
            "$Ref": "livecontext/sku/HeliosSiteId/catalog/NO_WS/" + ORANGES_SKU_EXTERNAL_ID
          },
          {
            "$Ref": "livecontext/sku/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID + "02"
          },
          {
            "$Ref": "livecontext/sku/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID + "03"
          }
        ],
        "total": 3
      }
      }
    },


    {
      "request": { "uri": "content", "method": "GET" },
      "response": { "body": {
        "root": {
          "$Ref": "content/1"
        },
        "baseHomeFolder": {
          "$Ref": "content/5"
        },
        "referrersWithDescriptorUriTemplate" : "content/{id:[0-9]+}/referrersWithDescriptor/{contentType:[^/]*}/{propertyName:[^/]*}",
        "referrersUriTemplate" : "content/{id:[0-9]+}/referrers",
        "contentTypes": [
          {
            "name": "CMExternalPage",
            "description": null,
            "directDescriptors": [
              {
                "$CapPropertyDescriptor": {
                  "name": "externalId",
                  "type": "String",
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "externalUriPath",
                  "type": "String",
                  "atomic": true
                }
              }
            ],
            "id": "coremedia:///cap/contenttype/CMExternalPage",
            "parent": {
              "$Ref": "content/type/CMHasContexts"
            },
            "instancesBean": {
              "$Ref": "content/type/CMExternalPage/instances"
            },
            "abstract": false,
            "$Bean": "content/type/CMExternalPage"
          },
          {
            "name": "CMHasContexts",
            "description": null,
            "directDescriptors": [
              {
                "$CapPropertyDescriptor": {
                  "name": "master",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 1,
                  "linkType": {
                    "$Ref": "content/type/CMHasContexts"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "contexts",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 2147483647,
                  "linkType": {
                    "$Ref": "content/type/CMContext"
                  },
                  "collection": true,
                  "atomic": false
                }
              }
            ],
            "id": "coremedia:///cap/contenttype/CMHasContexts",
            "parent": {
              "$Ref": "content/type/CMLinkable"
            },
            "instancesBean": {
              "$Ref": "content/type/CMHasContexts/instances"
            },
            "abstract": true,
            "$Bean": "content/type/CMHasContexts"
          },
          {
            "name": "CMLinkable",
            "description": null,
            "directDescriptors": [
              {
                "$CapPropertyDescriptor": {
                  "name": "master",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 1,
                  "linkType": {
                    "$Ref": "content/type/CMLinkable"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "keywords",
                  "type": "STRING",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "length": 1024,
                  "encodedLength": 3072,
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "viewtype",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 1,
                  "linkType": {
                    "$Ref": "content/type/CMViewtype"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "localSettings",
                  "type": "STRUCT",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "linkedSettings",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 2147483647,
                  "linkType": {
                    "$Ref": "content/type/CMSettings"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "validFrom",
                  "type": "DATE",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "validTo",
                  "type": "DATE",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "segment",
                  "type": "STRING",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "length": 64,
                  "encodedLength": 192,
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "title",
                  "type": "STRING",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "length": 512,
                  "encodedLength": 1536,
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "subjectTaxonomy",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 2147483647,
                  "linkType": {
                    "$Ref": "content/type/CMTaxonomy"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "locationTaxonomy",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 2147483647,
                  "linkType": {
                    "$Ref": "content/type/CMLocTaxonomy"
                  },
                  "collection": true,
                  "atomic": false
                }
              }
            ],
            "id": "coremedia:///cap/contenttype/CMLinkable",
            "parent": {
              "$Ref": "content/type/CMLocalized"
            },
            "instancesBean": {
              "$Ref": "content/type/CMLinkable/instances"
            },
            "abstract": true,
            "$Bean": "content/type/CMLinkable"
          },
          {
            "name": "CMLocalized",
            "description": null,
            "directDescriptors": [
              {
                "$CapPropertyDescriptor": {
                  "name": "locale",
                  "type": "STRING",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "length": 64,
                  "encodedLength": 192,
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "master",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 1,
                  "linkType": {
                    "$Ref": "content/type/CMLocalized"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "masterVersion",
                  "type": "INTEGER",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "collection": false,
                  "atomic": true
                }
              }
            ],
            "id": "coremedia:///cap/contenttype/CMLocalized",
            "parent": {
              "$Ref": "content/type/CMObject"
            },
            "instancesBean": {
              "$Ref": "content/type/CMLocalized/instances"
            },
            "abstract": true,
            "$Bean": "content/type/CMLocalized"
          },
          {
            "name": "CMMarketingSpot",
            "description": null,
            "directDescriptors": [
              {
                "$CapPropertyDescriptor": {
                  "name": "master",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 1,
                  "linkType": {
                    "$Ref": "content/type/CMMarketingSpot"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "externalId",
                  "type": "STRING",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "length": 256,
                  "encodedLength": 768,
                  "collection": false,
                  "atomic": true
                }
              }
            ],
            "id": "coremedia:///cap/contenttype/CMMarketingSpot",
            "parent": {
              "$Ref": "content/type/CMTeasable"
            },
            "instancesBean": {
              "$Ref": "content/type/CMMarketingSpot/instances"
            },
            "abstract": false,
            "$Bean": "content/type/CMMarketingSpot"
          },
          {
            "name": "CMObject",
            "description": null,
            "directDescriptors": [],
            "id": "coremedia:///cap/contenttype/CMObject",
            "parent": {
              "$Ref": "content/type/Document_"
            },
            "instancesBean": {
              "$Ref": "content/type/CMObject/instances"
            },
            "abstract": true,
            "$Bean": "content/type/CMObject"
          },
          {
            "name": "CMProductTeaser",
            "description": null,
            "directDescriptors": [
              {
                "$CapPropertyDescriptor": {
                  "name": "master",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 1,
                  "linkType": {
                    "$Ref": "content/type/CMProductTeaser"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "externalId",
                  "type": "STRING",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "length": 64,
                  "encodedLength": 192,
                  "collection": false,
                  "atomic": true
                }
              }
            ],
            "id": "coremedia:///cap/contenttype/CMProductTeaser",
            "parent": {
              "$Ref": "content/type/CMTeasable"
            },
            "instancesBean": {
              "$Ref": "content/type/CMProductTeaser/instances"
            },
            "abstract": false,
            "$Bean": "content/type/CMProductTeaser"
          },
          {
            "name": "CMPicture",
            "description": null,
            "directDescriptors": [
              {
                "$CapPropertyDescriptor": {
                  "name": "master",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 1,
                  "linkType": {
                    "$Ref": "content/type/CMPicture"
                  },
                  "collection": true,
                  "atomic": false
                }
              }
            ],
            "id": "coremedia:///cap/contenttype/CMPicture",
            "parent": {
              "$Ref": "content/type/CMTeasable"
            },
            "instancesBean": {
              "$Ref": "content/type/CMPicture/instances"
            },
            "abstract": false,
            "$Bean": "content/type/CMPicture"
          },
          {
            "abstract": false,
            "id": "coremedia:///cap/contenttype/CMSite",
            "directDescriptors": [{"$CapPropertyDescriptor":{
              "name" : "root",
              "type" : "LINK",
              "linkType" : {
                "$Ref" : "content/type/CMNavigation"
              },
              "atomic" : false,
              "collection" : true,
              "minCardinality" : 0,
              "maxCardinality" : 1
            }}],
            "description": "???CMSite",
            "$Bean": "content/type/CMSite",
            "name": "CMSite",
            "instancesBean": {"$Ref": "content/type/CMSite/instances"},
            "parent": {"$Ref": "content/type/CMLocalized"}
          },
          {
            "name": "CMTeasable",
            "description": null,
            "directDescriptors": [
              {
                "$CapPropertyDescriptor": {
                  "name": "master",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 1,
                  "linkType": {
                    "$Ref": "content/type/CMTeasable"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "teaserTitle",
                  "type": "STRING",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "length": 512,
                  "encodedLength": 1536,
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "teaserText",
                  "type": "MARKUP",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "grammar": "coremedia-richtext-1.0",
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "detailText",
                  "type": "MARKUP",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "grammar": "coremedia-richtext-1.0",
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "pictures",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 2147483647,
                  "linkType": {
                    "$Ref": "content/type/CMPicture"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "notSearchable",
                  "type": "INTEGER",
                  "minCardinality": 1,
                  "maxCardinality": 1,
                  "collection": false,
                  "atomic": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "related",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 2147483647,
                  "linkType": {
                    "$Ref": "content/type/CMTeasable"
                  },
                  "collection": true,
                  "atomic": false
                }
              }
            ],
            "id": "coremedia:///cap/contenttype/CMTeasable",
            "parent": {
              "$Ref": "content/type/CMHasContexts"
            },
            "instancesBean": {
              "$Ref": "content/type/CMTeasable/instances"
            },
            "abstract": true,
            "$Bean": "content/type/CMTeasable"
          },
          {
            "name": "CMTeaser",
            "description": null,
            "directDescriptors": [
              {
                "$CapPropertyDescriptor": {
                  "name": "master",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 1,
                  "linkType": {
                    "$Ref": "content/type/CMTeaser"
                  },
                  "collection": true,
                  "atomic": false
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "target",
                  "type": "LINK",
                  "minCardinality": 0,
                  "maxCardinality": 1,
                  "linkType": {
                    "$Ref": "content/type/CMLinkable"
                  },
                  "collection": true,
                  "atomic": false
                }
              }
            ],
            "id": "coremedia:///cap/contenttype/CMTeaser",
            "parent": {
              "$Ref": "content/type/CMTeasable"
            },
            "instancesBean": {
              "$Ref": "content/type/CMTeaser/instances"
            },
            "abstract": false,
            "$Bean": "content/type/CMTeaser"
          },
          {
            "name": "Content_",
            "description": "The root content type",
            "directDescriptors": [],
            "id": "coremedia:///cap/contenttype/Content_",
            "parent": null,
            "instancesBean": {
              "$Ref": "content/type/Content_/instances"
            },
            "abstract": true,
            "$Bean": "content/type/Content_"
          },
          {
            "name": "Document_",
            "description": "The document content type",
            "directDescriptors": [],
            "id": "coremedia:///cap/contenttype/Document_",
            "parent": {
              "$Ref": "content/type/Content_"
            },
            "instancesBean": {
              "$Ref": "content/type/Document_/instances"
            },
            "abstract": true,
            "$Bean": "content/type/Document_"
          },
          {
            "name": "Folder_",
            "description": "The folder content type",
            "directDescriptors": [],
            "id": "coremedia:///cap/contenttype/Folder_",
            "parent": {
              "$Ref": "content/type/Content_"
            },
            "instancesBean": {
              "$Ref": "content/type/Folder_/instances"
            },
            "abstract": false,
            "$Bean": "content/type/Folder_"
          }
        ],
        "contentContentType": {
          "$Ref": "content/type/Content_"
        },
        "folderContentType": {
          "$Ref": "content/type/Folder_"
        },
        "documentContentType": {
          "$Ref": "content/type/Document_"
        },
        "bulkRightsUri": "content/bulk/rights",
        "bulkCopyUri": "content/bulk/copy",
        "bulkMoveUri": "content/bulk/move",
        "bulkCheckInUri": "content/bulk/checkIn",
        "bulkRevertUri": "content/bulk/revert",
        "bulkApproveUri": "content/bulk/approve",
        "bulkDisapproveUri": "content/bulk/disapprove",
        "bulkPublishUri": "content/bulk/publish",
        "bulkApprovePublishUri": "content/bulk/approvePublish",
        "bulkWithdrawUri": "content/bulk/withdraw",
        "bulkDeleteUri": "content/bulk/delete",
        "bulkUndeleteUri": "content/bulk/undelete",
        "queryUri": "content/list",
        "searchUri": "content/search",
        "searchSuggestionsUri": "content/suggestions",
        "previewControllerUriPattern": "http://localhost:40081/blueprint/servlet/preview?id={0}",
        "previewUrlWhitelist": [],
        "timeZones": [
          "Europe/Berlin",
          "Europe/London",
          "America/New_York",
          "America/Los_Angeles"
        ],
        "defaultTimeZone": "Europe/Berlin",
        "availableLocalesContentPath": "/Settings/Options/Settings/LocaleSettings",
        "availableLocalesPropertyPath": "settings.availableLocales",
        "useStrictWorkflow": false
      }      }
    },
    {
      "request": { "uri": "content/1", "method": "GET" },
      "response": { "body": {
        "name": "root",
        "id": "coremedia:///cap/content/1",
        "properties": {
        },
        "type": {
          "$Ref": "content/type/Folder_"
        }
      }
      }
    },
    {
      "request": { "uri": "content/5", "method": "GET" },
      "response": { "body": {
        "name": "Home",
        "id": "coremedia:///cap/content/5",
        "properties": {
        },
        "type": {
          "$Ref": "content/type/Folder_"
        }
      }
      }
    },
    {
      "request": { "uri": "content/100", "method": "GET" },
      "response": { "body": {
        "name": "Product Teaser",
        "id": "coremedia:///cap/content/100",
        "properties": {
          "externalId": ORANGES_ID,
          "teaserText": null
        },
        "type": {
          "$Ref": "content/type/CMProductTeaser"
        }
      }
      }
    },
    {
      "request": { "uri": "content/100/properties/teaserText", "method": "GET" },
      "response": {
        "contentType": "text/xml",
        "headers": { "content-encoding": "UTF-8" },
        "body": null
      }
    },
    {
      "request": {"uri": "content/100/referrersWithDescriptor/CMSite/root", "method": "GET"},
      "response": {
        "body": []
      }
    },
    {
      "request": { "uri": "content/type/CMProductTeaser", "method": "GET" },
      "response": { "body": {
        "name": "CMProductTeaser",
        "description": null,
        "directDescriptors": [
          {
            "$CapPropertyDescriptor": {
              "name": "master",
              "type": "LINK",
              "minCardinality": 0,
              "maxCardinality": 1,
              "linkType": {
                "$Ref": "content/type/CMProductTeaser"
              },
              "collection": true,
              "atomic": false
            }
          },
          {
            "$CapPropertyDescriptor": {
              "name": "externalId",
              "type": "STRING",
              "minCardinality": 1,
              "maxCardinality": 1,
              "length": 64,
              "encodedLength": 192,
              "collection": false,
              "atomic": true
            }
          }
        ],
        "id": "coremedia:///cap/contenttype/CMProductTeaser",
        "parent": {
          "$Ref": "content/type/CMTeasable"
        },
        "instancesBean": {
          "$Ref": "content/type/CMProductTeaser/instances"
        },
        "abstract": false
      }      }
    },
    {
      "request": { "uri": "content/100", "method": "PUT",
        "contentType": "application/json",
        "body": {
          "properties": {"externalId": ORANGES_ID}
        }
      },
      "response": { "code": 200 }
    },


    {
      "request": { "uri": "content/100", "method": "PUT",
        "contentType": "application/json",
        "body": {
          "properties": {"externalId": ORANGES_ID + "503"}
        }
      },
      "response": { "code": 200 }
    },


    {
      "request": { "uri": "content/100", "method": "PUT",
        "contentType": "application/json",
        "body": {
          "properties": {"externalId": ORANGES_ID + "404"}
        }
      },
      "response": { "code": 200 }
    },

    {
      "request": { "uri": "content/100", "method": "PUT",
        "contentType": "application/json",
        "body": {
          "properties": {"externalId": ORANGES_SKU_ID}
        }
      },
      "response": { "code": 200 }
    },

    {
      "request": { "uri": "content/100", "method": "PUT",
        "contentType": "application/json",
        "body": {
          "properties": {"externalId": null}
        }
      },
      "response": { "code": 200 }
    },

    {
      "request": { "uri": "content/101", "method": "GET" },
      "response": { "body": {
        "name": "Marketing Spot",
        "id": "coremedia:///cap/content/101",
        "properties": {
          "externalId": ""
        }
      }
      }
    },

    {
      "request": { "uri": "content/101", "method": "PUT",
        "contentType": "application/json",
        "body": {
          "properties": {"externalId": MARKETING_SPOT_ID}
        }
      },
      "response": { "code": 200 }
    },

    {
      "request": { "uri": "content/101", "method": "PUT",
        "contentType": "application/json",
        "body": {
          "properties": {"externalId": MARKETING_SPOT_ID + "503"}
        }
      },
      "response": { "code": 200 }
    },


    {
      "request": { "uri": "content/101", "method": "PUT",
        "contentType": "application/json",
        "body": {
          "properties": {"externalId": MARKETING_SPOT_ID + "404"}
        }
      },
      "response": { "code": 200 }
    },
    {
      "request": { "uri": "content/200", "method": "GET" },
      "response": { "body": {
        "name": "CM Picture",
        "id": "coremedia:///cap/content/200",
        "properties": {
          "localSettings": {
            "$Ref": "content/200/structs/localSettings"
          }
        },
        "type": {
          "$Ref": "content/type/CMPicture"
        }
      }
      }
    },
    {
      "request": { "uri": "content/200/structs/localSettings", "method": "GET" },
      "response": {
        "contentType": "application/json",
        "body": {
          "$Struct": [
            {
              "$CapPropertyDescriptor": {
                "name": "commerce", "type": "STRUCT", "minCardinality": 1, "maxCardinality": 1, "atomic": true, "collection": false
              }
            }
          ],
          "commerce": {
            "$Struct": [
              {
                "$CapPropertyDescriptor": {
                  "name": "inherit", "type": "BOOLEAN", "atomic":true, "collection":false, "minCardinality": 1, "maxCardinality": 1
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "references", "type": "STRING", "minCardinality": 0, "maxCardinality": 2147483647,
                  "length": 2147483647, "encodedLength": 2147483647, "atomic": false, "collection": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "originReferences", "type": "STRING", "minCardinality": 0, "maxCardinality": 2147483647,
                  "length": 2147483647, "encodedLength": 2147483647, "atomic": false, "collection": true
                }
              }

            ],
            "inherit": false,
            "references": [ORANGES_ID, ORANGES_SKU_ID],
            "originReferences": [PRODUCT1_FROM_XMP_ID, PRODUCT2_FROM_XMP_ID]
          }
        }
      }
    },
    {
      "request": { "uri": "content/200/structs/localSettings", "method": "PUT",
        "contentType": "application/json"
      },
      "response": { "code": 200 }
    },
    {
      "request": { "uri": "content/202", "method": "GET" },
      "response": { "body": {
        "name": "CM Picture",
        "id": "coremedia:///cap/content/202",
        "properties": {
          "localSettings": {
            "$Ref": "content/202/structs/localSettings"
          }
        },
        "type": {
          "$Ref": "content/type/CMPicture"
        }
      }
      }
    },
    {
      "request": { "uri": "content/202/structs/localSettings", "method": "GET" },
      "response": {
        "contentType": "application/json",
        "body": {
          "$Struct": [
            {
              "$CapPropertyDescriptor": {
                "name": "commerce", "type": "STRUCT", "minCardinality": 1, "maxCardinality": 1, "atomic": true, "collection": false
              }
            }
          ],
          "commerce": {
            "$Struct": [
              {
                "$CapPropertyDescriptor": {
                  "name": "inherit", "type": "BOOLEAN", "atomic":true, "collection":false, "minCardinality": 1, "maxCardinality": 1
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "references", "type": "STRING", "minCardinality": 0, "maxCardinality": 2147483647,
                  "length": 2147483647, "encodedLength": 2147483647, "atomic": false, "collection": true
                }
              },
              {
                "$CapPropertyDescriptor": {
                  "name": "originReferences", "type": "STRING", "minCardinality": 0, "maxCardinality": 2147483647,
                  "length": 2147483647, "encodedLength": 2147483647, "atomic": false, "collection": true
                }
              }

            ],
            "inherit": true,
            "references": [PRODUCT1_FROM_XMP_ID, PRODUCT2_FROM_XMP_ID],
            "originReferences": [PRODUCT1_FROM_XMP_ID, PRODUCT2_FROM_XMP_ID]
          }
        }
      }
    },
    {
      "request": { "uri": "content/202/structs/localSettings", "method": "PUT",
        "contentType": "application/json"
      },
      "response": { "code": 200 }
    },
    {
      "request": { "uri": "content/300", "method": "GET" },
      "response": { "body": {
        "name": "Persona",
        "id": "coremedia:///cap/content/300",
        "properties": {
          "externalId": "",
          "teaserText": null
        },
        "type": {
          "$Ref": "content/type/CMUserProfile"
        }
      }
      }
    },
    {
      "request": { "uri": "content/400", "method": "GET" },
      "response": { "body": {
        "name": "Site Root Document",
        "id": "coremedia:///cap/content/400",
        "properties": {
          "externalId": "",
          "teaserText": null
        },
        "type": {
          "$Ref": "content/type/Document_"
        }
      }
      }
    },
    {
      "request": { "uri": "content/500", "method": "GET" },
      "response": { "body": {
        "name": "Root Category Document",
        "id": "coremedia:///cap/content/500",
        "properties": {
          "externalId": "ibm:///catalog/category/ROOT"
        },
        "type": {
          "$Ref": "content/type/Document_"
        }
      }
      }
    },
    {
      "request": { "uri": "content/600", "method": "GET" },
      "response": { "body": {
        "name": "Grocery Augmented Category",
        "id": "coremedia:///cap/content/600",
        "properties": {
          "externalId": "ibm:///catalog/category/Grocery"
        },
        "type": {
          "$Ref": "content/type/Document_"
        }
      }
      }
    },
    {
      "request": { "uri": "content/700", "method": "GET" },
      "response": { "body": {
        "name": "Fruit Augmented Category",
        "id": "coremedia:///cap/content/700",
        "properties": {
          "externalId": "ibm:///catalog/category/Fruit"
        },
        "type": {
          "$Ref": "content/type/Document_"
        }
      }
      }
    },
    {
      "request": { "uri": "content/1/rights;for=user_1", "method": "GET" },
      "response": { "body": [
        {
          "type": {
            "$Ref" : "content/type/CMMarketingSpot"
          },
          "rights": "RMDAPS"
        },
        {
          "type": {
            "$Ref" : "content/type/CMProductTeaser"
          },
          "rights": "RMDAPS"
        },
        {
          "type": {
            "$Ref" : "content/type/CMPicture"
          },
          "rights": "RMDAPS"
        },
        {
          "type":{
            "$Ref":"content/type/CMTeaser"
          },
          "rights":"R"
        }
      ]}
    }


  ];
}
}

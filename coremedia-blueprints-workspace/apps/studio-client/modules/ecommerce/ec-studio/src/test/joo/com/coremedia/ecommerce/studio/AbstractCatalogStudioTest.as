package com.coremedia.ecommerce.studio {
import com.coremedia.cap.content.impl.ContentImpl;
import com.coremedia.cap.content.impl.ContentStructRemoteBeanImpl;
import com.coremedia.cap.content.impl.ContentTypeImpl;
import com.coremedia.ecommerce.studio.model.*;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.BeanFactoryImpl;

public class AbstractCatalogStudioTest extends AbstractCatalogTest {

  protected static const HELIOS_SITE_ID:String = "HeliosSiteId";
  protected static const STORE_CONTEXT_ID:String = HELIOS_SITE_ID + "/NO_WS";
  protected static const STORE_CATALOG_CONTEXT_ID:String = HELIOS_SITE_ID + "/catalog/NO_WS";
  protected static const STORE_ID:String = "livecontext/store/" + STORE_CONTEXT_ID;
  protected static const ROOT_CATEGORY_ID:String = "livecontext/category/" + STORE_CATALOG_CONTEXT_ID + "/ROOT";
  protected static const MARKETING_ID:String = "livecontext/marketing/" + STORE_CONTEXT_ID;
  protected static const TOP_CATEGORY_ID:String = "livecontext/category/" + STORE_CATALOG_CONTEXT_ID + "/Grocery";
  protected static const LEAF_CATEGORY_ID:String = "livecontext/category/" + STORE_CATALOG_CONTEXT_ID + "/Fruit";
  protected static const STORE_NAME:String = "PerfectChefESite";
  protected static const TOP_CATEGORY_EXTERNAL_ID:String = "Grocery";
  protected static const LEAF_CATEGORY_EXTERNAL_ID:String = "Grocery Fruit";

  protected static const SITE_ROOT_DOCUMENT_ID:String = "content/400";
  protected static const ROOT_CATEGORY_DOCUMENT_ID:String = "content/500";
  protected static const TOP_CATEGORY_DOCUMENT_ID:String = "content/600";
  protected static const LEAF_CATEGORY_DOCUMENT_ID:String = "content/700";

  override public function setUp():void {
    super.setUp();
    resetCatalogHelper();
    BeanFactoryImpl(beanFactory).registerRemoteBeanClasses(ContentTypeImpl, ContentImpl, ContentStructRemoteBeanImpl,
            StoreImpl, CategoryImpl, MarketingImpl, ContractsImpl, ContractImpl, ProductImpl, MarketingSpotImpl, ProductVariantImpl);
  }
}
}
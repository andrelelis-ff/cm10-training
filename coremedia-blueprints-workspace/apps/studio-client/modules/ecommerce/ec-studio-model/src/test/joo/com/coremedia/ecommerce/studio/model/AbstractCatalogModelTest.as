package com.coremedia.ecommerce.studio.model {
import com.coremedia.cap.content.impl.ContentImpl;
import com.coremedia.cap.content.impl.ContentStructRemoteBeanImpl;
import com.coremedia.cap.content.impl.ContentTypeImpl;
import com.coremedia.ecommerce.studio.AbstractCatalogTest;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.BeanFactoryImpl;

public class AbstractCatalogModelTest extends AbstractCatalogTest {

  override public function setUp():void {
    super.setUp();
    BeanFactoryImpl(beanFactory).registerRemoteBeanClasses(ContentTypeImpl, ContentImpl, ContentStructRemoteBeanImpl,
            StoreImpl, CategoryImpl, MarketingImpl, ProductImpl, MarketingSpotImpl, ProductVariantImpl);
  }
}
}
package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.InvalidContextException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Currency;

import static org.mockito.Mockito.mock;

public class StoreContextHelperExceptionFormattingTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testInvalidContextExceptionFormattingForLocale() {
    thrown.expect(InvalidContextException.class);
    thrown.expectMessage("invalid commerce context: Locale missing in store context ("
            + "storeId: 4711, "
            + "storeName: toko, "
            + "catalogId: 0815, "
            + "currency: USD, "
            + "locale: null, "
            + "workspaceId: Optional.empty)");

    StoreContextImpl context = buildStoreContext()
            .withStoreId("4711")
            .withStoreName("toko")
            .withCatalogId(CatalogId.of("0815"))
            .withCurrency(Currency.getInstance("USD"))
            .withLocale(null)
            .build();

    StoreContextHelper.getLocale(context);
  }

  private IbmStoreContextBuilder buildStoreContext() {
    CommerceConnection connection = mock(CommerceConnection.class);
    String siteId = "awesome-site";

    return IbmStoreContextBuilder.from(connection, siteId);
  }
}

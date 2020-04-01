package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.catalog.WcCatalogWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.storeinfo.StoreInfoService;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SwitchableHoverflyExtension.class)
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_WcLanguageMappingServiceIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@WebAppConfiguration
@ContextConfiguration(classes = {IbmServiceTestBase.LocalConfig.class, AbstractWrapperServiceTestCase.LocalConfig.class})
@ActiveProfiles({IbmServiceTestBase.LocalConfig.PROFILE})
public class WcLanguageMappingServiceIT extends AbstractWrapperServiceTestCase {

  @Inject
  private WcCatalogWrapperService testling;

  @Inject
  @Named("commerce:wcs1")
  private CommerceConnection connection;

  @Inject
  protected StoreInfoService storeInfoService;

  private StoreContext storeContext;

  @BeforeEach
  void setup() {
    storeInfoService.getWcsVersion().ifPresent(testConfig::setWcsVersion);

    storeContext = testConfig.getStoreContext(connection);
  }

  @Test
  void testLanguageMapping() {
    assertThat(testling.getLanguageMapping()).isNotNull();

    CatalogAlias catalogAlias = CatalogAliasTranslationService.DEFAULT_CATALOG_ALIAS;
    CatalogId catalogId = testling.findCatalogId(catalogAlias, storeContext)
            .orElseThrow(() -> new IllegalStateException("Catalog alias must map to a catalog ID."));

    Currency currency = Currency.getInstance("USD");
    String userName = "forUser";

    Map<String, String[]> parametersMap;

    parametersMap = testling.buildParameterMap()
            .withCatalogId(catalogId)
            .withCurrency(currency)
            .withLanguageId(new Locale("en", "EN"))
            .withUserName(userName)
            .build();
    assertThat(parametersMap.get("langId")[0]).isEqualTo("-1");

    parametersMap = testling.buildParameterMap()
            .withCatalogId(catalogId)
            .withCurrency(currency)
            .withLanguageId(new Locale("en"))
            .withUserName(userName)
            .build();
    assertThat(parametersMap.get("langId")[0]).isEqualTo("-1");

    parametersMap = testling.buildParameterMap()
            .withCatalogId(catalogId)
            .withCurrency(currency)
            .withLanguageId(new Locale("de"))
            .withUserName(userName)
            .build();
    assertThat(parametersMap.get("langId")[0]).isEqualTo("-3");

    parametersMap = testling.buildParameterMap()
            .withCatalogId(catalogId)
            .withCurrency(currency)
            .withLanguageId(new Locale("xx"))
            .withUserName(userName)
            .build();
    assertThat(parametersMap.get("langId")[0]).isEqualTo("-1");
  }
}

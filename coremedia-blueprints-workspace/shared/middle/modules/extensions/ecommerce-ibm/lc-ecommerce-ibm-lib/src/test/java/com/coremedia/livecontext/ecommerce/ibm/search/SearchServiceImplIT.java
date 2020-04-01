package com.coremedia.livecontext.ecommerce.ibm.search;

import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.search.SuggestionResult;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_7;
import static org.junit.Assert.assertFalse;

@ExtendWith({SwitchableHoverflyExtension.class})
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_SearchServiceImplIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@ContextConfiguration(classes = IbmServiceTestBase.LocalConfig.class)
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
public class SearchServiceImplIT extends IbmServiceTestBase {

  @Inject
  SearchServiceImpl testling;

  @BeforeEach
  @Override
  public void setup() {
    super.setup();
  }

  @Test
  void testGetAutocompleteSuggestions() {
    if (StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_7)) {
      return;
    }

    List<SuggestionResult> suggestions = testling.getAutocompleteSuggestions("dres", storeContext);
    assertFalse(suggestions.isEmpty());
  }
}

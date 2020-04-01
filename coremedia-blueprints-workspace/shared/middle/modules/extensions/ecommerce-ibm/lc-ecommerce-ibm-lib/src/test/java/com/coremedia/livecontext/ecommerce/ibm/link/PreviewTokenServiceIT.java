package com.coremedia.livecontext.ecommerce.ibm.link;

import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static com.coremedia.blueprint.lc.test.HoverflyTestHelper.useTapes;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = IbmServiceTestBase.LocalConfig.class)
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
class PreviewTokenServiceIT extends IbmServiceTestBase {

  @Inject
  private PreviewTokenService testling;

  @Test
  void testGetPreviewToken() {
    if (useTapes()) {
      return;
    }

    String previewToken = testling.getPreviewToken(storeContext);
    assertThat(previewToken).isNotNull();
    assertThat(previewToken).isNotEmpty();
  }
}

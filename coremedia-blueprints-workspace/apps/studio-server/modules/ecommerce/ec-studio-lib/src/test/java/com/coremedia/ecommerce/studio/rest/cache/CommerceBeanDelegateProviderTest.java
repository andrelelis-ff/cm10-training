package com.coremedia.ecommerce.studio.rest.cache;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommerceBeanDelegateProviderTest {

  @Test
  public void encodePartNumber() {
    assertThat(CommerceBeanDelegateProvider.encodePartNumber("I am the /partnumb/er"))
            .isEqualTo("I%20am%20the%20/partnumb/er");
  }
}
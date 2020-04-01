package com.coremedia.livecontext.validation;

import com.coremedia.livecontext.contentbeans.CMExternalChannel;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.InvalidIdException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CMExternalChannelValidatorTest {

  @Mock
  private CMExternalChannel channel;

  @Mock
  private Category category;

  @InjectMocks
  private CMExternalChannelValidator testling;

  @Test
  public void validCatalogRootCategory() {
    when(channel.isCatalogRoot()).thenReturn(true);
    assertThat(testling.validate(channel)).isTrue();
  }

  @Test
  public void validCatalogCategory() {
    String reference = "test:///catalog/category/TEST";
    when(channel.getExternalId()).thenReturn(reference);
    when(channel.getCategory()).thenReturn(category);

    assertThat(testling.validate(channel)).isTrue();
  }

  @Test
  public void invalidCatalogCategory() {
    when(channel.getExternalId()).thenReturn("externalId");
    when(channel.getCategory()).thenThrow(new InvalidIdException("invalid"));
    // category does not exist
    assertThat(testling.validate(channel)).isFalse();
  }

  @Test
  public void emptyExternalId() {
    // external id is empty
    assertThat(testling.validate(channel)).isFalse();
  }

}
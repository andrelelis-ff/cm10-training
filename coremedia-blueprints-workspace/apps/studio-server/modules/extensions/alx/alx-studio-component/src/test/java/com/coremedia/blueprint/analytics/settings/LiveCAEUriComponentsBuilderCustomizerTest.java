package com.coremedia.blueprint.analytics.settings;

import com.coremedia.rest.cap.content.UrlModifier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LiveCAEUriComponentsBuilderCustomizerTest {

  private LiveCAEUriComponentsBuilderCustomizer customizer;
  private UriComponentsBuilder uriComponentsBuilder;

  @Before
  public void setup() {
    customizer = new LiveCAEUriComponentsBuilderCustomizer();
    customizer.setLiveCaeHost("{0}.coremedia.com");
    customizer.setLiveCaePort(8080);
    customizer.setLiveCaeScheme("http");

    uriComponentsBuilder = UriComponentsBuilder.newInstance();
  }

  @Test
  public void testNoCustomization() throws Exception {
    customizer.fillIn(uriComponentsBuilder);
    assertEquals("http://{0}.coremedia.com:8080", uriComponentsBuilder.build().toString());
  }

  @Test
  public void testCustomizeTenant() throws Exception {
    final UrlModifier mock = mock(UrlModifier.class);

    customizer.setUrlModifiers(Collections.singletonList(mock));
    when(mock.processUrl(anyString())).thenReturn("hiho");
    customizer.fillIn(uriComponentsBuilder);

    assertEquals("http://hiho:8080", uriComponentsBuilder.build().toString());
  }
}

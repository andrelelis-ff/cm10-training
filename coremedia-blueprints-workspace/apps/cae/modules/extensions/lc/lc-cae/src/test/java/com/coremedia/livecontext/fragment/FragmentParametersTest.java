package com.coremedia.livecontext.fragment;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class FragmentParametersTest {

  @Test
  public void testRequestUrl() {
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/10851/en-US/params;placement=header;view=test;parameter=abc";
    FragmentParameters parameters = FragmentParametersFactory.create(url);
    assertEquals(parameters.getStoreId(), "10851");
    assertEquals(parameters.getLocale(), Locale.forLanguageTag("en-US"));
    assertEquals(parameters.getPlacement(), "header");
    assertEquals(parameters.getView(), "test");
    assertEquals(parameters.getParameter(), "abc");
  }
}

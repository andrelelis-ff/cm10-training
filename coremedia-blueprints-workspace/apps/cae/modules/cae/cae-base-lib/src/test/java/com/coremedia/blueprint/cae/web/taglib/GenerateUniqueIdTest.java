package com.coremedia.blueprint.cae.web.taglib;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPageContext;

import static com.coremedia.blueprint.cae.web.taglib.GenerateUniqueId.ATTR_NAME_GENERATE_UNIQUE_ID;
import static org.junit.Assert.assertEquals;

/**
 * Test {@link GenerateUniqueId}
 */
public class GenerateUniqueIdTest {

  MockPageContext context;
  MockHttpServletRequest request;
  private int idInRequest;

  @Before
  public void setUp() throws Exception {

    request = new MockHttpServletRequest();
    context = new MockPageContext(null, request);
    request.setAttribute(ATTR_NAME_GENERATE_UNIQUE_ID, 0);
  }

  @Test
  public void testWithoutPrefix() {

    String generatedId = GenerateUniqueId.generateId(context);

    int expectedIdInRequest = idInRequest +1;
    //since #generateId is called without prefix, ID will not contain one.
    String expectedGeneratedId = String.valueOf(idInRequest +1);

    assertEquals("ID was not generated correctly.", expectedGeneratedId, generatedId);
    assertEquals("ID was not changed in request.", expectedIdInRequest, request.getAttribute(ATTR_NAME_GENERATE_UNIQUE_ID));

  }

  @Test
  public void testWithPrefix() {

    String prefix = "myPrefix";
    String generatedId = GenerateUniqueId.generateId(prefix, context);

    int expectedIdInRequest = idInRequest +1;
    //since #generateId is called without prefix, ID will not contain one.
    String expectedGeneratedId = prefix + (idInRequest +1);

    assertEquals("ID was not generated correctly.", expectedGeneratedId, generatedId);
    assertEquals("ID was not changed in request.", expectedIdInRequest, request.getAttribute(ATTR_NAME_GENERATE_UNIQUE_ID));

  }


}

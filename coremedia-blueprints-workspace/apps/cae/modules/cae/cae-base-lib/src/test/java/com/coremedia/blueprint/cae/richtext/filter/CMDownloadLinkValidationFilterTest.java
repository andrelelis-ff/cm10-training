package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.blueprint.common.contentbeans.CMDownload;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.id.IdProvider;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CMDownloadLinkValidationFilterTest {
  private static final String DIV_NS = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";
  private static final String CONTENT_LINK = "<a xlink:href=\"coremedia:///cap/content/242\">linktext</a>";

  @Mock
  private IdProvider idProvider;

  @Mock
  private ValidationService<Object> validationService;

  @Mock
  private CMDownload cmDownload;

  @Mock
  private IdProvider.UnknownId unknownId;

  private CMDownloadLinkValidationFilter testling;

  @Before
  public void setUp() throws Exception {
    testling = new CMDownloadLinkValidationFilter();
    testling.setIdProvider(idProvider);
    testling.setValidationService(validationService);

  }

  @Test
  public void testDoNothing() throws Exception {
    String tail = "<p>hello</p></div>";
    String result = doFilter(DIV_NS + tail);
    // Cannot rely on the order of the namespace declarations,
    // exclude the opening <div> from the check.
    assertTrue(result.endsWith(tail));
  }

  @Test
  public void testCreateLinkCmDownload() throws Exception {
    when(idProvider.parseId("coremedia:///cap/content/242")).thenReturn(cmDownload);
    when(validationService.validate(cmDownload)).thenReturn(true);
    when(cmDownload.getData()).thenReturn(mock(CapBlobRef.class));

    String tail = "<p>" + CONTENT_LINK + "</p></div>";
    String result = doFilter(DIV_NS + tail);
    assertTrue(result.endsWith(tail));
  }

  @Test
  public void testRemoveLinkForMissingBlob() throws Exception {
    when(idProvider.parseId("coremedia:///cap/content/242")).thenReturn(cmDownload);
    when(validationService.validate(cmDownload)).thenReturn(true);
    when(cmDownload.getData()).thenReturn(null);

    String tail = "<p>" + CONTENT_LINK + "</p></div>";
    String result = doFilter(DIV_NS + tail);
    assertFalse(result.contains(CONTENT_LINK));
    assertTrue(result.endsWith("<p>linktext</p></div>"));
  }

  @Test
  public void testNotACMDownload() throws Exception {
    CMObject content = mock(CMObject.class);
    when(idProvider.parseId("coremedia:///cap/content/242")).thenReturn(content);
    when(validationService.validate(content)).thenReturn(true);

    String tail = "<p>" + CONTENT_LINK + "</p></div>";
    String result = doFilter(DIV_NS + tail);
    assertTrue(result.endsWith(tail));
  }

// --- internal ---------------------------------------------------

  private String doFilter(String text) {
    Markup markup = MarkupFactory.fromString(text);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    markup.writeOn(Collections.singletonList(testling), bos);
    return bosToString(bos);
  }

  private static String bosToString(ByteArrayOutputStream bos) {
    try {
      return bos.toString("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new Error("UTF-8 must be supported!");
    }
  }
}
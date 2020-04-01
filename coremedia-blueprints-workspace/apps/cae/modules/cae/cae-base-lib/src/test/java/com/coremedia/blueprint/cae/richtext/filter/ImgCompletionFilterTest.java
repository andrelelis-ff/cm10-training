package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.id.IdProvider;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImgCompletionFilterTest {
  private static final String BLOB_ID = "coremedia:///cap/resources/2982/data";

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private IdProvider idProvider;

  @Mock
  private Content blobContent;

  @Mock
  private CMPicture picBean;

  @InjectMocks
  private ImgCompletionFilter testling;

  // --- setup ------------------------------------------------------

  @Before
  public void setup() {
    when(idProvider.parseId(IdHelper.parseContentIdFromBlobId(BLOB_ID))).thenReturn(blobContent);
    when(contentBeanFactory.createBeanFor(blobContent, ContentBean.class)).thenReturn(picBean);
    when(picBean.getAlt()).thenReturn("foo");
  }


  // --- Tests ------------------------------------------------------

  @Test
  public void testAddAlt() throws SAXException {
    AttributesImpl atts = new AttributesImpl();
    atts.addAttribute("http://www.w3.org/1999/xlink", "href", "xlink:href", "CDATA", BLOB_ID);
    atts.addAttribute("", null, "alt", "CDATA", "");
    AltChecker altChecker = new AltChecker("foo");
    testling.setContentHandler(altChecker);
    testling.startElement("", null, "img", atts);
    assertTrue("", altChecker.invoked);
  }

  @Test
  public void testPreserveExistingAlt() throws SAXException {
    AttributesImpl atts = new AttributesImpl();
    atts.addAttribute("http://www.w3.org/1999/xlink", "href", "xlink:href", "CDATA", BLOB_ID);
    atts.addAttribute("", null, "alt", "CDATA", "bar");
    AltChecker altChecker = new AltChecker("bar");
    testling.setContentHandler(altChecker);
    testling.startElement("", null, "img", atts);
    assertTrue("", altChecker.invoked);
  }

  @Test
  public void testOnlyImgTags() throws SAXException {
    AttributesImpl atts = new AttributesImpl();
    atts.addAttribute("http://www.w3.org/1999/xlink", "href", "xlink:href", "CDATA", BLOB_ID);
    AltChecker altChecker = new AltChecker(null);
    testling.setContentHandler(altChecker);
    testling.startElement("", null, "href", atts);
    assertTrue("", altChecker.invoked);
  }

  @Test
  public void testIgnoreInvalidAttributes() throws SAXException {
    AttributesImpl atts = new AttributesImpl();
    atts.addAttribute("http://www.w3.org/1999/xlink", "href", "xlink:href", "CDATA", "");
    AltChecker altChecker = new AltChecker(null);
    testling.setContentHandler(altChecker);
    testling.startElement("", null, "img", atts);
    assertTrue("", altChecker.invoked);
  }

  // --- internal ---------------------------------------------------

  private static class AltChecker extends DefaultHandler {
    private String expectedAltValue;
    private boolean invoked = false;

    public AltChecker(String expectedAltValue) {
      this.expectedAltValue = expectedAltValue;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      invoked = true;
      String actualAltValue = attributes.getValue("alt");
      if (expectedAltValue==null) {
        assertNull("non null alt value", actualAltValue);
      } else {
        assertEquals("wrong alt value", expectedAltValue, actualAltValue);
      }
    }
  }
}

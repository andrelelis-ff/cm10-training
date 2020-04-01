package com.coremedia.ecommerce.common;

import com.adobe.xmp.properties.XMPPropertyInfo;
import com.coremedia.common.util.Predicate;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertThat;

public class XmpImageMetadataExtractorTest {
  private static final String IPTC_XMP_EXT_NS = "http://iptc.org/std/Iptc4xmpExt/2008-02-29/";
  private static final String ARTWORK_NODE = "ArtworkOrObject";
  private static final String INVENTORY_INFO = "Iptc4xmpExt:AOSourceInvNo";

  private XmpImageMetadataExtractor extractor;

  @Before
  public void setUp() throws Exception {
    extractor = XmpImageMetadataExtractor.builder().atNameSpace(IPTC_XMP_EXT_NS).atProperty(ARTWORK_NODE).filteredBy(new Predicate<XMPPropertyInfo>() {
      @Override
      public boolean include(XMPPropertyInfo o) {
        return !Strings.isNullOrEmpty(o.getValue()) && o.getPath().endsWith(INVENTORY_INFO);
      }
    }).build();
  }

  @Test
  public void extractsSourceInvNo() throws Exception {
    URL resource = Resources.getResource(this.getClass(), "image-with-xmp-product-reference.jpg");
    Map<String, String> apply = getMetadataMap(resource);
    assertThat(apply, Matchers.allOf(
            Matchers.hasEntry(Matchers.endsWith(INVENTORY_INFO), Matchers.equalTo("PC_RED_DRESS")),
            Matchers.hasEntry(Matchers.endsWith(INVENTORY_INFO), Matchers.equalTo("PC_GREEN_DRESS"))
    ));
  }

  @Test
  public void handleNoXmpGracefully() throws Exception {
    URL resource = Resources.getResource(this.getClass(), "image-no-xmp.jpg");
    Map<String, String> apply = getMetadataMap(resource);
    assertThat(apply.entrySet(), Matchers.<Map.Entry<String, String>>empty());
  }

  @NonNull
  private Map<String, String> getMetadataMap(URL resource) throws ImageProcessingException, IOException, URISyntaxException {
    Objects.requireNonNull(resource, "Unable to find resource.");
    Metadata metadata = ImageMetadataReader.readMetadata(new File(resource.toURI()));
    return extractor.apply(metadata);
  }

}

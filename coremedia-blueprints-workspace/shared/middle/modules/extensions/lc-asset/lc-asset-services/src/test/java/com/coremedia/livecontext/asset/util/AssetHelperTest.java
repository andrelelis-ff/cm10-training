package com.coremedia.livecontext.asset.util;

import com.coremedia.cap.util.StructUtil;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_COMMERCE;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_INHERIT;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_ORIGIN_REFERENCES;
import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_REFERENCES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AssetHelperTest.AssetTestConfiguration.class)
public class AssetHelperTest {

  static final String CONTENT_XML_URI = "classpath:/com/coremedia/livecontext/ecommerce/asset/assetRepository.xml";

  static final List<String> AB_LIST = Arrays.asList("vendor:///catalog/product/A", "vendor:///catalog/sku/B");
  static final List<String> ACD_LIST = Arrays.asList("vendor:///catalog/product/A", "vendor:///catalog/sku/C", "vendor:///catalog/sku/D");
  static final List<String> EF_LIST = Arrays.asList("vendor:///catalog/product/E", "vendor:///catalog/sku/F");
  static final List<String> EMPTY_LIST = Collections.emptyList();

  @Inject
  private ContentRepository contentRepository;

  @Inject
  private StructService structService;

  @Inject
  private AssetReadSettingsHelper assetReadSettingsHelper;

  private AssetHelper testling;

  private Struct originStruct;
  private Content content;

  @Import(XmlRepoConfiguration.class)
  @Configuration
  @ImportResource(
          value = {
                  "classpath:/com/coremedia/cap/common/xml/uapi-xml-services.xml",
                  "classpath:/framework/spring/lc-asset-helpers.xml"
          },
          reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class
  )
  static class AssetTestConfiguration {
    @Bean
    XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_XML_URI);
    }
  }

  @Before
  public void setup() {
    testling = new AssetHelper();
    testling.setContentRepository(contentRepository);
    testling.setAssetReadSettingsHelper(assetReadSettingsHelper);

    content = contentRepository.getContent("2");
    originStruct = content.getStruct("localSettings");

    setupOriginStruct(false, null, null);
  }

  public void setupContentForLocalSettingsNull() {
    content = contentRepository.getContent("4");
    originStruct = content.getStruct("localSettings");
  }

  @Test
  public void updateStructWithEmptyReferences(){
    //prepare struct with arbitrary existing settings
    Struct existingSettings = originStruct.builder().declareString("myIndependentProperty", 256, "moin moin").build();
    content.checkOut();
    content.set("localSettings", existingSettings);
    content.checkIn();

    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, EMPTY_LIST);
    Struct commerceStruct = StructUtil.getSubstruct(updatedStruct, NAME_COMMERCE);
    assertTrue(commerceStruct != null);

    //check if existing settings still exists
    assertTrue(StructUtil.getString(updatedStruct, "myIndependentProperty").equals("moin moin"));
  }

  @Test
  public void updateStructForExternalIdsCase05Test() {
    List<String> newOriginCatalogObjects = EMPTY_LIST;

    resetOriginStruct();

    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);
    Struct commerceStruct = StructUtil.getSubstruct(updatedStruct, NAME_COMMERCE);

    assertTrue(commerceStruct == null);
  }

  @Test
  public void updateStructForExternalIdsCase06Test() {
    List<String> newOriginCatalogObjects = AB_LIST;

    resetOriginStruct();

    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);
    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);

    assertNotNull(commerceStruct);
    assertEquals(true, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_REFERENCES));

    assertNotNull(StructUtil.getSubstruct(updatedStruct, "focusArea"));
    assertEquals(8, updatedStruct.getProperties().size());
  }

  @Test
  public void updateStructForExternalIdsNoLocalSettingsCase06Test() {
    setupContentForLocalSettingsNull();
    List<String> newOriginCatalogObjects = AB_LIST;

    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);
    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);

    assertNotNull(commerceStruct);
    assertEquals(true, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_REFERENCES));

    assertEquals(1, updatedStruct.getProperties().size());
  }

  @Test
  public void updateStructForExternalIdsCase07Test() {
    Boolean inherit = true;
    List<String> oldOriginCatalogObjects = AB_LIST;
    List<String> oldCatalogObjects = AB_LIST;
    List<String> newOriginCatalogObjects = ACD_LIST;

    setupOriginStruct(inherit, oldOriginCatalogObjects, oldCatalogObjects);
    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);
    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);

    assertNotNull(commerceStruct);
    assertEquals(true, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertNotNull(StructUtil.getSubstruct(updatedStruct, "focusArea"));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_REFERENCES));
    assertNotNull(StructUtil.getSubstruct(updatedStruct, "focusArea"));
    assertEquals(8, updatedStruct.getProperties().size());
  }

  @Test
  public void updateStructForExternalIdsCase08Test() {
    Boolean inherit = true;
    List<String> oldOriginCatalogObjects = AB_LIST;
    List<String> oldCatalogObjects = AB_LIST;
    List<String> newOriginCatalogObjects = EMPTY_LIST;

    setupOriginStruct(inherit, oldOriginCatalogObjects, oldCatalogObjects);
    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);

    Struct commerceStruct = StructUtil.getSubstruct(updatedStruct, NAME_COMMERCE);
    assertTrue(commerceStruct == null);
    assertNotNull(StructUtil.getSubstruct(updatedStruct, "focusArea"));
    assertEquals(7, updatedStruct.getProperties().size());
  }

  @Test
  public void updateStructForExternalIdsCase09Test() {
    Boolean inherit = false;
    List<String> oldOriginCatalogObjects = AB_LIST;
    List<String> oldCatalogObjects = ACD_LIST;
    List<String> newOriginCatalogObjects = EF_LIST;

    setupOriginStruct(inherit, oldOriginCatalogObjects, oldCatalogObjects);
    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);

    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);
    assertNotNull(commerceStruct);
    assertEquals(false, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(oldCatalogObjects, commerceStruct.getStrings(NAME_REFERENCES));
    assertNotNull(StructUtil.getSubstruct(updatedStruct, "focusArea"));
    assertEquals(8, updatedStruct.getProperties().size());
  }

  @Test
  public void updateStructForExternalIdsCase10Test() {
    Boolean inherit = false;
    List<String> oldOriginCatalogObjects = AB_LIST;
    List<String> oldCatalogObjects = ACD_LIST;
    List<String> newOriginCatalogObjects = EMPTY_LIST;

    setupOriginStruct(inherit, oldOriginCatalogObjects, oldCatalogObjects);
    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);

    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);
    assertNotNull(commerceStruct);
    assertEquals(false, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(oldCatalogObjects, commerceStruct.getStrings(NAME_REFERENCES));
    assertNotNull(StructUtil.getSubstruct(updatedStruct, "focusArea"));
    assertEquals(8, updatedStruct.getProperties().size());
  }

  @Test
  public void updateStructForExternalIdsCase11Test() {
    Boolean inherit = false;
    List<String> oldOriginCatalogObjects = AB_LIST;
    List<String> oldCatalogObjects = EMPTY_LIST;
    List<String> newOriginCatalogObjects = EF_LIST;

    setupOriginStruct(inherit, oldOriginCatalogObjects, oldCatalogObjects);
    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);

    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);
    assertNotNull(commerceStruct);
    assertEquals(true, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_REFERENCES));
    assertNotNull(StructUtil.getSubstruct(updatedStruct, "focusArea"));
    assertEquals(8, updatedStruct.getProperties().size());
  }

  @Test
  public void updateStructForExternalIdsCase12Test() {
    Boolean inherit = false;
    List<String> oldOriginCatalogObjects = EMPTY_LIST;
    List<String> oldCatalogObjects = ACD_LIST;
    List<String> newOriginCatalogObjects = EF_LIST;

    setupOriginStruct(inherit, oldOriginCatalogObjects, oldCatalogObjects);
    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);

    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);
    assertNotNull(commerceStruct);
    assertEquals(false, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(oldCatalogObjects, commerceStruct.getStrings(NAME_REFERENCES));
    assertNotNull(StructUtil.getSubstruct(updatedStruct, "focusArea"));
    assertEquals(8, updatedStruct.getProperties().size());
  }

  @Test
  public void updateStructForExternalIdsCase13Test() {
    Boolean inherit = false;
    List<String> oldOriginCatalogObjects = EMPTY_LIST;
    List<String> oldCatalogObjects = ACD_LIST;
    List<String> newOriginCatalogObjects = EF_LIST;

    setupOriginStruct(inherit, oldOriginCatalogObjects, oldCatalogObjects);
    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);

    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);
    assertNotNull(commerceStruct);
    assertEquals(false, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(oldCatalogObjects, commerceStruct.getStrings(NAME_REFERENCES));
    assertNotNull(StructUtil.getSubstruct(updatedStruct, "focusArea"));
    assertEquals(8, updatedStruct.getProperties().size());
  }

  @Test
  public void updateStructForExternalIdsCase14Test() {
    Boolean inherit = false;
    List<String> oldOriginCatalogObjects = AB_LIST;
    List<String> oldCatalogObjects = EMPTY_LIST;
    List<String> newOriginCatalogObjects = EMPTY_LIST;

    setupOriginStruct(inherit, oldOriginCatalogObjects, oldCatalogObjects);
    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);

    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);
    assertNotNull(commerceStruct);
    assertEquals(false, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(EMPTY_LIST, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(EMPTY_LIST, commerceStruct.getStrings(NAME_REFERENCES));
    assertNotNull(StructUtil.getSubstruct(updatedStruct, "focusArea"));
    assertEquals(8, updatedStruct.getProperties().size());
  }

  @Test
  public void updateStructForExternalIdsCase15Test() {
    Boolean inherit = false;
    List<String> oldOriginCatalogObjects = EMPTY_LIST;
    List<String> oldCatalogObjects = EMPTY_LIST;
    List<String> newOriginCatalogObjects = EF_LIST;

    setupOriginStruct(inherit, oldOriginCatalogObjects, oldCatalogObjects);
    Struct updatedStruct = testling.updateCMPictureForExternalIds(content, newOriginCatalogObjects);

    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);
    assertNotNull(commerceStruct);
    assertEquals(true, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(newOriginCatalogObjects, commerceStruct.getStrings(NAME_REFERENCES));
    assertEquals(8, updatedStruct.getProperties().size());
  }

  @Test
  public void updateCMPictureOnBlobDeleteTest() {
    Boolean inherit = true;
    List<String> oldOriginCatalogObjects = AB_LIST;
    List<String> oldCatalogObjects = EF_LIST;

    setupOriginStruct(inherit, oldOriginCatalogObjects, oldCatalogObjects);
    Struct updatedStruct = testling.updateCMPictureOnBlobDelete(content);
    Struct commerceStruct = updatedStruct.getStruct(NAME_COMMERCE);

    assertNotNull(updatedStruct);
    assertEquals(false, commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(Collections.<String>emptyList(), commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(Collections.<String>emptyList(), commerceStruct.getStrings(NAME_REFERENCES));
    assertEquals(8, updatedStruct.getProperties().size());

    resetOriginStruct();

    updatedStruct = testling.updateCMPictureOnBlobDelete(content);
    commerceStruct = StructUtil.getSubstruct(updatedStruct, NAME_COMMERCE);

    assertNull(commerceStruct);
    assertEquals(7, updatedStruct.getProperties().size());

    resetOriginStruct();

    setupOriginStruct(false, oldOriginCatalogObjects, oldCatalogObjects);
    updatedStruct = testling.updateCMPictureOnBlobDelete(content);
    commerceStruct = StructUtil.getSubstruct(updatedStruct, NAME_COMMERCE);

    assertNotNull(commerceStruct);
    assertFalse(commerceStruct.getBoolean(NAME_INHERIT));
    assertEquals(oldOriginCatalogObjects, commerceStruct.getStrings(NAME_ORIGIN_REFERENCES));
    assertEquals(oldCatalogObjects, commerceStruct.getStrings(NAME_REFERENCES));
    assertEquals(8, updatedStruct.getProperties().size());
  }

  /**
   * Returns a struct with an empty commerce substruct with the given parameters
   *
   * @param inherit              The inherit value of the commerce substruct
   * @param originCatalogObjects The catalog object list oroginated from the picture data (XMP catalog objects) to be
   *                             saved in the commerce substruct
   * @param catalogObjects       The current catalog objects parameter of the commerce substruct
   */
  private void setupOriginStruct(boolean inherit, List<String> originCatalogObjects, List<String> catalogObjects) {
    StructBuilder originStructBuilder = originStruct.builder();

    if (StructUtil.getSubstruct(originStruct, NAME_COMMERCE) != null) {
      originStructBuilder = originStructBuilder.remove(NAME_COMMERCE); // step 1 of clear struct
      originStruct = originStructBuilder.build();// step 2 of clear struct
    }

    Struct commerceStruct = structService.createStructBuilder().build();
    StructBuilder commerceStructBuilder = commerceStruct.builder();
    commerceStructBuilder = commerceStructBuilder.declareBoolean(NAME_INHERIT, inherit);
    commerceStructBuilder = commerceStructBuilder.declareStrings(NAME_ORIGIN_REFERENCES, Integer.MAX_VALUE,
            originCatalogObjects);
    commerceStructBuilder = commerceStructBuilder.declareStrings(NAME_REFERENCES, Integer.MAX_VALUE, catalogObjects);
    commerceStruct = commerceStructBuilder.build();

    originStruct = originStructBuilder.declareStruct(NAME_COMMERCE, commerceStruct).build();

    content.checkOut();
    content.set("localSettings", originStruct);
    content.checkIn();
  }

  /**
   * Removes the commerce substruct from the origin struct
   */
  private void resetOriginStruct() {
    StructBuilder originStructBuilder = originStruct.builder();

    if (StructUtil.getSubstruct(originStruct, NAME_COMMERCE) != null) {
      originStructBuilder = originStructBuilder.remove(NAME_COMMERCE); // step 1 of clear struct
      originStruct = originStructBuilder.build();// step 2 of clear struct
    }

    content.checkOut();
    content.set("localSettings", originStruct);
    content.checkIn();
  }
}

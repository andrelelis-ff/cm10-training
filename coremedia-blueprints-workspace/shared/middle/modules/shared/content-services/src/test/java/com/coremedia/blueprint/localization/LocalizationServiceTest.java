package com.coremedia.blueprint.localization;

import com.coremedia.blueprint.localization.configuration.LocalizationServiceConfiguration;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LocalizationServiceTest.LocalConfig.class)
@ActiveProfiles(LocalizationServiceTest.LocalConfig.PROFILE)
public class LocalizationServiceTest {
  private static final Locale HAMBURG_LOCALE = new Locale("de", "DE", "hamburg");
  private static final Locale OTHER_LOCALE = new Locale("lv");

  @Configuration
  @Import({XmlRepoConfiguration.class, LocalizationServiceConfiguration.class})
  @ImportResource(
          value = {"classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml"},
          reader = ResourceAwareXmlBeanDefinitionReader.class)
  @Profile(LocalConfig.PROFILE)
  public static class LocalConfig {
    static final String PROFILE = "LocalizationServiceTest";
    private static final String CONTENT = "classpath:com/coremedia/blueprint/localization/content.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return XmlUapiConfig.builder()
              .withContent(CONTENT)
              .withContentTypes("classpath:com/coremedia/blueprint/localization/test-doctypes.xml")
              .build();
    }
  }

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentRepository contentRepository;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Resource(name="localizationService")
  private LocalizationService testling;

  @Before
  public void checkWiring() {
    assumeTrue("localizationService not found in LocalizationServiceConfiguration, these tests will fail.", testling!=null);
  }


  // --- contract ---------------------------------------------------

  @Test
  public void testSingleBundle() {
    Struct resources = testling.resources(content(2), null);
    assertEquals("str1fromBundle2", resources.get("str1"));
  }

  @Test
  public void testVariantFallbackRoot() {
    Struct resources = testling.resources(content(12), null);
    assertEquals("fixandfinal", resources.get("rootonly"));
    assertEquals("override", resources.get("str"));
    assertNull(resources.get("strdedehamburg"));
    assertNull(resources.get("strdede"));
    assertNull(resources.get("strde"));
    assertNull(resources.get("strfr"));
  }

  @Test
  public void testVariantFallbackInner() {
    Struct resources = testling.resources(content(12), new Locale("de", "DE"));
    assertEquals("fixandfinal", resources.get("rootonly"));
    assertEquals("overridedede", resources.get("str"));
    assertNull(resources.get("strdedehamburg"));
    assertEquals("strdede", resources.get("strdede"));
    assertEquals("strde", resources.get("strde"));
    assertNull(resources.get("strfr"));
  }

  @Test
  public void testVariantFallbackLeaf() {
    Struct resources = testling.resources(content(12), HAMBURG_LOCALE);
    assertEquals("fixandfinal", resources.get("rootonly"));
    assertEquals("overridededehamburg", resources.get("str"));
    assertEquals("strdedehamburg", resources.get("strdedehamburg"));
    assertEquals("strdede", resources.get("strdede"));
    assertEquals("strde", resources.get("strde"));
    assertNull(resources.get("strfr"));
  }

  @Test
  public void testUnknownLocaleWithoutExplicitFallbacks() {
    Struct resources = testling.resources(content(10), OTHER_LOCALE);
    assertNull(resources.get("stren"));
  }

  @Test
  public void testUnknownLocaleWitExplicitFallback() {
    Struct resources = testling.resources(contents(10), OTHER_LOCALE, contents(14));
    assertEquals("stren", resources.get("stren"));
  }

  @Test
  public void testPrecedences() {
    Struct resources = testling.resources(contents(12, 22), HAMBURG_LOCALE);

    // for hits with identical locales, the order of the given bundles rules.
    assertEquals("right", resources.get("byOrder"));

    // Locales first:
    // A better matching locale rules over the order of the given bundles.
    assertEquals("right", resources.get("localeOverOrder"));

    // Locales first:
    // A better matching locale rules over a shorter master path.
    // (In case of reasonably master-linked variants this case is not relevant.
    // It matters if there are variants like de, de_DE and de_DE_hamburg, and
    // the latter has en as master.)
    assertEquals("right", resources.get("languageOverMaster"));

    // If no matching locale has a hit, a shorter master path rules over
    // the order of the given bundles.
    assertEquals("right", resources.get("masterOverOrder"));
  }

  @Test
  public void testHierarchizeNatural() {
    Content folder = contentRepository.createSubfolders(contentRepository.getRoot(), "hierachizeNatural");
    Content foo = mkBundle(folder, "");
    Content foo_en = mkBundle(folder, "_en");
    Content foo_de = mkBundle(folder, "_de");
    Content foo_de_DE = mkBundle(folder, "_de_DE");
    testling.hierarchizeResourceBundles(foo);

    assertEquals("en", foo_en.getString("locale"));
    assertEquals("de", foo_de.getString("locale"));
    assertEquals("de-DE", foo_de_DE.getString("locale"));

    assertNull(foo.getLink("master"));
    assertEquals(foo, foo_en.getLink("master"));
    assertEquals(foo, foo_de.getLink("master"));
    assertEquals(foo_de, foo_de_DE.getLink("master"));
  }

  /**
   * Usecase: There is no actual root bundle, some concrete language is
   * declared as root and overrules the locale logic.
   */
  @Test
  public void testHierarchizeArbitrary() {
    Content folder = contentRepository.createSubfolders(contentRepository.getRoot(), "hierachizeNArbitrary");
    Content foo_en = mkBundle(folder, "_en");
    Content foo_de = mkBundle(folder, "_de");
    Content foo_de_DE = mkBundle(folder, "_de_DE");
    testling.hierarchizeResourceBundles(foo_en);

    assertNull(foo_en.getLink("master"));
    assertEquals(foo_en, foo_de.getLink("master"));
    assertEquals(foo_de, foo_de_DE.getLink("master"));
  }


  // --- internal helper tests --------------------------------------

  // These helped me trouble shooting for the contract tests.
  // If the internal methods change, adapt or delete these tests.

  @Test
  public void testVariantsMaps() {
    Collection<Content> contentCollection = contents(4, 24, 8, 2, 12, 22, 10, 26, 26, 4);
    List<Map<Locale, Content>> maps = testling.variantsMaps(contentCollection);
    assertEquals(3, maps.size());

    assertEquals(6, maps.get(0).size());
    assertEquals(content(6), maps.get(0).get(new Locale("de", "DE")));

    assertEquals(4, maps.get(1).size());
    assertEquals(content(20), maps.get(1).get(HAMBURG_LOCALE));

    assertEquals(1, maps.get(2).size());
    assertEquals(content(2), maps.get(2).get(new Locale("")));
  }

  @Test
  public void testFallback() {
    Collection<Content> contentCollection = contents(6, 20);
    List<Map<Locale, Content>> maps = testling.variantsMaps(contentCollection);

    // Check some preconditions for the actual fallback test.
    // If this fails, it is worth being extracted to test of its own.
    assertEquals(2, maps.size());
    assertEquals(6, maps.get(0).size());
    assertEquals(4, maps.get(1).size());

    List<Content> fallback = testling.localizationFallback(HAMBURG_LOCALE, maps);
    int[] expected = new int[] {4, 20, 6, 22, 8, 24, 14, 26, 10};
    compare(expected, fallback);
  }


  // --- internal ---------------------------------------------------

  private void compare(int[] expected, List<Content> actual) {
    assertEquals(expected.length, actual.size());
    for (int i=0; i<expected.length; ++i) {
      assertEquals(expected[i], IdHelper.parseContentId(actual.get(i).getId()));
    }
  }

  private Collection<Content> contents(int... ids) {
    Collection<Content> result = new ArrayList<>();
    for (int id : ids) {
      result.add(content(id));
    }
    return result;
  }

  private Content content(int id) {
    return contentRepository.getContent(IdHelper.formatContentId(id));
  }

  private Content mkBundle(Content folder, String locale) {
    return contentRepository.createChild(folder, "foo"+locale+".properties", "CMResourceBundle", Collections.emptyMap());
  }
}

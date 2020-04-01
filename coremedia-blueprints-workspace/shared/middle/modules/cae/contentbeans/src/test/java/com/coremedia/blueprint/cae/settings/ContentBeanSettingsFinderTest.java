package com.coremedia.blueprint.cae.settings;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.objectserver.dataviews.DataViewHelper;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ContentBeanSettingsFinderTest.CMLinkableBeanSettingsFinderTestConfiguration .class)
public class ContentBeanSettingsFinderTest {
  private static final String CONTENT_REPOSITORY_URL = "classpath:/com/coremedia/blueprint/cae/settings/settings.xml";

  @Inject
  private SettingsService settingsService;
  @Inject
  private ContentRepository contentRepository;
  @Inject
  private ContentBeanFactory contentBeanFactory;
  @Inject
  private DataViewFactory dataViewFactory;

  private CMLinkable linkable;


  // --- Setup ------------------------------------------------------

  @Before
  public void setup() {
    linkable = contentbeanFor(124);
  }


  // --- Tests ------------------------------------------------------

  @Test
  public void testSimpleDelegation() {
    String localValue = settingsService.setting("stringProperty", String.class, linkable);
    assertEquals("unexpected first bean value", "testString124", localValue);
  }

  @Test
  public void testContentBeanLink() {
    CMLinkable target = settingsService.setting("linkProperty", CMLinkable.class, linkable);
    assertNotNull("No linkable", target);
    assertEquals("unexpected linkable", 124, target.getContentId());
  }

  @Test
  public void testContentBeanLinkList() {
    List<? extends CMLinkable> target = settingsService.settingAsList("linkListProperty", CMLinkable.class, linkable);
    assertNotNull("No linkable", target);
    assertFalse("empty", target.isEmpty());
    assertEquals("unexpected linkable", 124, target.get(0).getContentId());
  }

  @Test
  public void testStructAsMap() {
    Map<String, Object> value = settingsService.settingAsMap("structProperty", String.class, Object.class, linkable);
    assertNotNull("No map", value);
  }


  @Test
  public void testDataview() {
    CMLinkable contentBean = contentbeanFor(2);
    CMLinkable cbLink = settingsService.setting("link", CMLinkable.class, contentBean);
    assertFalse(DataViewHelper.isDataView(cbLink));

    CMLinkable dataview = dataviewFor(2);
    CMLinkable dvLink = settingsService.setting("link", CMLinkable.class, dataview);
    assertTrue(DataViewHelper.isDataView(dvLink));
  }

  @Test
  public void testLinklistDataviews() {
    CMLinkable dataview = dataviewFor(2);
    assertDataviews(settingsService.settingAsList("linkList", CMLinkable.class, dataview), 2);
  }

  @Test
  public void testNestedSettingDataview() {
    CMLinkable contentBean = contentbeanFor(2);
    CMLinkable cbLink = settingsService.nestedSetting(Arrays.asList("struct", "link"), CMLinkable.class, contentBean);
    assertFalse(DataViewHelper.isDataView(cbLink));

    CMLinkable dataview = dataviewFor(2);
    CMLinkable dvLink = settingsService.nestedSetting(Arrays.asList("struct", "link"), CMLinkable.class, dataview);
    assertTrue(DataViewHelper.isDataView(dvLink));
  }

  @Test
  public void testNestedLinkDataview() {
    CMLinkable contentBean = contentbeanFor(2);
    Map<String, Object> cbMap = settingsService.settingAsMap("struct", String.class, Object.class, contentBean);
    assertFalse(DataViewHelper.isDataView(cbMap.get("link")));

    CMLinkable dataview = dataviewFor(2);
    Map<String, Object> dvMap = settingsService.settingAsMap("struct", String.class, Object.class, dataview);
    assertTrue(DataViewHelper.isDataView(dvMap.get("link")));
  }

  @Test
  public void testNestedLinklistDataviews() {
    CMLinkable dataview = dataviewFor(2);
    Map<String, Object> dvMap = settingsService.settingAsMap("struct", String.class, Object.class, dataview);
    assertDataviews(dvMap.get("linkList"), 2);
  }

  @Test
  public void testStructListDataview() {
    CMLinkable dataview = dataviewFor(2);
    List<Map> dvList = settingsService.settingAsList("structList", Map.class, dataview);
    assertEquals(2, dvList.size());
    Object link = dvList.get(0).get("link");
    assertTrue(link + " is not a dataview!", DataViewHelper.isDataView(link));
  }

  @Test
  public void testStructListDataviews() {
    CMLinkable dataview = dataviewFor(2);
    List<Map> dvList = settingsService.settingAsList("structList", Map.class, dataview);
    assertEquals(2, dvList.size());
    Object linklist = dvList.get(1).get("linkList");
    assertDataviews(linklist, 2);
  }


  @Test
  public void testBeanProxy() {
    LinkablePropertyProxyTest proxy = settingsService.createProxy(LinkablePropertyProxyTest.class, linkable);
    CMLinkable a124 = proxy.getLinkProperty();
    assertNotNull("no bean from proxy", a124);
    assertEquals("wrong bean from proxy", 124, a124.getContentId());
  }

  @Test
  public void testBeanListProxy() {
    LinkableListPropertyProxyTest proxy = settingsService.createProxy(LinkableListPropertyProxyTest.class, linkable);
    List<? extends CMLinkable> a124List = proxy.getLinkListProperty();
    assertNotNull("no list from proxy", a124List);
    assertFalse("empty list from proxy", a124List.isEmpty());
    assertEquals("wrong bean in list from proxy", 124, a124List.get(0).getContentId());
  }


  // --- internal ---------------------------------------------------

  private CMLinkable contentbeanFor(int id) {
    return contentBeanFactory.createBeanFor(contentRepository.getContent(IdHelper.formatContentId(id)), CMLinkable.class);
  }

  private CMLinkable dataviewFor(int id) {
    return dataviewFor(contentbeanFor(id));
  }

  private CMLinkable dataviewFor(CMLinkable contentBean) {
    return dataViewFactory.loadCached(contentBean, null);
  }

  private static void assertDataviews(Object o, int expectedSize) {
    assertTrue(o instanceof Collection);
    Collection collection = (Collection)o;
    assertEquals(expectedSize, collection.size());
    for (Object item : collection) {
      assertTrue(item + " is not a dataview!", DataViewHelper.isDataView(item));
    }
  }

  private interface LinkablePropertyProxyTest {
    CMLinkable getLinkProperty();
  }

  private interface LinkableListPropertyProxyTest {
    List<? extends CMLinkable> getLinkListProperty();
  }

  @Configuration
  @Import(XmlRepoConfiguration.class)
  @ImportResource(value = {
          "classpath:/com/coremedia/cap/common/xml/uapi-xml-services.xml",
          "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml",
          "classpath:/framework/spring/blueprint-contentbeans.xml",
          "classpath:/framework/spring/blueprint-contentbeans-settings.xml"
  },
          reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class)
  static class CMLinkableBeanSettingsFinderTestConfiguration {

    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY_URL)  ;
    }
  }

}

package com.coremedia.blueprint.personalization.elastic;

import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.personalization.forms.FormField;
import com.coremedia.blueprint.personalization.forms.PersonalizationForm;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.MapPropertyMaintainer;
import com.coremedia.personalization.context.PropertyProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:/com/coremedia/cae/contentbean-services.xml",
        "classpath:/com/coremedia/cae/dataview-services.xml",
        "classpath:/com/coremedia/cae/link-services.xml",
        "classpath:/com/coremedia/cache/cache-services.xml",
        "classpath:/com/coremedia/id/id-services.xml",
        "classpath:/com/coremedia/blueprint/personalization/p13n-xml-repo-context.xml",
        "classpath:/framework/spring/blueprint-contentbeans.xml",
        "classpath:/META-INF/coremedia/es-p13n-contexts.xml",
        "classpath:/com/coremedia/blueprint/personalization/elastic/es-p13n-cae-test-context.xml"
})
@TestPropertySource(properties = "elastic.core.persistence=memory")
public class InterestsServiceTest {

  @Inject
  private InterestsService interestsService;
  @Inject
  private InterestsConfiguration interestsConfiguration;
  @Inject
  private ContentBeanFactory contentBeanFactory;
  @Inject
  private ContentRepository contentRepository;
  @Inject
  private ContextCollection contextCollection;

  @Before
  public void before() {
    contextCollection.clear();
  }

  @Test
  public void testGetExplicitUserInterests() {
    Assert.assertNotNull(interestsService);

    final Map<String, Integer> stringMap = new HashMap<>();
    stringMap.put("not_a_content_bean", 1);
    stringMap.put(contentRepository.getChild("/Motorsport").getId(), 1);
    contextCollection.setContext(interestsConfiguration.getExplicitContextName(), new MapPropertyMaintainer(stringMap));

    final List<CMTaxonomy> explicitUserInterests = interestsService.getExplicitUserInterests();
    Assert.assertEquals("motorsport: " + explicitUserInterests, 1, explicitUserInterests.size());
  }

  @Test
  public void testGetExplicitInterests() {

    final CMObject explicitPersonalization = contentBeanFactory.createBeanFor(contentRepository.getChild("/Motorsport"), CMObject.class);

    final Map<String, Double> interests = new HashMap<>();
    interests.put(IdHelper.formatContentId(16), .6);
    interests.put(IdHelper.formatContentId(18), .6);
    interests.put(IdHelper.formatContentId(20), .6);
    contextCollection.setContext("explicit", new MapPropertyMaintainer(interests));

    final Page page = mock(Page.class);
    final CMArticle explicitInterestsSettingsMock = contentBeanFactory.createBeanFor(contentRepository.getChild("explicitInterestsSettingsMock"), CMArticle.class);
    Mockito.when(page.getContent()).thenReturn(explicitInterestsSettingsMock);

    final PersonalizationForm form = interestsService.getExplicitInterests(page);
    final List<FormField> entries = form.getEntries();
    Assert.assertEquals(1, entries.size());
    final FormField formField = entries.get(0);
    Assert.assertEquals(explicitPersonalization, formField.getBean());
    Assert.assertTrue(formField.isValue());
  }

  @Test
  public void testUpdateExplicitInterests() {
    final Map<String, Integer> stringMap = new HashMap<>();
    stringMap.put(contentRepository.getChild("/Motorsport").getId(), 1);  // id 16
    contextCollection.setContext(interestsConfiguration.getExplicitContextName(), new MapPropertyMaintainer(stringMap));

    final List<CMTaxonomy> explicitUserInterests = interestsService.getExplicitUserInterests();
    Assert.assertEquals("motorsport: " + explicitUserInterests, 1, explicitUserInterests.size());

    final Content taxonomy = contentRepository.getContent(IdHelper.formatContentId(18));  // formula 1
    final PersonalizationForm profileForm = new PersonalizationForm();
    final FormField formField = new FormField();
    formField.setValue(true);
    formField.setBean(contentBeanFactory.createBeanFor(taxonomy, CMObject.class));
    final List<FormField> formFields = Arrays.asList(formField);
    profileForm.setEntries(formFields);
    interestsService.updateExplicitInterests(null, profileForm, null);

    final List<String> expected = Arrays.asList(IdHelper.formatContentId(18), "numberOfExplicitInterests");
    final PropertyProvider context = contextCollection.getContext(interestsConfiguration.getExplicitContextName(), PropertyProvider.class);
    Assert.assertEquals("explicit interests", expected.toString(), context.getPropertyNames().toString());
  }

  @Test
  public void testGetImplicitSubjectTaxonomies() {
    final Map<String, Double> interests = new LinkedHashMap<>();
    interests.put(IdHelper.formatContentId(20), .20); // not a taxonomy
    interests.put(IdHelper.formatContentId(18), .18);
    interests.put(IdHelper.formatContentId(16), .16);
    contextCollection.setContext(interestsConfiguration.getImplicitSubjectTaxonomyContextName(), new MapPropertyMaintainer(interests));
    final Map<CMTaxonomy, Double> implicitSubjectTaxonomies = interestsService.getImplicitSubjectTaxonomies();
    Assert.assertEquals(2, implicitSubjectTaxonomies.size());
    int value = 20;
    for (Map.Entry<String, Double> entry : interests.entrySet()) {
      Assert.assertEquals(value, IdHelper.parseContentId(entry.getKey()));
      final double expected = ((double) value) / 100.0;
      final double actual = entry.getValue();
      Assert.assertEquals(expected, actual, 0.001);
      value -= 2;
    }
  }


}

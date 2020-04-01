package com.coremedia.blueprint.personalization;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.personalization.contentbeans.CMSelectionRulesImpl;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.personalization.context.BasicPropertyMaintainer;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.MapPropertyMaintainer;
import com.coremedia.personalization.context.PropertyProfile;
import com.coremedia.personalization.context.util.SegmentUtil;
import com.coremedia.personalization.rulelang.ConditionsProcessor;
import com.google.common.base.Function;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.transform;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:/com/coremedia/cae/contentbean-services.xml",
        "classpath:/com/coremedia/cae/dataview-services.xml",
        "classpath:/com/coremedia/cae/link-services.xml",
        "classpath:/com/coremedia/cache/cache-services.xml",
        "classpath:/com/coremedia/id/id-services.xml",
        "classpath:/com/coremedia/cae/dataview-services.xml",
        "classpath:/com/coremedia/cae/contentbean-services.xml",
        "classpath:/com/coremedia/blueprint/personalization/p13n-xml-repo-context.xml",
        "classpath:/framework/spring/blueprint-contentbeans.xml",

        "classpath:/framework/spring/personalization-plugin/personalization-contentbeans.xml"
})
public class P13NContentTest {

  private CMSelectionRulesImpl personalizedContent;
  private CMSelectionRulesImpl personalizedContent_2;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentRepository contentRepository;
  @Inject
  private ContextCollection contextCollection;
  @Inject
  private ContentBeanFactory contentBeanFactory;

  @Before
  public void setUp() {
    Content content = contentRepository.getChild("/personalizedContent");
    personalizedContent = contentBeanFactory.createBeanFor(content, CMSelectionRulesImpl.class);
    content = contentRepository.getChild("/personalizedContent_2");
    personalizedContent_2 = contentBeanFactory.createBeanFor(content, CMSelectionRulesImpl.class);
  }

  @After
  public void teardown() {
    contextCollection.clear();
  }

  //------------------------------------- SEGMENTS -------------------------------------

  @Test
  public void taxonomyTestSegment() {
    final PropertyProfile segmentProfile = new PropertyProfile();
    for (Integer contentId : Arrays.asList(14,15,16)) {
      final PropertyProfile matchingContext = new PropertyProfile();
      contextCollection.setContext("subjectTaxonomies", matchingContext);
      matchingContext.setProperty(IdHelper.formatContentId(contentId), 0.1);
      final Content segment = contentRepository.getChild("/taxonomyTestSegment");
      evalSegment(segment, segmentProfile);
      final String segmentId = IdHelper.formatContentId(32);
      if(contentId % 2 == 0) {
        Assert.assertTrue("segment: " + contentId, segmentProfile.getBooleanProperty(segmentId));
      } else {
        Assert.assertFalse("segment: " + contentId, segmentProfile.getBooleanProperty(segmentId));
      }
    }
  }

  @Test
  public void keywordTestSegment() {
    for (Integer dimension : Arrays.asList(0,1,2,3)) {
      PropertyProfile context = new PropertyProfile();
      final String keyword = dimension + "D Cinema";
      context.setProperty(keyword, 0.5);
      final PropertyProfile propertyProfile = new PropertyProfile();
      final Content segment = contentRepository.getChild("/keywordTestSegment");
      contextCollection.setContext("keyword", context);
      evalSegment(segment, propertyProfile);
      final String segmentContentId = IdHelper.formatContentId(34);
      if(dimension > 1) {
        Assert.assertTrue(keyword, propertyProfile.getBooleanProperty(segmentContentId));
      } else {
        Assert.assertFalse(keyword, propertyProfile.getBooleanProperty(segmentContentId));
      }
    }
  }

  private void evalSegment(final Content segment, final BasicPropertyMaintainer segmentProfile) {
    final String conditions = SegmentUtil.getConditions(segment, "conditions");
    final ConditionsProcessor processor = new ConditionsProcessor(conditions);
    final boolean processResult = processor.process(contextCollection);
    final String segmentPropertyName = segment.getId();
    segmentProfile.setProperty(segmentPropertyName, processResult);
  }

  //------------------------------------- Personalized Content -------------------------------------

  @Test
  public void personalizedContentWithoutContext(){
    // empty context, so no rule matches
    Assert.assertTrue(personalizedContent.getItemsUnfiltered().isEmpty());
  }

  @Test
  public void testDefaultContent(){
    final List<CMTeasable> itemsUnfiltered = personalizedContent_2.getItemsUnfiltered();
    final Integer[] contentIds = new Integer[itemsUnfiltered.size()];
    transform(itemsUnfiltered, new Function<CMTeasable, Integer>() {
      @Override
      public Integer apply(CMTeasable input) {
        return input.getContentId();
      }
    }).toArray(contentIds);
    Assert.assertArrayEquals(new Integer[]{26, 28}, contentIds);
  }

  @Test
  public void personalizedContentWithKeywordContext() {
    final PropertyProfile keywordContext = new PropertyProfile();
    keywordContext.setProperty("Formula 1", 0.6);
    contextCollection.setContext("keyword", keywordContext);

    final List<? extends CMTeasable> itemsUnfiltered = personalizedContent.getItemsUnfiltered();
    Assert.assertEquals(1, itemsUnfiltered.size());
    Assert.assertEquals(20, itemsUnfiltered.get(0).getContentId());
  }

  @Test
  public void personalizedContentWithSubjectTaxonomyContext() {
    final PropertyProfile subjectTaxonomyContext = new PropertyProfile();
    subjectTaxonomyContext.setProperty(com.coremedia.cap.common.IdHelper.formatContentId(18), 0.6);
    contextCollection.setContext("subjectTaxonomies", subjectTaxonomyContext);
    final List<? extends CMTeasable> itemsUnfiltered = personalizedContent.getItemsUnfiltered();
    Assert.assertEquals(1, itemsUnfiltered.size());
    Assert.assertEquals(20, itemsUnfiltered.get(0).getContentId());
  }

  @Test
  public void personalizedContentWithExplicitContext() {
    final PropertyProfile explicit = new PropertyProfile();
    explicit.setProperty(com.coremedia.cap.common.IdHelper.formatContentId(18), 0.6);
    contextCollection.setContext("explicit", explicit);
    final List<? extends CMTeasable> itemsUnfiltered = personalizedContent.getItemsUnfiltered();
    Assert.assertEquals(1, itemsUnfiltered.size());
    Assert.assertEquals(20, itemsUnfiltered.get(0).getContentId());
  }

  @Test
  public void personalizedContentWithKeywordSegment() {
    final PropertyProfile segmentProfile = new PropertyProfile();
    contextCollection.setContext("segment", segmentProfile);

    final PropertyProfile keyword = new PropertyProfile();
    keyword.setProperty("3D Cinema", 0.6);
    contextCollection.setContext("keyword", keyword);

    evalSegment(contentRepository.getChild("/keywordTestSegment"),segmentProfile);
    final Object property = segmentProfile.getProperty(segmentProfile.getPropertyNames().iterator().next());
    Assert.assertEquals("keyword segment", Boolean.TRUE, property);

    final List<? extends CMTeasable> itemsUnfiltered = personalizedContent.getItemsUnfiltered();
    Assert.assertEquals(1, itemsUnfiltered.size());
    Assert.assertEquals(30, itemsUnfiltered.get(0).getContentId());
  }

  @Test
  public void personalizedContentWithBooleanContext() {
    contextCollection.setContext("myContext", new MapPropertyMaintainer(Collections.singletonMap("default", true)));
    Assert.assertEquals(22, personalizedContent.getItemsUnfiltered().get(0).getContentId());
  }

}

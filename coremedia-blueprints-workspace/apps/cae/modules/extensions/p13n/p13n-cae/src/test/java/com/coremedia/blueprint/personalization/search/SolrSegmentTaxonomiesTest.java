package com.coremedia.blueprint.personalization.search;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.ContextCollectionImpl;
import com.coremedia.personalization.context.MapPropertyMaintainer;
import com.coremedia.personalization.search.SearchFunctionArguments;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Collections;

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
public class SolrSegmentTaxonomiesTest {

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentRepository contentRepository;

  @Test
  public void testEvaluate() throws Exception {
    final SolrSegmentTaxonomies iut = new SolrSegmentTaxonomies();
    iut.setContentRepository(contentRepository);
    iut.setDefaultContextName("testContext");
    iut.setDefaultField("myField");
    final ContextCollection contextCollection = new ContextCollectionImpl();
    Assert.assertEquals("1:2", iut.evaluate(contextCollection, new SearchFunctionArguments()));

    final MapPropertyMaintainer segment32Active = new MapPropertyMaintainer(Collections.singletonMap(IdHelper.formatContentId(32), true));
    contextCollection.setContext("segment", segment32Active);
    Assert.assertEquals("myField:(14 OR 16)", iut.evaluate(contextCollection, new SearchFunctionArguments("context:segment")));
  }
}

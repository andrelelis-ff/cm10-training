package com.coremedia.blueprint.personalization.tracking;

import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.ContextCollectionImpl;
import com.coremedia.personalization.context.PropertyProvider;
import com.coremedia.personalization.scoring.CountScoring;
import com.coremedia.personalization.scoring.ScoringContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.util.Collections;

/**
 * Tests {@link com.coremedia.personalization.tracking.KeywordInterceptor}.
 */
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
public class TaxonomyInterceptorTest {

  private static final String SUBJECT_CONTEXT_NAME = "subjects";
  private static final String LOCATION_CONTEXT_NAME = "locations";

  @Inject
  private ContentBeanFactory contentBeanFactory;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentRepository contentRepository;
  @Inject
  private SitesService sitesService;

  private ContextCollection contextCollection;
  private TaxonomyInterceptor interceptor;
  private CMChannel contextBean;
  private CMLinkable linkable;
  private CMLinkable linkable2;

  @Before
  public void setUp() throws Exception {
    // set up the contextBean
    contextCollection = new ContextCollectionImpl();
    contextCollection.setContext(SUBJECT_CONTEXT_NAME, new ScoringContext(new CountScoring()));
    contextCollection.setContext(LOCATION_CONTEXT_NAME, new ScoringContext(new CountScoring()));

    // set up the interceptor
    interceptor = new TaxonomyInterceptor();
    interceptor.setPropertyToContextMap(Collections.singletonMap("subjectTaxonomy", SUBJECT_CONTEXT_NAME));
    interceptor.setContextCollection(contextCollection);

    contextBean = getContentBean(24);
    linkable = getContentBean(20);
    linkable2 = getContentBean(22);
  }

  private <T> T getContentBean(int i) {
    return (T)contentBeanFactory.createBeanFor(contentRepository.getContent(IdHelper.formatContentId(i)), ContentBean.class);
  }

  // tests whether keywords are properly incremented
  @Test
  public void testTaxonomyIncrement() throws Exception {

    final ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("self", new PageImpl(contextBean, linkable, true, sitesService, null, null, null, null));

    interceptor.postHandle(null, null, null, modelAndView);

    final PropertyProvider context = contextCollection.getContext(SUBJECT_CONTEXT_NAME, PropertyProvider.class);
    Assert.assertEquals(1 + 2, context.getPropertyNames().size());
    Assert.assertEquals(1.0, context.getProperty(IdHelper.formatContentId("18")));
    Assert.assertEquals(null, context.getProperty(IdHelper.formatContentId("16")));

    interceptor.postHandle(null, null, null, modelAndView);
    interceptor.postHandle(null, null, null, modelAndView);

    Assert.assertEquals(1 + 2, context.getPropertyNames().size());
    Assert.assertEquals(3.0, context.getProperty(IdHelper.formatContentId("18")));
  }

  @Test
  public void testOtherProperty() throws Exception {
    final ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("self", new PageImpl(contextBean, linkable, true, sitesService, null, null, null, null));

    TaxonomyInterceptor locationInterceptor = new TaxonomyInterceptor();
    locationInterceptor.setPropertyToContextMap(Collections.singletonMap("locationTaxonomy", LOCATION_CONTEXT_NAME));
    locationInterceptor.setContextCollection(contextCollection);

    locationInterceptor.postHandle(null, null, null, modelAndView);

    final PropertyProvider context = contextCollection.getContext(LOCATION_CONTEXT_NAME, PropertyProvider.class);
    Assert.assertEquals(3 + 2, context.getPropertyNames().size());
    Assert.assertEquals(1.0, context.getProperty(IdHelper.formatContentId("10")));
    Assert.assertEquals(1.0, context.getProperty(IdHelper.formatContentId("6")));
    Assert.assertEquals(1.0, context.getProperty(IdHelper.formatContentId("40")));

    modelAndView.addObject("self", new PageImpl(contextBean, linkable2, true, sitesService, null, null, null, null));
    locationInterceptor.postHandle(null, null, null, modelAndView);
    Assert.assertEquals(3 + 2, context.getPropertyNames().size());
    Assert.assertEquals(1.0, context.getProperty(IdHelper.formatContentId("10")));
    Assert.assertEquals(1.0, context.getProperty(IdHelper.formatContentId("6")));
    Assert.assertEquals(2.0, context.getProperty(IdHelper.formatContentId("40")));
  }

  @Test
  public void testUnknownKeywordProperty() throws Exception {
    final ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("self", new PageImpl(contextBean, linkable, true, sitesService, null, null, null, null));

    interceptor = new TaxonomyInterceptor();
    interceptor.setPropertyToContextMap(Collections.singletonMap("someInvalidProperty", SUBJECT_CONTEXT_NAME));
    interceptor.setContextCollection(contextCollection);

    // this call must not cause an exception!
    interceptor.postHandle(null, null, null, modelAndView);
  }

  @Test
  public void testSelfBeanNotAvailable() throws Exception {
    final ModelAndView modelAndView = new ModelAndView();

    // this call must not cause an exception!
    interceptor.postHandle(null, null, null, modelAndView);
  }
}

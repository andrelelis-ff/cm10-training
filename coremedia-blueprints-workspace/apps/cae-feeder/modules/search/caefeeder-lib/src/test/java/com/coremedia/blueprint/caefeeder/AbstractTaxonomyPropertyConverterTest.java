package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.base.caefeeder.TreePathKeyFactory;
import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.testing.ContentTestConfiguration;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.persistentcache.EvaluationException;
import com.coremedia.cap.persistentcache.PersistentCache;
import com.coremedia.cap.persistentcache.PersistentCacheKey;
import com.coremedia.cap.persistentcache.StoreException;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Before;
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

import javax.inject.Inject;

import static com.coremedia.blueprint.caefeeder.AbstractTaxonomyPropertyConverterTest.LocalConfig.PROFILE;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CACHE;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CONTENT_BEAN_FACTORY;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.DATA_VIEW_FACTORY;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.ID_PROVIDER;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AbstractTaxonomyPropertyConverterTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
public abstract class AbstractTaxonomyPropertyConverterTest {
  @Configuration
  @ImportResource(
          value = {
                  CACHE,
                  CONTENT_BEAN_FACTORY,
                  DATA_VIEW_FACTORY,
                  ID_PROVIDER,
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import({XmlRepoConfiguration.class, ContentTestConfiguration.class})
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "AbstractTaxonomyPropertyConverterTest";
    private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/testing/contenttest.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }
  }
  
  protected CMLocTaxonomy sanFrancisco;
  protected CMLocTaxonomy michigan;
  protected CMTaxonomy formula1;

  @Inject
  private ContentTestHelper contentTestHelper;
  @Inject
  private ContentRepository contentRepository;
  @Inject
  private TreeRelation<Content> taxonomyTreeRelation;

  TreePathKeyFactory<NamedTaxonomy> taxonomyPathKeyFactory;

  @Before
  public void setUp() throws Exception {
    sanFrancisco = contentTestHelper.getContentBean(72);
    michigan = contentTestHelper.getContentBean(70);
    formula1 = contentTestHelper.getContentBean(80);

    taxonomyPathKeyFactory = new TreePathKeyFactory<>("taxonomypath.test:", new DummyPersistentCache(),
                                                      contentRepository, taxonomyTreeRelation,
                                                      new NamedTaxonomyFactory());
  }

  private static class DummyPersistentCache implements PersistentCache {
    @Override
    public Object getCached(PersistentCacheKey key) throws StoreException, EvaluationException {
      return get(key);
    }

    @Override
    public Object get(PersistentCacheKey persistentCacheKey) throws StoreException, EvaluationException {
      try {
        return persistentCacheKey.evaluate();
      } catch (Exception e) {
        throw new EvaluationException(e);
      }
    }

    @Override
    public void remove(PersistentCacheKey persistentCacheKey) throws StoreException {
      throw new UnsupportedOperationException("Unimplemented: #remove");
    }
  }
}

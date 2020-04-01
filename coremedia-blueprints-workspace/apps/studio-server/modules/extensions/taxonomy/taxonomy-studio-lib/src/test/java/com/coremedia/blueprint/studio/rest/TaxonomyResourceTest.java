package com.coremedia.blueprint.studio.rest;

import com.coremedia.blueprint.studio.rest.taxonomies.TaxonomyResource;
import com.coremedia.blueprint.taxonomies.TaxonomyConfiguration;
import com.coremedia.blueprint.taxonomies.TaxonomyNode;
import com.coremedia.blueprint.taxonomies.TaxonomyNodeList;
import com.coremedia.blueprint.taxonomies.TaxonomyResolver;
import com.coremedia.blueprint.taxonomies.cycleprevention.TaxonomyCycleValidator;
import com.coremedia.blueprint.taxonomies.strategy.TaxonomyResolverImpl;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.rest.RestCoreLinkingConfiguration;
import com.coremedia.rest.cap.content.search.solr.SolrSearchService;
import com.coremedia.rest.linking.LinkResolver;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {XmlRepoConfiguration.class, TaxonomyResourceTest.LocalConfig.class})
public class TaxonomyResourceTest {
  @Inject
  private TaxonomyResource taxonomyResource;

  @Test
  public void testTaxonomyResource() {
    TaxonomyNodeList roots = taxonomyResource.getRoots(null, false);
    Assert.assertFalse(roots.getNodes().isEmpty());
    roots.sortByName();
    for (TaxonomyNode node : roots.getNodes()) {
      Assert.assertNotNull(node.getName());
      Assert.assertNotNull(node.getRef());
      Assert.assertNotNull(node.getType());
      Assert.assertTrue(node.isRoot());
      Assert.assertNull(node.getPath());
      Assert.assertNotNull(node.getTaxonomyId());

      Assert.assertNotNull(taxonomyResource.getRoot(null, node.getTaxonomyId()));
      Assert.assertNotNull(taxonomyResource.getNode(null, node.getTaxonomyId(), node.getRef()));

      TaxonomyNodeList children = taxonomyResource.getChildren(null, node.getTaxonomyId(), node.getRef(), 0, 50);
      Assert.assertNotNull(children);
      for (TaxonomyNode child : children.getNodes()) {
        Assert.assertNotNull(taxonomyResource.getPath(null, child.getTaxonomyId(), child.getRef()));
      }
    }
  }

  @Configuration
  @Import({RestCoreLinkingConfiguration.class, TaxonomyConfiguration.class})
  static class LocalConfig {
    @Bean
    XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/testing/contenttest.xml");
    }

    @Bean
    TaxonomyResource taxonomyResource(TaxonomyResolver taxonomyResolver, LinkResolver linkResolver) {
      final TaxonomyResource taxonomyResource = new TaxonomyResource(taxonomyResolver, new ArrayList<>());
      taxonomyResource.setLinkResolver(linkResolver);
      taxonomyResource.afterPropertiesSet();
      return taxonomyResource;
    }

    @Bean
    @Primary
    TaxonomyResolverImpl strategyResolver(ContentRepository contentRepository,
                                          SitesService sitesService,
                                          SolrSearchService solrSearchService,
                                          TaxonomyCycleValidator taxonomyCycleValidator) {
      return new TaxonomyResolverImpl(sitesService,
              contentRepository,
              solrSearchService,
              taxonomyCycleValidator,
              Map.of("Query", "Subject", "QueryLocation", "Location"),
              "CMTaxonomy",
              "Settings/Options",
              "/");
    }

  }
}

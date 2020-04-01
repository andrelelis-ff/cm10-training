package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.coremedia.elastic.core.test.Injection.inject;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CMALXBaseListServiceTest {

  private ElasticSocialPlugin elasticSocialPlugin;

  private static final String TYPE_NAME = "CMALXBaseList";
  private ContentRepository contentRepository;
  private CMALXBaseListService cmalxBaseListService;
  private ContentType contentType;

  @Before
  public void setup() {
    elasticSocialPlugin = mock(ElasticSocialPlugin.class);
    contentType = mock(ContentType.class);
    contentRepository = mock(ContentRepository.class);
    when(contentRepository.getContentType(TYPE_NAME)).thenReturn(contentType);

    cmalxBaseListService = new CMALXBaseListService();
    inject(cmalxBaseListService, contentRepository);
  }

  @Test
  public void testGetCMALXBaseLists() throws Exception {
    inject(cmalxBaseListService, elasticSocialPlugin);
    cmalxBaseListService.initialize();
    verify(contentRepository).getContentType(TYPE_NAME);

    final Content c1 = mock(Content.class);
    when(c1.isInProduction()).thenReturn(true);
    final Content c2 = mock(Content.class);
    when(c2.isInProduction()).thenReturn(true);
    final Content c3 = mock(Content.class);
    when(c3.isInProduction()).thenReturn(false);

    when(contentType.getInstances()).thenReturn(
            new HashSet<>(Arrays.asList(c1,c2))
    );

    ElasticSocialConfiguration config = mock(ElasticSocialConfiguration.class);
    when(elasticSocialPlugin.getElasticSocialConfiguration(c1, null)).thenReturn(config);
    when(config.getTenant()).thenReturn("test");
    ElasticSocialConfiguration config2 = mock(ElasticSocialConfiguration.class);
    when(elasticSocialPlugin.getElasticSocialConfiguration(c2, null)).thenReturn(config2);
    when(config2.getTenant()).thenReturn("unknown");

    assertEquals(singletonList(c1), cmalxBaseListService.getCMALXBaseLists(null, "test"));
    List<Content> emptyList = emptyList();
    assertEquals(emptyList, cmalxBaseListService.getCMALXBaseLists(null, "doesNotExist"));
  }

}

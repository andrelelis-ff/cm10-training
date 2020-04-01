package com.coremedia.livecontext.fragment;

import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.contentbeans.CMExternalChannel;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CMExternalChannelBySiteCacheKeyTest {

  @Mock
  private Site site;
  @Mock
  private Content rootFolder;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ContentRepository contentRepository;

  private Cache cache;

  @Before
  public void setup() {
    cache = new Cache("cache");
    cache.setCapacity(Object.class.getName(), 42);

    when(site.getSiteRootFolder()).thenReturn(rootFolder);
    when(rootFolder.getRepository()).thenReturn(contentRepository);
    Collection<Content> externalChannels = emptyList();
    channelsFulfilling(externalChannels);
  }

  private void channelsFulfilling(Collection<Content> externalChannels) {
    when(contentRepository.getQueryService().getContentsFulfilling(anyCollection(), anyString(), any()))
            .thenReturn(externalChannels);
  }

  @Test
  public void contentTypeMissing() throws Exception {
    assertEquals(emptyMap(), cache.get(new CMExternalPageBySiteCacheKey(site)));
    assertEquals(emptyMap(), cache.get(new CMExternalPageBySiteCacheKey(site)));
  }

  @Test
  public void evaluateEmpty() throws Exception {
    assertEquals(emptyMap(), cache.get(new CMExternalPageBySiteCacheKey(site)));
    assertEquals(emptyMap(), cache.get(new CMExternalPageBySiteCacheKey(site)));
  }

  @Test
  public void evaluate() throws Exception {
    // now let's add some instances
    Content content1 = mock(Content.class);
    Content content2 = mock(Content.class);
    when(content1.getString(CMExternalChannel.EXTERNAL_ID)).thenReturn("hi");
    when(content2.getString(CMExternalChannel.EXTERNAL_ID)).thenReturn("ho");
    channelsFulfilling(asList(content1, content2));

    Map<String, Object> expected = ImmutableMap.of("hi", content1, "ho", content2);
    assertEquals(expected, cache.get(new CMExternalPageBySiteCacheKey(site)));
    assertEquals(expected, cache.get(new CMExternalPageBySiteCacheKey(site)));
  }

}
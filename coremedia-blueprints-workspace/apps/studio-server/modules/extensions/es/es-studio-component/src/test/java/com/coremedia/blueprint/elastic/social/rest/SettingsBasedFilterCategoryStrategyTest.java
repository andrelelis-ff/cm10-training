package com.coremedia.blueprint.elastic.social.rest;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.base.elastic.tenant.TenantSiteMapping;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.elastic.core.api.tenant.TenantService;
import com.coremedia.elastic.social.rest.api.CategoryKeyAndDisplay;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.elastic.core.test.Injection.inject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SettingsBasedFilterCategoryStrategyTest {

  private static final String CHANNEL_SEGMENT = "channelSegment";
  private static final String CHANNEL_TITLE = "channelTitle";
  private static final String TAXONOMY_NAME = "taxonomyName";
  private static final String TENANT_NAME = "tenant";

  @InjectMocks
  private SettingsBasedFilterCategoryStrategy settingsBasedFilterCategoryStrategy = new SettingsBasedFilterCategoryStrategy();

  @Mock
  private TenantService tenantService;

  @Mock
  private TenantSiteMapping tenantSiteMappingHelper;

  @Mock
  private SettingsService settingsService;

  @Mock
  private Map<String, Collection<Content>> tenantSiteMap;

  @Mock
  private Content rootChannel1;

  @Mock
  private Content rootChannel2;

  @Mock
  private Content channelContent;

  @Mock
  private Content taxonomyContent;

  @Mock
  private Content articleContent;

  @Mock
  private ContentType channelContentType;

  @Mock
  private ContentType taxonomyContentType;

  @Mock
  private ContentType articleContentType;

  @Before
  public void setup() {
    when(channelContent.getType()).thenReturn(channelContentType);
    when(channelContentType.isSubtypeOf("CMChannel")).thenReturn(true);
    when(channelContent.getString("segment")).thenReturn(CHANNEL_SEGMENT);
    when(channelContent.getString("title")).thenReturn(CHANNEL_TITLE);
    when(taxonomyContent.getType()).thenReturn(taxonomyContentType);
    when(taxonomyContentType.isSubtypeOf("CMTaxonomy")).thenReturn(true);
    when(taxonomyContent.getString("value")).thenReturn(TAXONOMY_NAME);
    when(articleContent.getType()).thenReturn(articleContentType);

    when(tenantService.getCurrent()).thenReturn(TENANT_NAME);
    when(tenantSiteMappingHelper.getTenantSiteMap()).thenReturn(tenantSiteMap);
  }

  @Test
  public void testSettingsBasedFilterStrategyOneChannel() {
    getCategoryResolvers(new CMChannelCategoryResolver(), new CMTaxonomyCategoryResolver());
    List<Content> rootChannels = getRootChannels(rootChannel1);
    when(tenantSiteMap.get(TENANT_NAME)).thenReturn(rootChannels);
    Map<String, Object> settings = getFilterCategorySettings(channelContent, taxonomyContent, articleContent);
    when(settingsService.mergedSettingAsMap(anyString(), eq(String.class), eq(Object.class), eq(rootChannel1), eq(rootChannel1))).thenReturn(settings);

    List<CategoryKeyAndDisplay> categories = settingsBasedFilterCategoryStrategy.getCategoryList();

    Assert.assertTrue(categoriesContainCategory(categories, new CategoryKeyAndDisplay(CHANNEL_SEGMENT, CHANNEL_TITLE)));
    Assert.assertTrue(categoriesContainCategory(categories, new CategoryKeyAndDisplay(TAXONOMY_NAME, TAXONOMY_NAME)));
  }

  @Test
  public void testSettingsBasedFilterStrategyTwoChannels() {
    getCategoryResolvers(new CMChannelCategoryResolver(), new CMTaxonomyCategoryResolver());
    List<Content> rootChannels = getRootChannels(rootChannel1, rootChannel2);
    when(tenantSiteMap.get(TENANT_NAME)).thenReturn(rootChannels);
    Map<String, Object> settings = getFilterCategorySettings(channelContent);
    when(settingsService.mergedSettingAsMap(anyString(), eq(String.class), eq(Object.class), eq(rootChannel1), eq(rootChannel1))).thenReturn(settings);
    Map<String, Object> settings2 = getFilterCategorySettings(taxonomyContent);
    when(settingsService.mergedSettingAsMap(anyString(), eq(String.class), eq(Object.class), eq(rootChannel2), eq(rootChannel2))).thenReturn(settings2);

    List<CategoryKeyAndDisplay> categories = settingsBasedFilterCategoryStrategy.getCategoryList();

    Assert.assertTrue(categoriesContainCategory(categories, new CategoryKeyAndDisplay(CHANNEL_SEGMENT, CHANNEL_TITLE)));
    Assert.assertTrue(categoriesContainCategory(categories, new CategoryKeyAndDisplay(TAXONOMY_NAME, TAXONOMY_NAME)));
  }

  private boolean categoriesContainCategory(List<CategoryKeyAndDisplay> categories, CategoryKeyAndDisplay categoryKeyAndDisplay) {
    for (CategoryKeyAndDisplay category : categories) {
      if (category.getKey().equals(categoryKeyAndDisplay.getKey())
              && category.getDisplay().equals(categoryKeyAndDisplay.getDisplay())) {
        return true;
      }
    }

    return false;
  }

  private List<Content> getRootChannels(Content... channels) {
    List<Content> rootChannels = new ArrayList<>();
    rootChannels.addAll(Arrays.asList(channels));
    return rootChannels;
  }

  private void getCategoryResolvers(CategoryResolver... resolvers) {
    List<CategoryResolver> categoryResolvers = new ArrayList<>();
    categoryResolvers.addAll(Arrays.asList(resolvers));
    inject(settingsBasedFilterCategoryStrategy, categoryResolvers);
  }

  private Map<String, Object> getFilterCategorySettings(Content... contents) {
    Map<String, Object> settings = new HashMap<>();
    List<Content> filterCategories = new ArrayList<>();
    filterCategories.addAll(Arrays.asList(contents));
    settings.put(SettingsBasedFilterCategoryStrategy.FILTER_CATEGORIES_PROPERTY, filterCategories);
    return settings;
  }
}

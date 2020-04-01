package com.coremedia.blueprint.personalization.include;

import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.layout.ContentBeanBackedPageGridPlacement;
import com.coremedia.blueprint.cae.web.taglib.DynamizableCMTeasableContainerWithDynamicStrategy;
import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator;
import com.coremedia.blueprint.common.layout.DynamizableContainer;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.blueprint.viewtype.ViewtypeService;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {XmlRepoConfiguration.class, P13NDynamicContainerStrategyCacheIntegrationTest.LocalConfig.class})
public class P13NDynamicContainerStrategyCacheIntegrationTest {

  private Content persoContent;

  private CMTeasable teasable;

  private P13NDynamicContainerStrategy strategy;

  //  ----------------------------Injects----------------------------
  @Inject
  private Cache cache;

  @Inject
  private ContentRepository contentRepository;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentBeanFactory contentBeanFactory;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentBackedPageGridService contentBackedPageGridService;

  @Inject
  private ValidationService<Linkable> validationService;

  @Inject
  private ViewtypeService viewtypeService;

  @Inject
  private ValidityPeriodValidator visibilityValidator;

  //  ----------------------------Mocks----------------------------
  @Mock
  private SettingsService settingsService;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private SitesService sitesService;

  @Mock
  private Site site;

  @PostConstruct
  private void initialize() {
    MockitoAnnotations.initMocks(this);

    //initialize strategy
    strategy = new P13NDynamicContainerStrategy(settingsService, sitesService, cache);

    when(sitesService.getContentSiteAspect(any()).getSite()).thenReturn(site);

    when(settingsService.settingWithDefault(any(), any(), any(), any())).thenReturn(true);

    //create a teaseable content
    teasable = getTeasable();

    //create perso content
    persoContent = getPersoContent();
  }

  @Test
  public void testDynamizableContainerOnContentChangePersoRemoved() throws Exception {
    //test with DynamizableContainer
    addPersoItemToTeasableItems();

    List itemsList = singletonList(getDynamizableContainer(teasable, "related"));
    assertThat(strategy.isDynamic(itemsList)).isTrue();

    removePersoItemfromTeasableItems();
    assertThat(strategy.isDynamic(itemsList)).isFalse();
  }

  @Test
  public void testDynamizableContainerContentChangePersoAdded() throws Exception {
    //test with DynamizableContainer
    removePersoItemfromTeasableItems();

    List itemsList = singletonList(getDynamizableContainer(teasable, "related"));
    assertThat(strategy.isDynamic(itemsList)).isFalse();

    addPersoItemToTeasableItems();
    assertThat(strategy.isDynamic(itemsList)).isTrue();
  }

  @Test
  public void testContentBeanBackedPageGridPlacementPersoAdded() throws Exception {
    CMChannel channel = getChannelWithPageGrid();
    List itemsList = singletonList(getContentBeanBackedPageGridPlacement());

    assertThat(strategy.isDynamic(itemsList)).isFalse();

    addPlacementItem(channel, "212", persoContent);
    assertThat(strategy.isDynamic(itemsList)).isTrue();
  }

  @Test
  public void testContentBeanBackedPageGridPlacementPersoRemoved() throws Exception {
    CMChannel channel = getChannelWithPageGrid();
    addPlacementItem(channel, "212", persoContent);

    List itemsList = singletonList(getContentBeanBackedPageGridPlacement());

    assertThat(strategy.isDynamic(itemsList)).isTrue();

    removePlacementItem(channel, "212", persoContent);
    assertThat(strategy.isDynamic(itemsList)).isFalse();
  }

  private void addPlacementItem(CMChannel channel, String placement, Content item) {
    StructBuilder placementStructBuilder = getChannelsPlacementStructBuilder(channel, placement);
    placementStructBuilder.add("items", item);
    Struct placementStruct = placementStructBuilder.build();
    setProperty(channel.getContent(), "placement", placementStruct);
  }

  private void removePlacementItem(CMChannel channel, String placement, Content item) {
    StructBuilder placementStructBuilder = getChannelsPlacementStructBuilder(channel, placement);
    List<Content> items = placementStructBuilder.currentStruct().getLinks("items");
    placementStructBuilder.remove("items", items.indexOf(item));
    Struct placementStruct = placementStructBuilder.build();
    setProperty(channel.getContent(), "placement", placementStruct);
  }

  private StructBuilder getChannelsPlacementStructBuilder(CMChannel channel, String placement) {
    StructBuilder placementStructBuilder = channel
            .getPlacement()
            .builder();

    placementStructBuilder.at("placements_2", "placements", placement);
    return placementStructBuilder;
  }

  private DynamizableContainer getDynamizableContainer(CMTeasable teasable, String propertyPath) {
    return new DynamizableCMTeasableContainerWithDynamicStrategy(teasable, propertyPath, strategy);
  }

  private ContentBeanBackedPageGridPlacement getContentBeanBackedPageGridPlacement() {
    CMChannel channel = getChannelWithPageGrid();
    int indexOffset = 1;
    int row = 1 - indexOffset;
    int columns = 0;
    int colIndex = 1 - indexOffset;
    return new ContentBeanBackedPageGridPlacement(
            channel,
            row,
            columns,
            colIndex,
            contentBackedPageGridService,
            validationService,
            visibilityValidator,
            viewtypeService
    );
  }

  private Content getPersoContent() {
    return contentRepository.getChild("/personalizedContent");
  }

  private CMTeasable getTeasable() {
    Content content = contentRepository.getChild("/visitedArticle1");
    return contentBeanFactory.createBeanFor(content, CMArticle.class);
  }

  private CMChannel getChannelWithPageGrid() {
    Content content = contentRepository.getChild("/PagegriddedChannel");
    return contentBeanFactory.createBeanFor(content, CMChannel.class);
  }

  private void addPersoItemToTeasableItems() {
    setTeasableItem(singletonList(persoContent), "related");
  }

  private void removePersoItemfromTeasableItems() {
    setTeasableItem(Collections.emptyList(), "related");
  }

  private void setTeasableItem(Object item, String propertyPath) {
    Content content = teasable.getContent();
    setProperty(content, propertyPath, item);
  }

  private void setProperty(Content content, String propertyName, Object propertyValue) {
    content.set(propertyName, propertyValue);
    contentRepository.getConnection().flush();
  }

  // --- configuration class ----------------------------------------------------------------------

  @Configuration
  @ImportResource(
    value = {
      "classpath:/framework/spring/personalization-plugin/personalization-contentbeans.xml"
    },
    reader = ResourceAwareXmlBeanDefinitionReader.class)
  public static class LocalConfig {
    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/blueprint/P13NDynamicContainerStrategyCache/p13nContainerStrategyCacheTestRepo.xml");
    }
  }
}

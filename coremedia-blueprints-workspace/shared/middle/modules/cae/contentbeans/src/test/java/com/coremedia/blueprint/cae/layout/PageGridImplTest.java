package com.coremedia.blueprint.cae.layout;

import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.base.pagegrid.PageGridConstants;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.blueprint.common.layout.PageGridRow;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.blueprint.viewtype.ViewtypeService;
import com.coremedia.blueprint.viewtype.configuration.ViewtypeServiceConfiguration;
import com.coremedia.cache.EvaluationException;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={PageGridImplTest.LocalConfig.class, XmlRepoConfiguration.class})
public class PageGridImplTest {

  private static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

  private static final List<String> PREVIEW_DATES = ImmutableList.of(
                  "1963-01-01T01:00:00+01:00", // 0
                  "1963-11-19T06:00:00+01:00", // 1
                  "1974-07-01T03:00:00+01:00", // 2
                  "2005-12-31T06:00:00+01:00", // 3
                  "2007-12-31T06:00:01+01:00", // 4
                  "2015-01-01T03:00:00+01:00", // 5
                  "2025-01-01T03:00:00+01:00"  // 6
          );

  private static final List<Integer> EXPECTED_ITEM_COUNTS = ImmutableList.of(
          0,
          0,
          3,
          2,
          3,
          0,
          0
  );

  @Inject
  private ContentBackedPageGridService contentBackedPageGridService;
  @Inject
  private ValidationService<Linkable> validationService;
  @Inject
  private ValidityPeriodValidator visibilityValidator;
  @Inject
  private ContentBeanFactory contentBeanFactory;
  @Inject
  private ContentRepository contentRepository;
  @Inject
  private ViewtypeService viewtypeService;

  private PageGridImpl pageGrid;


  // --- setup ------------------------------------------------------

  @Before
  public void setUp() throws Exception {
    Content content = contentRepository.getContent(IdHelper.formatContentId(222));
    CMChannel channel = contentBeanFactory.createBeanFor(content, CMChannel.class);
    pageGrid = new PageGridImpl(channel, contentBackedPageGridService, validationService, visibilityValidator, viewtypeService);
  }


  // --- Tests ------------------------------------------------------

  @Test
  public void testGetRows() {
    List<PageGridRow> rows = pageGrid.getRows();
    assertEquals("wrong number of rows", 3, rows.size());
    assertEquals("wrong length row 1", 2, rows.get(0).getPlacements().size());
    assertEquals("wrong west placements", "west", rows.get(0).getPlacements().get(0).getName());
    assertEquals("wrong north placements", "north", rows.get(0).getPlacements().get(1).getName());
    assertEquals("wrong length row 2", 2, rows.get(1).getPlacements().size());
    assertEquals("wrong main placements", PageGridConstants.MAIN_PLACEMENT_NAME, rows.get(1).getPlacements().get(0).getName());
    assertEquals("wrong east placements", "east", rows.get(1).getPlacements().get(1).getName());
    assertEquals("wrong length row 3", 1, rows.get(2).getPlacements().size());
    assertEquals("wrong south placements", "south", rows.get(2).getPlacements().get(0).getName());
  }

  @Test
  public void testGetNumcols() {
    assertEquals("wrong number of columns", 4, pageGrid.getNumcols());
  }

  @Test
  public void testGetMainItems() {
    List<? extends Content> mainItems = pageGrid.getMainItems();
    assertEquals("wrong number of main items", 1, mainItems.size());
    assertEquals("wrong main content", "article1", mainItems.get(0).getName());
  }

  @Test
  public void testPlacement() {
    PageGridPlacement pageGridPlacement = pageGrid.getRows().get(2).getPlacements().get(0);
    assertEquals("wrong col", 1, pageGridPlacement.getCol());
    assertEquals("wrong colspan", 2, pageGridPlacement.getColspan());
    assertEquals("wrong width", 50, pageGridPlacement.getWidth());
    assertEquals("wrong name", "south", pageGridPlacement.getName());
    assertEquals("wrong placements", 1, pageGridPlacement.getItems().size());
    assertNull("unexpected viewtype", pageGridPlacement.getViewTypeName());
  }

  @Test
  public void testPlacementByName() {
    PageGridPlacement pageGridPlacement = pageGrid.getPlacementForName("main");
    assertEquals("wrong col", 2, pageGridPlacement.getCol());
    assertEquals("wrong colspan", 1, pageGridPlacement.getColspan());
    assertEquals("wrong width", 25, pageGridPlacement.getWidth());
    assertEquals("wrong name", "main", pageGridPlacement.getName());
    assertEquals("wrong placements", 1, pageGridPlacement.getItems().size());
    assertNull("unexpected viewtype", pageGridPlacement.getViewTypeName());
  }

  @Test
  public void testPlacementByWrongName() {
    PageGridPlacement pageGridPlacement = pageGrid.getPlacementForName("wrongName");
    assertNull("placement is not null", pageGridPlacement);
  }

  @Test(expected = EvaluationException.class)
  public void testBrokenPageGrid() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(668));
    CMChannel brokenChannel = contentBeanFactory.createBeanFor(content, CMChannel.class);
    PageGrid brokenGrid = new PageGridImpl(brokenChannel, contentBackedPageGridService, validationService, visibilityValidator, viewtypeService);

    //Exception will be thrown when trying to access a placement.
    PageGridPlacement pageGridPlacement = brokenGrid.getPlacementForName("wrongName");
  }

  @Test
  public void testVisibilityInPageGrid() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(888));
    CMChannel channel = contentBeanFactory.createBeanFor(content, CMChannel.class);
    Map<String, Integer> expected = IntStream.range(0, PREVIEW_DATES.size())
            .boxed()
            .collect(Collectors.toMap(
                    index -> PREVIEW_DATES.get(index),
                    index -> EXPECTED_ITEM_COUNTS.get(index)));
    Map<String, Integer> actual = PREVIEW_DATES.stream()
            .map(DATE_TIME_FORMATTER::parse)
            .map(ZonedDateTime::from)
            .collect(Collectors.toMap(
                    DATE_TIME_FORMATTER::format,
                    time -> countItems(time, channel)));

    assertEquals("wrong number of placements", expected, actual);
  }

  private Integer countItems(ZonedDateTime time, CMChannel channel) {
    String date = DATE_TIME_FORMATTER.format(time);

    setPreviewDate(GregorianCalendar.from(time));
    PageGrid pageGrid = new PageGridImpl(channel, contentBackedPageGridService, validationService, visibilityValidator, viewtypeService);
    PageGridPlacement pageGridPlacement = pageGrid.getRows().get(0).getPlacements().get(0);

    assertEquals("wrong name", "west", pageGridPlacement.getName());
    List<? extends Linkable> items = pageGridPlacement.getItems();
    return items.size();
  }

  private void setPreviewDate(Calendar calendar) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    ServletRequestAttributes attrs = new ServletRequestAttributes(request);
    RequestContextHolder.setRequestAttributes(attrs);
    when(request.getAttribute(REQUEST_ATTRIBUTE_PREVIEW_DATE)).thenReturn(calendar);
  }

  @Test
  public void testGetCssClass() {
    assertEquals("wrong cssClassName", "test-setting", pageGrid.getCssClassName());
  }

  //====================================================================================================================

  @Configuration
  @Import(ViewtypeServiceConfiguration.class)
  @ImportResource(value = {"classpath:/framework/spring/blueprint-contentbeans.xml", "classpath:/framework/spring/blueprint-services.xml"},
          reader = ResourceAwareXmlBeanDefinitionReader.class)
  public static class LocalConfig {
    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/blueprint/cae/layout/pagegridcontent.xml");
    }
  }

}

package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.layout.Pagination;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CONTENT_BEAN_FACTORY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CMQueryListImplPaginationTest.LocalConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CMQueryListImplPaginationTest {
  @Configuration
  @Import(XmlRepoConfiguration.class)
  @ImportResource(
          value = {
                  "classpath:/framework/spring/blueprint-contentbeans.xml",
                  CONTENT_BEAN_FACTORY
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  static class LocalConfig {
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return XmlUapiConfig.builder()
              .withSchema("classpath:com/coremedia/blueprint/base/testing/blueprint-doctypes-xmlrepo.xml")
              .build();
    }
  }

  private static int testFolderNumber = 0;

  @Autowired
  private ContentBeanFactory contentBeanFactory;

  @Autowired
  private CapConnection connection;


  // --- Tests ------------------------------------------------------

  @Test
  void testPaginationNoItemsAtAll() {
    Content testFolder = createTestContent();
    try {
      CMQueryListImpl testling = new TestlingBuilder().withTestFolder(testFolder).withPageSize(3).build();
      checkPagination(testling, 3, 0, 0, Collections.emptyList());
      // Special case for template convenience:
      // Always serve a page #0, even if there is logically no page at all.
      checkPage(testling, 0);
    } finally {
      deleteAll(testFolder);
    }
  }

  @Test
  void testPaginationSearchHitsOnly() {
    Content testFolder = createTestContent();
    try {
      CMQueryListImpl testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(3)
              .withAvailableHits(1).build();
      checkPagination(testling, 3, 1, 1, Collections.emptyList());

      testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(3)
              .withAvailableHits(9).build();
      checkPagination(testling, 3, 3, 9, Collections.emptyList());

      testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(3)
              .withAvailableHits(10).build();
      checkPagination(testling, 3, 4, 10, Collections.emptyList());
    } finally {
      deleteAll(testFolder);
    }
  }

  @Test
  void testPagination() {
    Content testFolder = createTestContent();
    try {
      List<Integer> fixedItemIndices = ImmutableList.of(0, 2, 3, 8, 13);
      CMQueryListImpl testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(3)
              .withAvailableHits(9)
              .withFixedItemsAt(fixedItemIndices).build();
      checkPagination(testling, 3, 5, 14, fixedItemIndices);

      fixedItemIndices = ImmutableList.of(0);
      testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(3)
              .withAvailableHits(1)
              .withFixedItemsAt(fixedItemIndices).build();
      checkPagination(testling, 3, 1, 2, fixedItemIndices);

      fixedItemIndices = ImmutableList.of(1);
      testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(3)
              .withAvailableHits(1)
              .withFixedItemsAt(fixedItemIndices).build();
      checkPagination(testling, 3, 1, 2, fixedItemIndices);
    } finally {
      deleteAll(testFolder);
    }
  }

  @Test
  void testPaginationFixedItemsOnly() {
    Content testFolder = createTestContent();
    try {
      List<Integer> fixedItemIndices = ImmutableList.of(0, 1, 2, 3, 4, 5);
      CMQueryListImpl testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(3)
              .withFixedItemsAt(fixedItemIndices).build();
      checkPagination(testling, 3, 2, 6, fixedItemIndices);
    } finally {
      deleteAll(testFolder);
    }
  }

  /**
   * Worst practice test
   * <p>
   * Don't try this at home, aka in your production repository.
   * <p>
   * Invalid items should better not occur in practice, because post-filtering
   * limited query results generally leads to incomplete results.
   * But unfortunately, it is likely to happen, so this test demonstrates
   * the as-is behaviour and proves API robustness.
   */
  @Test
  void testPaginationWithInvalidItems() {
    Content testFolder = createTestContent();
    try {
      // 4 valid and many invalid search hits...
      List<Integer> invalidItemIndices = ImmutableList.of(0, 1, 2, 4, 5, 9, 10);
      List<CMArticle> searchHits = createSearchHits(testFolder, 4, invalidItemIndices);
      SearchResultFactory searchResultFactory = new MockSearchResultFactory(searchHits, searchHits.size());
      // ... mixed with 4 fixed items
      List<Integer> fixedItemIndices = ImmutableList.of(5, 6, 10, 14);
      CMQueryListImpl testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(3)
              .withSearchResultFactory(searchResultFactory)
              .withFixedItemsAt(fixedItemIndices).build();

      // Index calculations are based on the raw search result:
      // 15 items are divided into 5 pages.
      checkNumbers(testling, 3, 5, 15);

      // Invalid items are filtered out per page, so that the actual number of
      // items on a particular page may be less than the specified itemsPerPage.
      // The comments show where invalid items are dropped.
      checkPage(testling, 0 /*, invalid0, invalid1, invalid2*/);
      checkPage(testling, 1, "hit0", /*invalid3,*/ "fix0");
      checkPage(testling, 2, "fix1", /*invalid4,*/ "hit1");
      checkPage(testling, 3, "hit2", "fix2", "hit3");
      checkPage(testling, 4, /*invalid5, invalid6*/ "fix3");
    } finally {
      deleteAll(testFolder);
    }
  }

  @Test
  void testPageSizes() {
    Content testFolder = createTestContent();
    try {
      List<Integer> fixedItemIndices = ImmutableList.of(0, 2, 3, 8, 13);

      CMQueryListImpl testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(1)
              .withAvailableHits(9)
              .withFixedItemsAt(fixedItemIndices).build();
      checkPagination(testling, 1, 14, 14, fixedItemIndices);

      testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(5)
              .withAvailableHits(9)
              .withFixedItemsAt(fixedItemIndices).build();
      checkPagination(testling, 5, 3, 14, fixedItemIndices);

      testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(7)
              .withAvailableHits(9)
              .withFixedItemsAt(fixedItemIndices).build();
      checkPagination(testling, 7, 2, 14, fixedItemIndices);

      testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(1000)
              .withAvailableHits(9)
              .withFixedItemsAt(fixedItemIndices).build();
      checkPagination(testling, 1000, 1, 14, fixedItemIndices);

      testling = new TestlingBuilder().withTestFolder(testFolder)
              .withPageSize(-1)
              .withAvailableHits(9)
              .withFixedItemsAt(fixedItemIndices).build();
      checkPagination(testling, -1, 1, 14, fixedItemIndices);
    } finally {
      deleteAll(testFolder);
    }
  }


  // --- generic checkers -------------------------------------------

  private void checkPagination(CMQueryListImpl testling, int pageSize, int numPages, int numItems, List<Integer> fixedIndices) {
    checkNumbers(testling, pageSize, numPages, numItems);
    checkItems(testling, pageSize, numPages, numItems, fixedIndices);
  }

  private void checkNumbers(CMQueryListImpl testling, int pageSize, int numPages, int numItems) {
    assertTrue(testling.isPaginated());
    assertEquals(pageSize, testling.itemsPerPage());

    Pagination pagination = testling.asPagination();
    assertEquals(numPages, pagination.getNumberOfPages());
    if (pagination instanceof PaginationImpl) {
      assertEquals(numItems, ((PaginationImpl)pagination).totalNumberOfItems());
    }

    // pageSize==ALL => numPages==1
    assertTrue(testling.itemsPerPage()!=-1 || pagination.getNumberOfPages()==1);
  }

  private void checkItems(CMQueryListImpl testling, int pageSize, int numPages, int numItems, List<Integer> fixedIndices) {
    int hitCounter = 0;
    int fixCounter = 0;
    // for loop arithmetics:
    // if pageSize==ALL, the (one and only) page contains numItems (all) items.
    int realPageSize = pageSize==-1 ? numItems : pageSize;
    for (int page=0; page<numPages; ++page) {
      List<? extends Linkable> items = testling.asPagination(page).getItems();
      int from = page * realPageSize;
      int to = Math.min(from+realPageSize, numItems);
      assertEquals(to-from, items.size(), "Number of items on page " + page);
      for (int i=from; i<to; ++i) {
        String expectedName;
        if (fixedIndices.contains(i)) {
          expectedName = "fix"+fixCounter++;
        } else {
          expectedName = "hit"+hitCounter++;
        }
        String actualName = ((CMLinkable) items.get(i - from)).getContent().getName();
        assertEquals(expectedName, actualName, "Unexpected item on page " + page + ", position " + (i-from));
      }
    }
  }

  private void checkPage(CMQueryListImpl testling, int pageNum, String ... itemNames) {
    List<? extends Linkable> items = testling.asPagination(pageNum).getItems();
    assertEquals(itemNames.length, items.size());
    for (int i=items.size()-1; i>=0; --i) {
      assertEquals(itemNames[i], ((CMLinkable)items.get(i)).getContent().getName());
    }
  }


  // --- internal ---------------------------------------------------

  private List<CMArticle> createSearchHits(Content testFolder, int num) {
    return createSearchHits(testFolder, num, Collections.emptyList());
  }

  /**
   * Create a mock search result hits with num valid hits plus
   * additional invalid hits at the given positions.
   * Hits and invalid hits have distinct sequential numbers.
   * Result looks like:
   * "hit0", "invalid0", "hit1", "hit2", "invalid1"
   */
  private List<CMArticle> createSearchHits(Content testFolder, int num, List<Integer> invalidItemsAt) {
    int hitCounter = 0;
    int invalidCounter = 0;
    List<CMArticle> result = new ArrayList<>();
    for (int i=0; i<num; ++i) {
      result.add(contentBeanFactory.createBeanFor(testFolder.getChild("hit"+hitCounter++), CMArticle.class));
    }
    for (int i : invalidItemsAt) {
      result.add(i, contentBeanFactory.createBeanFor(testFolder.getChild("invalid"+invalidCounter++), CMArticle.class));
    }
    return result;
  }


  // --- manage the test content ------------------------------------

  private void deleteAll(Content content) {
    for (Content child : content.getChildren()) {
      deleteAll(child);
    }
    content.delete();
  }

  private Content createTestContent() {
    ContentRepository repository = connection.getContentRepository();
    Content testFolder = repository.createSubfolders("/"+getClass().getName()+"-"+(testFolderNumber++)+"-"+System.currentTimeMillis());

    // Create the testling content
    repository.createChild(testFolder, "testling", "CMQueryList", Collections.emptyMap());
    // Create some fixed items
    for (int i=0; i<10; ++i) {
      repository.createChild(testFolder, "fix"+i, "CMArticle", Collections.emptyMap());
    }
    // Create some "search hits"
    for (int i=0; i<10; ++i) {
      repository.createChild(testFolder, "hit"+i, "CMArticle", Collections.emptyMap());
    }

    // Create some invalid search hits.
    // This is admittedly subtle: We include these items in the mocked
    // search results, while our real search would sort them out because of
    // validFrom/validTo.  However, the ValidationService in this test scope
    // consists only of a ValidityPeriodValidator, so this is our only chance
    // to have invalid items in the search hits.  In a real world CAE, the
    // ValidationService has additional extension or custom validators which
    // consider other hits as invalid.
    Map<String,?> pastFromTo = ImmutableMap.of("validFrom", calendar(42), "validTo", calendar(3600000));
    for (int i=0; i<10; ++i) {
      repository.createChild(testFolder, "invalid"+i, "CMArticle", pastFromTo);
    }

    return testFolder;
  }

  private static Calendar calendar(int millis) {
    Calendar result = Calendar.getInstance();
    result.setTimeInMillis(millis);
    return result;
  }

  private Struct createQuerylistLocalSettingsStruct(int pageSize) {
    Struct fq = connection.getStructService().createStructBuilder().set("documenttype", "CMArticle").build();
    return connection.getStructService().createStructBuilder()
              .set("loadMore", true)
              .set("limit", pageSize)
              .set("fq", fq)
              .build();
  }

  /**
   * Indices start counting from 0 here.  This is more convenient and intuitive
   * for test writing.  In the annotated linklist struct of CMQueryList however,
   * indices start counting from 1, so we adjust that within this method.
   */
  private Struct createExtendedItemsStruct(Content testFolder, List<Integer> indices) {
    int counter = 0;
    List<Struct> links = new ArrayList<>();
    for (int index : indices) {
      links.add(connection.getStructService().createStructBuilder()
              .set("target", testFolder.getChild("fix"+counter++))
              .set("index", index+1)
              .build());
    }
    StructBuilder extendedItemsStructBuilder = connection.getStructService().createStructBuilder();
    if (!links.isEmpty()) {
      extendedItemsStructBuilder.set("links", links);
    }
    return extendedItemsStructBuilder.build();
  }


  // --- setup the testling -----------------------------------------

  private class TestlingBuilder {
    private Content testFolder = null;
    private int pageSize = 1;
    private int availableHits = 0;
    private int totalHits = 0;
    private final List<Integer> fixedItemsAt = new ArrayList<>();
    private SearchResultFactory searchResultFactory = null;

    CMQueryListImpl build() {
      List<CMArticle> limitedSearchHits = createSearchHits(testFolder, availableHits);
      if (searchResultFactory == null) {
        searchResultFactory = new MockSearchResultFactory(limitedSearchHits, totalHits==0 ? availableHits : totalHits);
      }
      Content queryList = testFolder.getChild("testling");
      queryList.set("extendedItems", createExtendedItemsStruct(testFolder, fixedItemsAt));
      queryList.set("localSettings", createQuerylistLocalSettingsStruct(pageSize));
      connection.flush();
      CMQueryListImpl testling = contentBeanFactory.createBeanFor(queryList, CMQueryListImpl.class);
      assumeTrue(testling != null);
      testling.setResultFactory(searchResultFactory);
      return testling;
    }

    TestlingBuilder withTestFolder(Content testFolder) {
      this.testFolder = testFolder;
      return this;
    }

    TestlingBuilder withPageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
    }

    TestlingBuilder withAvailableHits(int availableHits) {
      this.availableHits = availableHits;
      return this;
    }

    TestlingBuilder withTotalHits(int totalHits) {
      this.totalHits = totalHits;
      return this;
    }

    TestlingBuilder withFixedItemsAt(List<Integer> indices) {
      fixedItemsAt.addAll(indices);
      return this;
    }

    TestlingBuilder withSearchResultFactory(SearchResultFactory searchResultFactory) {
      this.searchResultFactory = searchResultFactory;
      return this;
    }
  }
}

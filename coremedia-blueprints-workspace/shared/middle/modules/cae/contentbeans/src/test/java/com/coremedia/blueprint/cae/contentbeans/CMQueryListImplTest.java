package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.common.contentbeans.CMQueryList;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import com.coremedia.objectserver.beans.ContentBean;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE;
import static org.junit.Assert.assertEquals;


public class CMQueryListImplTest extends ContentBeanTestBase {

  private SearchResultFactory resultFactoryEmptyResult;
  private SearchResultFactory resultFactoryNonEmptyResult;

  private CMQueryList contentBean;
  private CMQueryList extendedItemsContentBean;

  private CMTeasable dynamicContent;

  private ContentBean content2;
  private ContentBean content6;


  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    setUpPreviewDate(REQUEST_ATTRIBUTE_PREVIEW_DATE);
    contentBean = getContentBean(108);
    dynamicContent = getContentBean(10);
    resultFactoryEmptyResult = new MockSearchResultFactory(Collections.emptyList());
    resultFactoryNonEmptyResult = new MockSearchResultFactory(Collections.singletonList(dynamicContent));
    extendedItemsContentBean = getContentBean(150);

    content2 = getContentBean(2);
    content6 = getContentBean(6);
  }

  @Test
  public void testGetItemsLegacy() throws Exception {
    CMQueryListImpl queryListImpl = (CMQueryListImpl) contentBean;
    queryListImpl.setResultFactory(resultFactoryEmptyResult);
    assertEquals(1, contentBean.getItems().size());
  }

  @Test
  public void testGetItemsLegacy_withSearchResult() throws Exception {
    CMQueryListImpl queryListImpl = (CMQueryListImpl) contentBean;
    queryListImpl.setResultFactory(resultFactoryNonEmptyResult);
    assertEquals(2, contentBean.getItems().size());
  }

  @Test
  public void testGetExtendedItems() throws Exception {
    CMQueryListImpl queryListImpl = (CMQueryListImpl) extendedItemsContentBean;
    queryListImpl.setResultFactory(resultFactoryEmptyResult);
    List<? extends Linkable> items = extendedItemsContentBean.getItems();
    assertEquals(2, items.size());
    assertEquals(content2, items.get(0));
    assertEquals(content6, items.get(1));
  }

  @Test
  public void testGetExtendedItems_withSearchResult() throws Exception {
    CMQueryListImpl queryListImpl = (CMQueryListImpl) extendedItemsContentBean;
    queryListImpl.setResultFactory(resultFactoryNonEmptyResult);
    List<? extends Linkable> items = extendedItemsContentBean.getItems();
    assertEquals(3, items.size());
    assertEquals(content2, items.get(0));
    assertEquals(dynamicContent, items.get(1));
    assertEquals(content6, items.get(2));
  }

  @Test
  public void testGetExtendedItems_withDuplicateInStruct() throws Exception {
    CMQueryList duplicateExtendedItemsContentBean = getContentBean(174);
    CMQueryListImpl queryListImpl = (CMQueryListImpl) duplicateExtendedItemsContentBean;
    queryListImpl.setResultFactory(resultFactoryEmptyResult);
    List<? extends Linkable> items = duplicateExtendedItemsContentBean.getItems();
    assertEquals(3, items.size());
    assertEquals(content2, items.get(0));
    assertEquals(content2, items.get(1));
    assertEquals(content6, items.get(2));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetExtendedItems_limitMixedItems() throws Exception {
    CMTeasable searchResultContent1 = getContentBean(12);
    CMTeasable searchResultContent2 = getContentBean(14);
    CMTeasable searchResultContent3 = getContentBean(16);
    List contents = Arrays.asList(searchResultContent1, searchResultContent2, searchResultContent3);
    SearchResultFactory resultFactory = new MockSearchResultFactory(contents);

    CMQueryListImpl queryListImpl = (CMQueryListImpl) extendedItemsContentBean;
    queryListImpl.setResultFactory(resultFactory);

    List<? extends Linkable> items = extendedItemsContentBean.getItems();
    assertEquals(4, items.size());
    assertEquals(content2, items.get(0));
    assertEquals(searchResultContent1, items.get(1));
    assertEquals(content6, items.get(2));
    assertEquals(searchResultContent2, items.get(3));
  }
}

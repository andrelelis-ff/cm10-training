package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.base.caefeeder.TreePathKeyFactory;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBean;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrefixedPathHierarchyConverterTest {

  @InjectMocks
  private  PrefixedPathHierarchyConverter prefixedPathHierarchyConverter;

  @Mock
  private TreePathKeyFactory<NamedTaxonomy> treePathKeyFactory;

  @Test
  public void testConvertType_alwaysList() {
    assertEquals("Even with a null parameter the result is List", List.class, prefixedPathHierarchyConverter.convertType(null));
    assertEquals("The result is List no matter what the param value is", List.class, prefixedPathHierarchyConverter.convertType(String.class));
  }

  @Test
  public void testConvertValue_nullParam_returnsEmptyList() {
    Object result = prefixedPathHierarchyConverter.convertValue(null);
    assertTrue("The resulting value should always be a collection", result instanceof Collection && ((Collection)result).isEmpty());
  }

  @Test
  public void testConvertValue_legalContent_returnsResult() {
    ContentBean contentBean = mock(ContentBean.class);
    Content content = mock(Content.class);
    when(contentBean.getContent()).thenReturn(content);

    List<String> expectedResults = Arrays.asList("0/1", "1/1/2", "2/1/2/3");
    ImmutableList<NamedTaxonomy> taxonomyPath = taxonomyPath(1, 2, 3);
    when(treePathKeyFactory.getPath(content)).thenReturn(taxonomyPath);

    Object result = prefixedPathHierarchyConverter.convertValue(Arrays.asList(contentBean));

    assertTrue("Result should be some kind of non-empty collection", result instanceof Collection && !((Collection)result).isEmpty());
    for (Object o : (Collection) result) {
      assertTrue("Result contains " + String.valueOf(o) + " but should be one of " + expectedResults.toString(), expectedResults.contains(o));
    }
  }

  @Test
  public void testConvertValue_legalContentWithRootPathSegment_returnsRootPrefixedPathsResult() {
    ContentBean contentBean = mock(ContentBean.class);
    Content content = mock(Content.class);
    when(contentBean.getContent()).thenReturn(content);

    List<String> expectedResults = Arrays.asList("0/1", "1/1/2");
    ImmutableList<NamedTaxonomy> taxonomyPath = taxonomyPath(1, 2);
    when(treePathKeyFactory.getPath(content)).thenReturn(taxonomyPath);

    Object result = prefixedPathHierarchyConverter.convertValue(Arrays.asList(contentBean));

    assertTrue("Result should be some kind of non-empty collection", result instanceof Collection && !((Collection)result).isEmpty());
    for (Object o : (Collection) result) {
      assertTrue("Result contains " + String.valueOf(o) + " but should be one of " + expectedResults.toString(), expectedResults.contains(o));
    }
  }

  @Test
  public void testConvertValue_legalContentWithDuplicateSegments_returnsResultWithoutDuplicates() {
    ContentBean contentBean = mock(ContentBean.class);
    Content content = mock(Content.class);
    when(contentBean.getContent()).thenReturn(content);
    ContentBean contentBeanInSameRoot = mock(ContentBean.class);
    Content contentInSameRoot = mock(Content.class);
    when(contentBeanInSameRoot.getContent()).thenReturn(contentInSameRoot);

    List<String> expectedResults = Arrays.asList("0/1", "1/1/2", "2/1/2/3", "2/1/2/4");
    ImmutableList<NamedTaxonomy> taxonomyPath123 = taxonomyPath(1, 2, 3);
    when(treePathKeyFactory.getPath(content)).thenReturn(taxonomyPath123);
    ImmutableList<NamedTaxonomy> taxonomyPath124 = taxonomyPath(1, 2, 4);
    when(treePathKeyFactory.getPath(contentInSameRoot)).thenReturn(taxonomyPath124);

    Object result = prefixedPathHierarchyConverter.convertValue(Arrays.asList(contentBean, contentBeanInSameRoot));

    assertTrue("Result should be some kind of non-empty collection", result instanceof Collection && !((Collection)result).isEmpty());
    assertEquals("Result should contain only X paths", expectedResults.size(), ((Collection)result).size() );
    for (Object o : (Collection) result) {
      assertTrue("Result contains " + String.valueOf(o) + " but should be one of " + expectedResults.toString(), expectedResults.contains(o));
    }
  }

  private static ImmutableList<NamedTaxonomy> taxonomyPath(Integer... ids) {
    ImmutableList.Builder<NamedTaxonomy> builder = ImmutableList.builder();
    for (Integer id : ids) {
      builder.add(new NamedTaxonomy(content(id)));
    }
    return builder.build();
  }


  private static Content content(int id) {
    Content content = mock(Content.class, String.valueOf(id));
    when(content.getId()).thenReturn(IdHelper.formatContentId(id));
    return content;
  }
}
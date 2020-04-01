package com.coremedia.blueprint.taxonomies.cycleprevention;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxonomyCycleValidatorImplTest {

  private static final ContentType TAXONOMY_CONTENT_TYPE = mock(ContentType.class);
  private static final String ROOT_ID = "rootId";
  private static final String CHILD_ID_1 = "CHILD_1";

  @Spy
  private TaxonomyCycleValidatorImpl taxonomyCycleValidator;

  @Test
  void onlyOneTaxonomyIsNotACycle() {
    boolean isCyclic = taxonomyCycleValidator.isCyclic(mockTaxonomyContentNoChildren(ROOT_ID), TAXONOMY_CONTENT_TYPE);
    assertThat(isCyclic).isFalse();
  }

  @Test
  void twoTimesTheSameTaxonomyIdInPathIsACycle() {
    Content leaf = mockTaxonomyContentNoChildren(ROOT_ID);
    Content root = mockRootWithChildren(leaf);
    boolean isCyclic = taxonomyCycleValidator.isCyclic(root, TAXONOMY_CONTENT_TYPE);
    assertThat(isCyclic).isTrue();
  }

  @Test
  void twoTimesDifferentTaxonomyIdsInPathIsACycle() {
    Content leaf = mockTaxonomyContentNoChildren(CHILD_ID_1);
    Content root = mockRootWithChildren(leaf);
    boolean isCyclic = taxonomyCycleValidator.isCyclic(root, TAXONOMY_CONTENT_TYPE);
    assertThat(isCyclic).isFalse();
  }

  @NonNull
  private Content mockTaxonomyContentNoChildren(@NonNull String id) {
    Content content = mock(Content.class);
    doReturn(true).when(taxonomyCycleValidator).isTaxonomy(content, TAXONOMY_CONTENT_TYPE);
    when(content.getId()).thenReturn(id);
    return content;
  }

  @NonNull
  private Content mockRootWithChildren(@NonNull Content... children) {
    Content content = mock(Content.class);
    when(content.isDestroyed()).thenReturn(false);
    when(content.isInProduction()).thenReturn(true);
    doReturn(true).when(taxonomyCycleValidator).isTaxonomy(content, TAXONOMY_CONTENT_TYPE);
    when(content.getLinks(TaxonomyCycleValidatorImpl.CHILDREN_ATTRIBUTE_IDENTIFIER)).thenReturn(Arrays.asList(children));
    when(content.getId()).thenReturn(ROOT_ID);
    return content;
  }
}

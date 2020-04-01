package com.coremedia.livecontext.view;

import com.coremedia.livecontext.fragment.FragmentParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class PrefetchFragmentsViewTest {

  private static final String MATRIX_SEPERATOR = ";";
  private static final String KEY_VALUE_SEPERATOR = "=";
  private static final String IF_NULL_VALUE = "";

  private static final String EXTERNAL_REF_KEY = "externalRef";
  private static final String CATEGORY_ID_KEY = "categoryId";
  private static final String PRODUCT_ID_KEY = "productId";
  private static final String PAGE_ID_KEY = "pageId";

  private static final String EXTERNAL_REF_VALUE = "externalRefValue";
  private static final String CATEGORY_ID_VALUE = "categoryIdValue";
  private static final String PRODUCT_ID_VALUE = "productIdValue";
  private static final String PAGE_ID_VALUE = "pageIdValue";

  @Mock
  private FragmentParameters fragmentParameters;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);

    when(fragmentParameters.getExternalRef()).thenReturn(EXTERNAL_REF_VALUE);
    when(fragmentParameters.getCategoryId()).thenReturn(CATEGORY_ID_VALUE);
    when(fragmentParameters.getProductId()).thenReturn(PRODUCT_ID_VALUE);
    when(fragmentParameters.getPageId()).thenReturn(PAGE_ID_VALUE);
  }

  @Test
  void createPageKeyFromParametersAllParametersPresent() {
    String pageKey
      = EXTERNAL_REF_KEY + KEY_VALUE_SEPERATOR + EXTERNAL_REF_VALUE
      + MATRIX_SEPERATOR
      + CATEGORY_ID_KEY + KEY_VALUE_SEPERATOR + CATEGORY_ID_VALUE
      + MATRIX_SEPERATOR
      + PRODUCT_ID_KEY + KEY_VALUE_SEPERATOR + PRODUCT_ID_VALUE
      + MATRIX_SEPERATOR
      + PAGE_ID_KEY + KEY_VALUE_SEPERATOR + PAGE_ID_VALUE;

    assertThat(PrefetchFragmentsView.createPageKeyFromParameters(fragmentParameters)).isEqualTo(pageKey);
  }

  @Test
  void createPageKeyFromParametersWithNullParameter() {
    when(fragmentParameters.getExternalRef()).thenReturn(null);

    String pageKey
      = EXTERNAL_REF_KEY + KEY_VALUE_SEPERATOR + IF_NULL_VALUE
      + MATRIX_SEPERATOR
      + CATEGORY_ID_KEY + KEY_VALUE_SEPERATOR + CATEGORY_ID_VALUE
      + MATRIX_SEPERATOR
      + PRODUCT_ID_KEY + KEY_VALUE_SEPERATOR + PRODUCT_ID_VALUE
      + MATRIX_SEPERATOR
      + PAGE_ID_KEY + KEY_VALUE_SEPERATOR + PAGE_ID_VALUE;

    assertThat(PrefetchFragmentsView.createPageKeyFromParameters(fragmentParameters)).isEqualTo(pageKey);
  }
}

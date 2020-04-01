package com.coremedia.catalog.studio.lib.validators;

import com.coremedia.cap.content.Content;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;
import com.coremedia.rest.validators.NotEmptyValidator;
import org.junit.Test;

import static com.coremedia.catalog.studio.lib.validators.CatalogProductValidator.CODE_ISSUE_NOT_IN_CATALOG;
import static com.coremedia.catalog.studio.lib.validators.CatalogProductValidator.CONTEXTS_PROPERTY_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link CatalogProductValidator}.
 */
public class CatalogProductValidatorTest {

  private static final String TEST_CONTENT_TYPE = "TestCatalogProductContentType";
  private static final String TEST_PROPERTY_NULL = "testCatalogProductPropertyNull";
  private static final String TEST_PROPERTY_NONNULL = "testCatalogProductPropertyNonnull";

  @Test
  public void validate() {
    Issues issues = mock(Issues.class);

    Content content = mock(Content.class);
    when(content.getLinks(CONTEXTS_PROPERTY_NAME)).thenReturn(emptyList());
    when(content.get(TEST_PROPERTY_NULL)).thenReturn(null);
    when(content.get(TEST_PROPERTY_NULL)).thenReturn(null);
    when(content.get(TEST_PROPERTY_NONNULL)).thenReturn("nonnull");

    NotEmptyValidator notEmptyValidatorOnNull = new NotEmptyValidator();
    notEmptyValidatorOnNull.setProperty(TEST_PROPERTY_NULL);

    NotEmptyValidator notEmptyValidatorOnNonnull = new NotEmptyValidator();
    notEmptyValidatorOnNonnull.setProperty(TEST_PROPERTY_NONNULL);

    CatalogProductValidator validator = new CatalogProductValidator();
    validator.setContentType(TEST_CONTENT_TYPE);
    validator.setValidators(asList(notEmptyValidatorOnNull, notEmptyValidatorOnNonnull));
    validator.validate(content, issues);

    verify(issues).addIssue(any(Severity.class), eq(CONTEXTS_PROPERTY_NAME), eq(CODE_ISSUE_NOT_IN_CATALOG));
    verify(issues).addIssue(any(Severity.class), eq(TEST_PROPERTY_NULL), eq(NotEmptyValidator.class.getSimpleName()));
    verify(issues, times(0)).addIssue(any(Severity.class), eq(TEST_PROPERTY_NONNULL), eq(NotEmptyValidator.class.getSimpleName()));
  }
}

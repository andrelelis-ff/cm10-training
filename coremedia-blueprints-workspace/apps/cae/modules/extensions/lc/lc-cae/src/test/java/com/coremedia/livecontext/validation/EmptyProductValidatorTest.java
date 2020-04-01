package com.coremedia.livecontext.validation;

import com.coremedia.livecontext.contentbeans.CMProductTeaser;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class EmptyProductValidatorTest {

  private EmptyProductValidator testling;
  private Predicate predicate;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CMProductTeaser productTeaser;

  @Mock
  private Product product;

  @Before
  public void defaultSetup() {
    testling = new EmptyProductValidator();
    predicate = testling.createPredicate();
    when(productTeaser.getProduct()).thenReturn(product);
    when(productTeaser.getContent().getPath()).thenReturn("irrelevant");
  }

  @Test
  public void supports() {
    assertTrue(testling.supports(CMProductTeaser.class));
  }

  @Test
  public void predicateIsLiveNoProductTeaser() {
    assertFalse(predicate.test(null));
  }

  @Test
  public void predicateIsPreviewNoProductTeaser() {
    testling.setPreview(true);
    assertTrue(predicate.test(null));
  }

  @Test
  public void predicateIsLiveHasProduct() {
    assertTrue(predicate.test(productTeaser));
    verify(productTeaser).getProduct();
  }

  @Test
  public void predicateIsPreviewHasProduct() {
    assertTrue(predicate.test(productTeaser));
    verify(productTeaser).getProduct();
  }

  @Test
  public void predicateIsLiveNoProduct() {
    when(productTeaser.getProduct()).thenReturn(null);
    assertFalse(predicate.test(productTeaser));
    verify(productTeaser).getProduct();
  }

  @Test
  public void predicateIsPreviewNoProduct() {
    testling.setPreview(true);
    assertTrue(predicate.test(productTeaser));
    verify(productTeaser, never()).getProduct();
  }

  @Test
  public void predicateIsLiveNotFoundException() {
    when(productTeaser.getProduct()).thenThrow(NotFoundException.class);
    assertFalse(predicate.test(productTeaser));
    verify(productTeaser).getProduct();
  }

  @Test
  public void predicateIsPreviewNotFoundException() {
    testling.setPreview(true);
    assertTrue(predicate.test(productTeaser));
    verify(productTeaser, never()).getProduct();
  }
}

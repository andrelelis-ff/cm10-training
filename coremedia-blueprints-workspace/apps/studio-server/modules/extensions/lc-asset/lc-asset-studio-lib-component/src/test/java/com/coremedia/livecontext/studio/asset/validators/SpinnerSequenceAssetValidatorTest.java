package com.coremedia.livecontext.studio.asset.validators;

import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.livecontext.asset.util.AssetReadSettingsHelper;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.coremedia.livecontext.asset.util.AssetReadSettingsHelper.NAME_LOCAL_SETTINGS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SpinnerSequenceAssetValidatorTest {
  @Mock
  Content pic1;
  @Mock
  Content pic2;
  @Mock
  Content pic3;
  @Mock
  Struct settings1;
  @Mock
  Struct settings2;
  @Mock
  Struct settings3;
  @Mock
  Content spinner;
  @Mock
  AssetReadSettingsHelper assetHelper;
  @Mock
  Issues issues;
  @Mock
  CapConnection connection;

  SpinnerSequenceAssetValidator testling;

  private Map<String, Object> contentProperties1;
  private Map<String, Object> contentProperties2;
  private Map<String, Object> contentProperties3;

  @Before
  public void setup() {
    initMocks(this);

    testling = new SpinnerSequenceAssetValidator(assetHelper, connection);

    contentProperties1 = new HashMap<>();
    contentProperties2 = new HashMap<>();
    contentProperties3 = new HashMap<>();

    contentProperties1.put(NAME_LOCAL_SETTINGS, settings1);
    contentProperties2.put(NAME_LOCAL_SETTINGS, settings2);
    contentProperties3.put(NAME_LOCAL_SETTINGS, settings3);

    when(pic1.getProperties()).thenReturn(contentProperties1);
    when(pic2.getProperties()).thenReturn(contentProperties2);
    when(pic3.getProperties()).thenReturn(contentProperties3);
  }

  @Test
  public void noPicturesAtAll() {
    when(spinner.getLinks(SpinnerSequenceAssetValidator.SEQUENCE_PROPERTY)).thenReturn(Collections.emptyList());
    testling.validate(spinner, issues);
    verify(issues, never()).addIssue(any(Severity.class), any(String.class), any(String.class));
  }

  @Test
  public void noExternalProducts() {
    when(spinner.getLinks(SpinnerSequenceAssetValidator.SEQUENCE_PROPERTY)).thenReturn(Arrays.asList(pic1, pic2, pic3));
    when(assetHelper.getCommerceReferences(any(Map.class))).thenReturn(Collections.<String>emptyList());
    testling.validate(spinner, issues);
    verify(issues, never()).addIssue(any(Severity.class), any(String.class), any(String.class));
  }

  @Test
  public void uniqueExternalProducts() {
    when(spinner.getLinks(SpinnerSequenceAssetValidator.SEQUENCE_PROPERTY)).thenReturn(Arrays.asList(pic1, pic2, pic3));
    when(assetHelper.getCommerceReferences(contentProperties1)).thenReturn(Collections.emptyList());
    when(assetHelper.getCommerceReferences(contentProperties2)).thenReturn(Arrays.asList("prod1", "prod2"));
    when(assetHelper.getCommerceReferences(contentProperties3)).thenReturn(Collections.emptyList());
    testling.validate(spinner, issues);
    verify(issues, never()).addIssue(any(Severity.class), any(String.class), any(String.class));
  }

  @Test
  public void partialExternalProducts() {
    when(spinner.getLinks(SpinnerSequenceAssetValidator.SEQUENCE_PROPERTY)).thenReturn(Arrays.asList(pic1, pic2, pic3));
    when(assetHelper.getCommerceReferences(contentProperties1)).thenReturn(Arrays.asList("prod1", "prod2"));
    when(assetHelper.getCommerceReferences(contentProperties2)).thenReturn(Collections.emptyList());
    when(assetHelper.getCommerceReferences(contentProperties3)).thenReturn(Arrays.asList("prod1", "prod2"));
    testling.validate(spinner, issues);
    verify(issues, never()).addIssue(any(Severity.class), any(String.class), any(String.class));
  }

  @Test
  public void sameExternalProducts() {
    when(spinner.getLinks(SpinnerSequenceAssetValidator.SEQUENCE_PROPERTY)).thenReturn(Arrays.asList(pic1, pic2, pic3));
    when(assetHelper.getCommerceReferences(contentProperties1)).thenReturn(Arrays.asList("prod1", "prod2"));
    when(assetHelper.getCommerceReferences(contentProperties2)).thenReturn(Arrays.asList("prod1", "prod2"));
    when(assetHelper.getCommerceReferences(contentProperties3)).thenReturn(Arrays.asList("prod1", "prod2"));
    testling.validate(spinner, issues);
    verify(issues, never()).addIssue(any(Severity.class), any(String.class), any(String.class));
  }

  @Test
  public void equivalentExternalProducts() {
    when(spinner.getLinks(SpinnerSequenceAssetValidator.SEQUENCE_PROPERTY)).thenReturn(Arrays.asList(pic1, pic2, pic3));
    when(assetHelper.getCommerceReferences(contentProperties1)).thenReturn(Arrays.asList("prod1", "prod2"));
    when(assetHelper.getCommerceReferences(contentProperties2)).thenReturn(Arrays.asList("prod2", "prod1"));
    when(assetHelper.getCommerceReferences(contentProperties3)).thenReturn(Arrays.asList("prod1", "prod1", "prod2"));
    testling.validate(spinner, issues);
    verify(issues, never()).addIssue(any(Severity.class), any(String.class), any(String.class));
  }

  @Test
  public void mismatchingExternalProducts() {
    when(spinner.getLinks(SpinnerSequenceAssetValidator.SEQUENCE_PROPERTY)).thenReturn(Arrays.asList(pic1, pic2));
    when(assetHelper.getCommerceReferences(contentProperties1)).thenReturn(Arrays.asList("prod1", "prod2"));
    when(assetHelper.getCommerceReferences(contentProperties2)).thenReturn(Collections.singletonList("prod1"));
    testling.validate(spinner, issues);
    verify(issues, atLeastOnce()).addIssue(any(Severity.class), any(String.class), any(String.class));
  }
}

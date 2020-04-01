package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.cae.util.SecureHashCodeGeneratorStrategy;
import com.coremedia.elastic.core.api.blobs.Blob;
import com.coremedia.elastic.core.api.blobs.BlobService;
import com.coremedia.transform.BlobTransformer;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticBlobHandlerTest {
  @InjectMocks
  private ElasticBlobHandler handler;

  private BlobRef blobRef;

  protected String id = "123";
  protected String md5 = "md5";
  protected String securityHash = "secure";
  protected String fileName = "name.ext";

  @Mock
  private BlobService blobService;

  @Mock
  private BlobTransformer blobTransformer;

  @Mock
  protected Blob blob;

  @Mock
  private SecureHashCodeGeneratorStrategy secureHashCodeGeneratorStrategy;

  @Mock
  private HttpServletRequest request;

  @Before
  public void setup() {
    blobRef = new BlobRefImpl(id);
    when(blobService.get(id)).thenReturn(blob);
    when(blob.getId()).thenReturn(id);
    when(blob.getMd5()).thenReturn(md5);
    when(blob.getFileName()).thenReturn(fileName);
    when(secureHashCodeGeneratorStrategy.generateSecureHashCode(anyMap())).thenReturn(securityHash);
  }

  protected void validateMap(Map<String, ?> expected, Map<String, ?> result) {
    assertNotNull(result);
    assertEquals(expected.size(), result.size());

    for (Map.Entry<String, ?> entry : expected.entrySet()) {
      assertEquals(entry.getValue(), result.get(entry.getKey()));
    }
  }

  protected Map<String, Object> getDefaultResultMap() {
    HashMap<String, Object> map = new HashMap<>();
    map.put("contentId", id);
    map.put("version", md5);
    map.put("secHash", securityHash);
    map.put("contentName", fileName);
    map.put("site", "-");

    return map;
  }

  protected Map<String, Object> getDefaultResultMap(int width, int height) {
    Map<String, Object> map = getDefaultResultMap();
    map.put("width", width);
    map.put("height", height);

    return map;
  }

  @Test
  public void buildLink() {
    Map<String, ?> result = handler.buildLink(blob, request);

    validateMap(getDefaultResultMap(), result);
  }

  @Test
  public void buildLinkWithTransformation() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("width", 100, "height", 100, "transform", true);
    Map<String, Object> expectedResult = getDefaultResultMap(100, 100);

    Map<String, ?> result = handler.buildLinkWithTransformation(blob, params, request);

    validateMap(expectedResult, result);
  }

  @Test
  public void buildLinkWithTransformationWithDefaultHeight() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("height", 100, "transform", true);
    Map<String, Object> expectedResult = getDefaultResultMap(48, 100);

    Map<String, ?> result = handler.buildLinkWithTransformation(blob, params, request);

    validateMap(expectedResult, result);
  }

  @Test
  public void buildLinkWithTransformationWithDefaultWidth() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("width", 100, "transform", true);
    Map<String, Object> expectedResult = getDefaultResultMap(100, 48);

    Map<String, ?> result = handler.buildLinkWithTransformation(blob, params, request);

    validateMap(expectedResult, result);
  }

  @Test
  public void buildLinkWithTransformationWithWidthAndHeight() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("width", 100, "height", 100);
    Map<String, Object> expectedResult = getDefaultResultMap(100, 100);

    Map<String, ?> result = handler.buildLinkWithWidthAndHeight(blob, params, request);

    validateMap(expectedResult, result);
  }

  @Test
  public void buildLinkForRef() {
    Map<String, ?> result = handler.buildLink(blobRef, request);

    validateMap(getDefaultResultMap(), result);
  }

  @Test
  public void buildLinkForRefWithTransformation() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("width", 100, "height", 100, "transform", true);
    Map<String, Object> expectedResult = getDefaultResultMap(100, 100);

    Map<String, ?> result = handler.buildLinkWithTransformation(blobRef, params, request);

    validateMap(expectedResult, result);
  }

  @Test
  public void buildLinkForRefWithTransformationWithDefaultHeight() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("height", 100, "transform", true);
    Map<String, Object> expectedResult = getDefaultResultMap(48, 100);

    Map<String, ?> result = handler.buildLinkWithTransformation(blobRef, params, request);

    validateMap(expectedResult, result);
  }

  @Test
  public void buildLinkForRefWithTransformationWithDefaultWidth() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("width", 100, "transform", true);
    Map<String, Object> expectedResult = getDefaultResultMap(100, 48);

    Map<String, ?> result = handler.buildLinkWithTransformation(blobRef, params, request);

    validateMap(expectedResult, result);
  }

  @Test
  public void buildLinkForRefWithTransformationWithWidthAndHeight() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("width", 100, "height", 100);
    Map<String, Object> expectedResult = getDefaultResultMap(100, 100);

    Map<String, ?> result = handler.buildLinkWithWidthAndHeight(blobRef, params, request);

    validateMap(expectedResult, result);
  }
}

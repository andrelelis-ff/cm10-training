package com.coremedia.blueprint.assets.studio.upload;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static com.coremedia.blueprint.assets.studio.upload.AMDoctypeRewriteUploadInterceptor.ASSET_ROOT_FOLDER;
import static com.coremedia.blueprint.assets.studio.upload.AMDoctypeRewriteUploadInterceptor.DATA_ATTRIBUTE;
import static com.coremedia.blueprint.assets.studio.upload.AMDoctypeRewriteUploadInterceptor.ORIGINAL_ATTRIBUTE;
import static com.coremedia.rest.cap.intercept.InterceptorControlAttributes.DO_NOTHING;
import static com.coremedia.rest.cap.intercept.InterceptorControlAttributes.UPLOADED_DOCUMENTS;
import static com.google.common.collect.ImmutableList.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AMDoctypeRewriteUploadInterceptorTest {

  private static final String CREATED_CONTENT_NAME = "Created Content Name";
  private static final String DATA_OBJECT = "anydata";

  @Mock
  private ContentType originalDoctype;

  @Mock
  private ContentType targetDoctype;

  private AMDoctypeRewriteUploadInterceptor testling;

  @Before
  public void setup() {
    testling = new AMDoctypeRewriteUploadInterceptor(originalDoctype, targetDoctype);
  }

  @Test
  public void interceptSuccessful() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(DATA_ATTRIBUTE, DATA_OBJECT);

    ContentWriteRequest request = mockRequest(properties, ASSET_ROOT_FOLDER,null);

    Content newCreatedContent = configureTargetDoctypeForCreation(request, targetDoctype);

    testling.intercept(request);
    verifyCreated(request, newCreatedContent);
  }

  @Test
  public void interceptInterruptedNotBelowAssetFolder() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(DATA_ATTRIBUTE, DATA_OBJECT);

    ContentWriteRequest request = mockRequest(properties, "/NotBelowAssets", null);

    testling.intercept(request);

    verifyNotCreated(request);
  }

  @Test
  public void interceptInterruptedNotBelowAssetFolderButAssetFolder() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(DATA_ATTRIBUTE, DATA_OBJECT);

    ContentWriteRequest request = mockRequest(properties, "/PATH/TO/ANY/SITE/Assets", null);

    testling.intercept(request);

    verifyNotCreated(request);
  }

  @Test
  public void interceptInterruptedEntityExist() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(DATA_ATTRIBUTE, DATA_OBJECT);

    ContentWriteRequest request = mockRequest(properties, ASSET_ROOT_FOLDER, mock(Content.class));

    testling.intercept(request);
    verifyNotCreated(request);
  }

  @NonNull
  private Content configureTargetDoctypeForCreation(@NonNull ContentWriteRequest request,
                                                    @NonNull ContentType targetDoctype) {
    Map<String, Object> targetProperties = new HashMap<>();
    targetProperties.put(ORIGINAL_ATTRIBUTE, DATA_OBJECT);
    Content newCreatedContent = mock(Content.class);
    when(targetDoctype.createByTemplate(request.getParent(), CREATED_CONTENT_NAME, "{3} ({1})", targetProperties)).thenReturn(newCreatedContent);
    return newCreatedContent;
  }

  @NonNull
  private ContentWriteRequest mockRequest(@NonNull Map<String, Object> properties,
                                          @NonNull String parentPath,
                                          @Nullable Content entity) {
    ContentWriteRequest request = mock(ContentWriteRequest.class);
    when(request.getProperties()).thenReturn(properties);
    Content parentContent = mock(Content.class);
    when(parentContent.getPath()).thenReturn(parentPath);
    when(request.getName()).thenReturn(CREATED_CONTENT_NAME);
    when(request.getParent()).thenReturn(parentContent);
    when(request.getEntity()).thenReturn(entity);
    return request;
  }

  private void verifyNotCreated(@NonNull ContentWriteRequest request) {
    verify(request, times(0)).setAttribute(DO_NOTHING, true);
    verify(request, times(0)).setAttribute(any(), any());
  }

  private void verifyCreated(@NonNull ContentWriteRequest request, @NonNull Content newCreatedContent) {
    verify(request, times(1)).setAttribute(DO_NOTHING, true);
    verify(request, times(1)).setAttribute(eq(UPLOADED_DOCUMENTS), eq(of(newCreatedContent)));
  }
}

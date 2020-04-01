package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WorkspaceIdAppendingLinkTransformerTest {

  private WorkspaceIdAppendingLinkTransformer testling;

  @BeforeEach
  void setUp() {
    testling = new WorkspaceIdAppendingLinkTransformer();
  }

  @AfterEach
  void tearDown() {
    CurrentStoreContext.remove();
  }

  @ParameterizedTest
  @MethodSource("provideTransformData")
  void transform(WorkspaceId workspaceId, boolean preview, String expected) {
    CommerceConnection commerceConnection = mock(CommerceConnection.class);

    StoreContext storeContext = StoreContextBuilderImpl.from(commerceConnection, "someSiteId")
            .withWorkspaceId(workspaceId)
            .build();

    CurrentStoreContext.set(storeContext);

    testling.setPreview(preview);

    String actual = testling.transform("/foo/bar/baz", null, null, null, null, false);

    assertThat(actual).isEqualTo(expected);
  }

  private static Stream<Arguments> provideTransformData() {
    return Stream.of(
            Arguments.of(
                    // no preview: don't append parameter
                    WorkspaceId.of("someWorkspaceId"),
                    false,
                    "/foo/bar/baz"
            ),
            Arguments.of(
                    // preview, but no workspace ID set: don't append parameter
                    null,
                    true,
                    "/foo/bar/baz"
            ),
            Arguments.of(
                    // preview with workspace ID set: append parameter
                    WorkspaceId.of("someWorkspaceId"),
                    true,
                    "/foo/bar/baz?workspaceId=someWorkspaceId"
            )
    );
  }
}

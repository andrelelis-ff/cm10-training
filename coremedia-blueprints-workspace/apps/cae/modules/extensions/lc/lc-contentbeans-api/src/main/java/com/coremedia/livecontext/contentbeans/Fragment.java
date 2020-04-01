package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.common.contentbeans.AbstractPage;

/**
 * This is a {@link com.coremedia.blueprint.common.contentbeans.AbstractPage blueprint response} that represents a fragment only. This type of
 * response should be returned by all handlers which do not want to render a complete
 * {@link com.coremedia.blueprint.common.contentbeans.Page page}.
 */
public interface Fragment extends AbstractPage {
}

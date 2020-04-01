package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.login.LoginService;
import com.coremedia.livecontext.ecommerce.ibm.login.WcCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;

class PreviewUserCacheKey extends AbstractCommerceCacheKey<WcCredentials> {

  private final String username;
  private final String password;
  private final LoginService loginService;

  PreviewUserCacheKey(String username, String password, @NonNull StoreContext storeContext, CommerceCache commerceCache,
                      @NonNull LoginService loginService) {
    super(username, storeContext, CONFIG_KEY_PREVIEW_USER_CREDENTIALS, commerceCache);
    this.username = username;
    this.password = password;
    this.loginService = loginService;
  }

  @Override
  public WcCredentials computeValue(Cache cache) {
    return loginService.loginIdentity(username, password, storeContext);
  }

  @Override
  public void addExplicitDependency(WcCredentials wcCredentials) {
  }
}

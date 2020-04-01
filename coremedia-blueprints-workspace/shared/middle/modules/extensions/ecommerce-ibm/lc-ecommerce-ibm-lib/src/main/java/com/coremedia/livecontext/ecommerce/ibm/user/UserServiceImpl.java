package com.coremedia.livecontext.ecommerce.ibm.user;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.user.User;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.user.UserService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider.commerceId;

public class UserServiceImpl implements UserService {

  private WcPersonWrapperService personWrapperService;
  private CommerceCache commerceCache;
  private CommerceBeanFactory commerceBeanFactory;

  @Required
  public void setCommerceCache(CommerceCache commerceCache) {
    this.commerceCache = commerceCache;
  }

  @Required
  public void setPersonWrapperService(WcPersonWrapperService wrapperService) {
    this.personWrapperService = wrapperService;
  }

  @Required
  public void setCommerceBeanFactory(CommerceBeanFactory commerceBeanFactory) {
    this.commerceBeanFactory = commerceBeanFactory;
  }

  // ----- methods that use WCS REST api -----------------------------

  @Override
  public User findCurrentUser() {
    UserContext userContext = CurrentUserContext.get();
    StoreContext storeContext = CurrentStoreContext.get();

    Map<String, Object> personWrapper = commerceCache.get(
            new FindCommercePersonCacheKey("" + userContext.getUserId(), storeContext, userContext,
                    personWrapperService, commerceCache));

    return createUserBeanFor(personWrapper, storeContext);
  }

  // ----- Helper -----------------------------

  protected User createUserBeanFor(Map<String, Object> personWrapper, @NonNull StoreContext storeContext) {
    if (personWrapper == null) {
      return null;
    }

    String userId = DataMapHelper.findString(personWrapper, "userId").orElse(null);
    if (userId == null) {
      return null;
    }

    CommerceId commerceId = commerceId(BaseCommerceBeanType.USER).withExternalId(userId).build();

    User user = (User) commerceBeanFactory.createBeanFor(commerceId, storeContext);
    ((AbstractIbmCommerceBean) user).setDelegate(personWrapper);
    return user;
  }
}

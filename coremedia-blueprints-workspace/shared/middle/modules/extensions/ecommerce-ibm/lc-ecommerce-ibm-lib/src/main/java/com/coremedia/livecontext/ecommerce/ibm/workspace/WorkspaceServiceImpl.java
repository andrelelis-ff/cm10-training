package com.coremedia.livecontext.ecommerce.ibm.workspace;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.Workspace;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.WORKSPACE;
import static com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider.commerceId;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class WorkspaceServiceImpl implements WorkspaceService {

  private WcWorkspaceWrapperService workspaceWrapperService;
  private CommerceBeanFactory commerceBeanFactory;
  private CommerceCache commerceCache;

  @NonNull
  @Override
  public List<Workspace> findAllWorkspaces(@NonNull StoreContext storeContext) {
    UserContext userContext = CurrentUserContext.get();

    WorkspacesCacheKey cacheKey = new WorkspacesCacheKey(storeContext, userContext, workspaceWrapperService,
            commerceCache);

    Map map = commerceCache.get(cacheKey);
    if (map == null) {
      return emptyList();
    }

    Object workspacesObj = map.get("workspaces");
    if (!(workspacesObj instanceof List)) {
      return emptyList();
    }

    return createWorkspaceBeansFor((List<Map>) workspacesObj, storeContext);
  }

  @NonNull
  private List<Workspace> createWorkspaceBeansFor(@NonNull List<Map> list, @NonNull StoreContext context) {
    return list.stream()
            .filter(Objects::nonNull)
            .map(workspaceMap -> createWorkspaceBeanFor(workspaceMap, context))
            .filter(Objects::nonNull)
            .collect(toList());
  }

  @Nullable
  private Workspace createWorkspaceBeanFor(@NonNull Map map, @NonNull StoreContext context) {
    CommerceId commerceId = commerceId(WORKSPACE).withExternalId((String) map.get("id")).build();
    Workspace workspace = (Workspace) commerceBeanFactory.createBeanFor(commerceId, context);
    ((AbstractIbmCommerceBean) workspace).setDelegate(map);
    return workspace;
  }

  @Required
  public void setWorkspaceWrapperService(WcWorkspaceWrapperService workspaceWrapperService) {
    this.workspaceWrapperService = workspaceWrapperService;
  }

  @Required
  public void setCommerceCache(CommerceCache commerceCache) {
    this.commerceCache = commerceCache;
  }

  @Required
  public void setCommerceBeanFactory(CommerceBeanFactory commerceBeanFactory) {
    this.commerceBeanFactory = commerceBeanFactory;
  }
}

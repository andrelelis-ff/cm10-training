package com.coremedia.blueprint.cae.web.links;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.objectserver.web.links.TokenResolver;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import org.springframework.core.annotation.Order;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A {@link TokenResolver} that resolves tokens against the {@link SettingsService}.
 */
@Named
@Order(10)
public class SettingsTokenResolver implements TokenResolver {

  // --- construct and configure ------------------------------------

  private SettingsService settingsService;

  @Inject
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }


  // --- TokenResolver ----------------------------------------------

  /**
   * Resolve the token.
   * <p>
   * Fetches additional settings beans from the request, so the resolution may
   * be more successful than you would expect.
   * <p>
   * {@link TokenResolver#resolveToken(String, Object, HttpServletRequest)
   * supports only simple tokens.  You can denote nested settings by a
   * "."-separated compound name.  In this case the single names of the struct
   * properties must not contain ".", though.
   */
  @Override
  public String resolveToken(String token, Object bean, HttpServletRequest request) {
    if (token == null) {
      return null;
    }

    Object[] beans = grabBeans(bean, request);

    // 1. Check for a direct setting
    String directSetting = settingsService.setting(token, String.class, beans);
    if (directSetting != null) {
      return directSetting;
    }

    // 2. Interpret the token as nested setting
    List<String> names = Splitter.on('.').omitEmptyStrings().splitToList(token);
    return names.size() > 1 ? settingsService.nestedSetting(names, String.class, beans) : null;
  }


  // --- internal ---------------------------------------------------

  /**
   * Policy is "desperate", grab additional beans to search for settings.
   */
  @VisibleForTesting
  static Object[] grabBeans(Object bean, HttpServletRequest request) {
    Object self = request != null ? request.getAttribute("self") : null;
    Object page = request != null ? RequestAttributeConstants.getPage(request) : null;
    LinkedHashSet<Object> beans = new LinkedHashSet<>(3);
    beans.add(bean);
    beans.add(self);
    beans.add(page);
    return beans.toArray(new Object[beans.size()]);
  }
}

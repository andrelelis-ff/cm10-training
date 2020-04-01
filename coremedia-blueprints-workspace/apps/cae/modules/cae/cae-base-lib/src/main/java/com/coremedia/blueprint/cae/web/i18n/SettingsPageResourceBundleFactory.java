package com.coremedia.blueprint.cae.web.i18n;

import com.coremedia.cap.util.ContentCacheKey;
import com.coremedia.cap.util.StructUtil;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cache.Cache;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.user.User;
import com.google.common.base.Function;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.ResourceBundle;

import static com.google.common.collect.Lists.transform;

/**
 * Derives the ResourceBundle from the beans' settings.
 * <p>
 * Backward compatible strategy for existing projects.
 * Supports only settings of the root channel.
 * Does not support developer features.
 *
 * @deprecated Move resource bundles to resourceBundles, and use the {@link LinklistPageResourceBundleFactory}.
 */
@Deprecated
public class SettingsPageResourceBundleFactory implements PageResourceBundleFactory {
  private static final String CMLINKABLE_LOCALSETTINGS = "localSettings";
  private static final String CMLINKABLE_LINKEDSETTINGS = "linkedSettings";

  private Cache cache;


  // --- configure --------------------------------------------------

  public void setCache(Cache cache) {
    this.cache = cache;
  }


  // --- PageResourceBundleFactory ----------------------------------

  @Override
  public ResourceBundle resourceBundle(Page page, User developer) {
    return fetchResourceBundle(page.getNavigation());
  }

  @Override
  public ResourceBundle resourceBundle(Navigation navigation, User developer) {
    return fetchResourceBundle(navigation);
  }


  // --- internal ---------------------------------------------------

  private ResourceBundle fetchResourceBundle(Navigation navigation) {
    CMNavigation rootNavigation = navigation.getRootNavigation();
    Content content = rootNavigation.getContent();
    return cache!=null ? cache.get(new ResourceBundleCacheKey(content)) : buildResourceBundle(content);
  }

  private static ResourceBundle buildResourceBundle(Content content) {
    Struct struct = content.getStruct(CMLINKABLE_LOCALSETTINGS);
    if(content.getType().getDescriptor(CMLINKABLE_LINKEDSETTINGS) != null) {
      List<Struct> structList = transform(content.getLinks(CMLINKABLE_LINKEDSETTINGS), new CMSettingsContentToStruct());
      struct = StructUtil.mergeStructList(struct, structList);
    }
    return struct==null ? EmptyResourceBundle.emptyResourceBundle() : CapStructHelper.asResourceBundle(struct);
  }

  private static class CMSettingsContentToStruct implements Function<Content, Struct> {
    private static final String CMSETTINGS_SETTINGS = "settings";

    @Override
    public Struct apply(Content input) {
      return input!=null ? input.getStruct(CMSETTINGS_SETTINGS) : null;
    }
  }

  private static class ResourceBundleCacheKey extends ContentCacheKey<ResourceBundle> {
    public ResourceBundleCacheKey(@NonNull Content content) {
      super(content);
    }

    @Override
    public ResourceBundle evaluate(Cache cache) throws Exception {
      return buildResourceBundle(getContent());
    }
  }
}

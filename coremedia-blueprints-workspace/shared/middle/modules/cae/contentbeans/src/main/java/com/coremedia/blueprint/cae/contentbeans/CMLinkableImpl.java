package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMSettings;
import com.coremedia.blueprint.common.contentbeans.CMViewtype;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructBuilderMode;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generated extension class for immutable beans of document type "CMLinkable".
 */
public abstract class CMLinkableImpl extends CMLinkableBase {
  @Override
  public List<CMContext> getContexts() {
    return getContextStrategy().findContextsFor(this);
  }

  @Override
  public Collection<? extends Navigation> getRootNavigations() {
    Set<Navigation> roots = new HashSet<>();
    for (CMNavigation parent : getContexts()) {
      roots.add(parent.getRootNavigation());
    }
    return roots;
  }

  protected Struct getLocalAndLinkedSettings() {
    Struct localSettings = getLocalSettings();
    List<? extends CMSettings> linkedSettings = getLinkedSettings();
    if(!linkedSettings.isEmpty()) {
      // only instantiate struct builder once. only clone root struct if necessary.
      StructBuilder structBuilder = localSettings.builder();
      //tell structbuilder to allow merging of structs
      structBuilder = structBuilder.mode(StructBuilderMode.LOOSE);
      for (CMSettings settings : linkedSettings) {
        structBuilder.defaultTo(settings.getSettings());
      }
      localSettings = structBuilder.build();
    }

    return localSettings;
  }

  @Override
  public String getSegment() {
    return getUrlPathFormattingHelper().getVanityName(getContent());
  }

  @Override
  public String getViewTypeName() {
    CMViewtype viewType = getViewtype();
    if (viewType != null) {
      String name = viewType.getLayout();
      if (StringUtils.hasLength(name)) {
        return name;
      }
    }
    return null;
  }

  @Override
  public boolean isOpenInNewTab() {
    return false;
  }
}

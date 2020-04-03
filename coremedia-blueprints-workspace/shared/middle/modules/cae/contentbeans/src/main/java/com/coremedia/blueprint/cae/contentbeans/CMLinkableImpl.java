package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.*;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.common.InvalidPropertyValueException;
import com.coremedia.cap.common.NoSuchPropertyDescriptorException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructBuilderMode;
import com.coremedia.objectserver.beans.UnexpectedBeanTypeException;
import org.springframework.util.StringUtils;

import java.util.*;

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

  /**
   * {@inheritDoc}
   */
  @Override
  public Struct getPRJExtendedFields() {
    Struct struct = getContent().getStruct(PRJ_EXTENDED_FIELDS);
    return struct != null ? struct : getContent().getRepository().getConnection().getStructService().
            emptyStruct();
  }

  /**
   * {@inheritDoc}
   * <br/>
   * If an error is encountered at some point of reading the list of sources, only a list with partial, up-to-now
   * results is returned.
   */
  @Override
  public List<CMSymbol> getSources() {
    List<CMSymbol> sourceDocuments = new ArrayList<>();
    Struct prjExtendedFields = getPRJExtendedFields();
    try {
      List<Struct> sources = prjExtendedFields.getStructs(SOURCES_STRUCT_PROPERTY);
      for (Struct source : sources) {
        Content sourceContent = source.getLink(LINK_STRUCT_PROPERTY);
        sourceDocuments.add(getContentBeanFactory().createBeanFor(sourceContent, CMSymbol.class));
      }
    } catch (NoSuchPropertyDescriptorException e) {
      // no error, simply no sources edited
    } catch (InvalidPropertyValueException | UnexpectedBeanTypeException e) {
      //LOG.warn("unexpected value at struct property {}.{} for document id {}", PRJ_EXTENDED_FIELDS, SOURCES_STRUCT_PROPERTY, getContentId());
    }
    return sourceDocuments;
  }
}

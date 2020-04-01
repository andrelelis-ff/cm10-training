package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Objects;

/**
 * An abstract {@link com.coremedia.livecontext.fragment.resolver.ExternalReferenceResolver} which is responsible
 * to resolve external references with a certain prefix.
 */
public abstract class ExternalReferenceResolverBase implements ExternalReferenceResolver {

  private static final Logger LOG = LoggerFactory.getLogger(ExternalReferenceResolverBase.class);

  private final String supportedReferencePrefix;

  protected ContextHelper contextHelper;
  protected ContentBeanFactory contentBeanFactory;
  protected DataViewFactory dataViewFactory;
  protected ContentRepository contentRepository;

  /**
   * @param supportedReferencePrefix the prefix of supported external reference values or the empty string to support
   *                                 values independently from their prefix
   */
  protected ExternalReferenceResolverBase(@NonNull String supportedReferencePrefix) {
    this.supportedReferencePrefix = supportedReferencePrefix;
  }

  @Override
  public boolean include(@Nullable FragmentParameters fragmentParameters) {
    if (fragmentParameters == null) {
      return false;
    }
    String referenceInfo = stripPrefixFromExternalReference(fragmentParameters);
    return referenceInfo != null && include(fragmentParameters, referenceInfo);
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Required
  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }

  @Required
  public void setContextHelper(ContextHelper contextHelper) {
    this.contextHelper = contextHelper;
  }

  @Required
  public void setContentRepository(@NonNull ContentRepository contentRepository) {
    Objects.requireNonNull(contentRepository);
    this.contentRepository = contentRepository;
  }

  /**
   * Returns whether this resolver is responsible for resolving external fragments that start with the resolver's
   * supported external reference prefix and have the given parameters.
   *
   * <p>The default implementation of {@link com.coremedia.livecontext.fragment.resolver.ExternalReferenceResolverBase}
   * returns true and may be overridden in concrete implementations.
   *
   * @param fragmentParameters fragment request parameters
   * @param referenceInfo {@link com.coremedia.livecontext.fragment.FragmentParameters#getExternalRef()
   *                      external reference} with prefix of this resolver removed
   * @return true if this resolver is responsible to resolve the given external reference
   */
  protected boolean include(@NonNull FragmentParameters fragmentParameters, @NonNull String referenceInfo) {
    return true;
  }

  @Override
  @Nullable
  public LinkableAndNavigation resolveExternalRef(@NonNull FragmentParameters fragmentParameters, @NonNull Site site) {
    if (!include(fragmentParameters)) {
      return null;
    }
    String referenceInfo = stripPrefixFromExternalReference(fragmentParameters);
    return referenceInfo == null ? null : resolveExternalRef(fragmentParameters, referenceInfo, site);
  }

  /**
   * Returns the {@link LinkableAndNavigation} that is identified by the
   * {@link com.coremedia.livecontext.fragment.FragmentParameters#getExternalRef() external reference} of the given
   * {@link com.coremedia.livecontext.fragment.FragmentParameters}.
   *
   * @param fragmentParameters the fragment request parameters with external reference
   * @param referenceInfo {@link com.coremedia.livecontext.fragment.FragmentParameters#getExternalRef()
   *                      external reference} with prefix of this resolver removed
   * @param site the site to resolve the reference in
   * @return the resolved {@link LinkableAndNavigation} or null if not found
   */
  @Nullable
  protected abstract LinkableAndNavigation resolveExternalRef(@NonNull FragmentParameters fragmentParameters,
                                                              @NonNull String referenceInfo,
                                                              @NonNull Site site);

  private String stripPrefixFromExternalReference(@NonNull FragmentParameters fragmentParameters) {
    String externalRef = fragmentParameters.getExternalRef();
    return externalRef != null && externalRef.startsWith(supportedReferencePrefix)
            ? externalRef.substring(supportedReferencePrefix.length())
            : null;
  }

  /**
   * Return the given content as a bean
   * @param cnt the content object
   * @return the bean
   */
  protected CMLinkable asBean(Content cnt) {
    if (cnt == null) {
      return null;
    }

    try {
      CMLinkable result = contentBeanFactory.createBeanFor(cnt, CMLinkable.class);
      result = dataViewFactory.loadCached(result, null);
      return result;
    }
    catch (Exception e) {
      LOG.error("Cannot convert content to bean", e);
      return null;
    }
  }

  /**
   * Return navigation for linkable
   * @param linkable the content object
   * @return the navigation content
   */
  protected Content getNavigationForLinkable(Content linkable){
    if (linkable != null) {
      // Determine context of linkable and set it as navigation
      CMNavigation navigationBean = contextHelper.contextFor(asBean(linkable));
      if (navigationBean != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("ContentPath externalRef resolved context");
        }
        return navigationBean.getContent();
      }
    }
    return null;
  }
}

package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.cap.multisite.Site;
import com.coremedia.common.util.Predicate;
import com.coremedia.livecontext.fragment.FragmentParameters;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Interface to be implemented by beans that can resolve a {@link LinkableAndNavigation} from an
 * {@link com.coremedia.livecontext.fragment.FragmentParameters#getExternalRef() external reference}.
 *
 * <p>ExternalReferenceResolvers are used to answer
 * {@link com.coremedia.livecontext.fragment.FragmentPageHandler#handleFragment fragment requests} from the commerce
 * system, triggered by {@code lc:include} tags with {@code externalRef} attribute.
 *
 * <p>This interface extends {@link Predicate}<{@link com.coremedia.livecontext.fragment.FragmentParameters}>. Its
 * {@link #include} method returns true if this resolver is responsible to resolve references for the given parameters.
 * It returns false if {@link com.coremedia.livecontext.fragment.FragmentParameters#getExternalRef()} is null.
 *
 * <p>Note that the actual {@link #resolveExternalRef} method may still return null even if {@link #include} returned
 * true for the same parameters. In that case, the referenced {@link LinkableAndNavigation} just does not exist.
 */
public interface ExternalReferenceResolver extends Predicate<FragmentParameters> {

  String CONTENT_ID_FRAGMENT_PREFIX = "cm-";

  /**
   * Returns the {@link LinkableAndNavigation} that is identified by the
   * {@link com.coremedia.livecontext.fragment.FragmentParameters#getExternalRef() external reference} of the given
   * {@link com.coremedia.livecontext.fragment.FragmentParameters}.
   *
   * <p>This method returns null if this resolver cannot resolve the external reference, for example if the referenced
   * {@link LinkableAndNavigation} does not exist or this resolver is not responsible to resolve external references
   * for the given parameters. In the latter case, method {@link #include} returns false when called with the given
   * parameters.
   *
   * @param fragmentParameters the fragment request parameters with external reference
   * @param site the site to resolve the reference in
   * @return the resolved {@link LinkableAndNavigation} or null if not found
   */
  @Nullable
  LinkableAndNavigation resolveExternalRef(@NonNull FragmentParameters fragmentParameters, @NonNull Site site);
}

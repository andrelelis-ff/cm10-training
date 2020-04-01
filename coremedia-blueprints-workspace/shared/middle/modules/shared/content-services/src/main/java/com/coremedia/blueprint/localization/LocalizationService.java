package com.coremedia.blueprint.localization;

import com.coremedia.cap.util.StructUtil;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructService;
import com.coremedia.common.graph.DirectedGraph;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Coordinates CMResourceBundle fallback strategies.
 * <p>
 * The LocalizationService features multiple axis of fallbacks for resources
 * of CMResourceBundles:
 * <ol>
 * <li>The usual Locale semantics of language, country, variant</li>
 * <li>The master relation of the Blueprint CMLocalizable semantics</li>
 * <li>Multiple bundles</li>
 * </ol>
 * The resources are merged from the locale matching variants of the given
 * bundles (S. {@link ContentSiteAspect#getVariantsByLocale()}.  Generally,
 * a more specific locale takes precedence over the order of the given bundles.
 * If your bundles are master-linked appropriately, there is no contradiction
 * between Locales and master relation, so that precedence does not matter.
 * Otherwise, a more specific Locale takes precedence over a shorter master
 * path.
 */
public class LocalizationService {
  private static final String CM_RESOURCE_BUNDLE = "CMResourceBundle";
  private static final String MASTER = "master";
  private static final String MASTER_VERSION = "masterVersion";
  private static final String LOCALE = "locale";

  private static final String RESOURCE_BUNDLE_NAME_SUFFIX = ".properties";
  private static final String RESOURCE_BUNDLE_NAME_ENDSWITH_PATTERN = "(_.*)?\\.properties";

  private static final Locale GLOBAL = new Locale("");

  private final SitesService sitesService;
  private final StructService structService;
  private final BundleResolver bundleResolver;


  // --- construct and configure ------------------------------------

  public LocalizationService(@NonNull StructService structService,
                             @NonNull SitesService sitesService,
                             @NonNull BundleResolver bundleResolver) {
    this.structService = structService;
    this.sitesService = sitesService;
    this.bundleResolver = bundleResolver;
  }


  // --- features ---------------------------------------------------

  /**
   * Returns the resources of the given bundles.
   * <p>
   * The resources are merged from the variants of the bundles that match the
   * given locale and their master variants.  I.e. each bundle in the bundles
   * collection represents the set of all its variants, and two variants of the
   * same bundle (like myBundle_de and myBundle_de_DE) in the bundles
   * collection are considered as duplicate.
   * <p>
   * Explicit fallback bundles (third parameter) are guaranteed to be used as
   * a fallback. If the given explicit bundles are not already contained in the
   * list of merged variants (see previous paragraph) they will be appended to the
   * fallback list, so their localization is used if no other resource bundle
   * contains the requested localization.
   */
  @NonNull
  public Struct resources(@NonNull Collection<Content> bundles, @Nullable Locale locale, @NonNull Collection<Content> explicitFallbackBundles) {
    checkAreBundles(bundles);
    List<Struct> localizations = new ArrayList<>();
    List<Content> fallback = localizationFallback(locale!=null ? locale : GLOBAL, variantsMaps(bundles));
    fallback.addAll(explicitFallbackBundles.stream().filter(bundle -> !fallback.contains(bundle)).collect(Collectors.toList()));
    for (Content bundle : fallback) {
      Struct l10ns = bundleResolver.resolveBundle(bundle);
      if (l10ns != null) {
        localizations.add(l10ns);
      }
    }
    Struct result = StructUtil.mergeStructList(localizations);
    return result!=null ? result : structService.emptyStruct();
  }

  /**
   * Convenience variant of {@link #resources(Collection, Locale, Collection)} with
   * no explicit fallback bundles.
   */
  @NonNull
  public Struct resources(@NonNull Collection<Content> bundles, @Nullable Locale locale) {
    return resources(bundles, locale, Collections.emptyList());
  }

  /**
   * Convenience variant of {@link #resources(Collection, Locale)} for a single
   * bundle.
   */
  @NonNull
  public final Struct resources(@NonNull Content bundle, @Nullable Locale locale) {
    return resources(Collections.singletonList(bundle), locale);
  }

  /**
   * Set the master links in a set of resource bundles.
   * <p>
   * The set of bundles is determined by naming conventions:
   * <ul>
   *   <li>All bundles must reside in the same folder.</li>
   *   <li>The bundle names are:
   *   &lt;commonBaseName&gt;&lt;_optional_locale&gt;.properties</li>
   * </ul>
   * The naming convention (esp. the .properties suffix) is motivated by our
   * frontend workflow.
   * <p>
   * The hierarchy is derived from the locales in the names.  The dedicated
   * root bundle is handled explicitely, so that you can make
   * mybundle_en.properties master of mybundle_de.properties (and even of
   * mybundle.properties, which would be confusing, though).
   * <p>
   * Be aware, that this is a persistent operation, so you need WRITE access
   * to all the bundles.  The operation fails completely if one bundle is not
   * writable.
   *
   * @param rootBundle the root bundle
   */
  public void hierarchizeResourceBundles(Content rootBundle) {
    checkIsA(rootBundle, CM_RESOURCE_BUNDLE);
    String name = resourceBundleBaseName(rootBundle.getName());
    String pattern = name + RESOURCE_BUNDLE_NAME_ENDSWITH_PATTERN;
    //noinspection ConstantConditions
    Set<Content> bundles = rootBundle.getParent().getChildDocumentsMatching(pattern);  // NOSONAR parent ensured by checkIsA
    if (!bundles.contains(rootBundle)) {
      throw new IllegalArgumentException("Cannot derive the set of bundles of " + rootBundle + ", does not follow our naming conventions.");
    }
    checkAreBundles(bundles);
    Collection<Content> checkedOutByThis = checkOut(bundles);
    Collection<Content> changed = new HashSet<>();
    initializeLocales(bundles, changed);
    masterizeBundles(rootBundle, bundles, checkedOutByThis, changed);
    checkinOrRevert(checkedOutByThis, changed);
  }


  // --- internal ---------------------------------------------------

  @VisibleForTesting
  List<Content> localizationFallback(Locale locale, List<Map<Locale, Content>> vbls) {
    List<Content> fallback = new ArrayList<>();
    // Fetch locale-matching variants of all bundles, precedence by
    // 1. most specific locale
    // 2. order of vbls
    boolean[] foundByLocale = new boolean[vbls.size()];
    for (Locale lcl : deriveLocales(locale)) {
      for (int i=0; i<vbls.size(); ++i) {
        // If we have already found a locale specific variant, do not add the
        // global variant here, because it will be handled along the master
        // chain.  If there is no locale specific variant though, the global
        // variant is the appropriate point of entry for this bundle.
        if (!GLOBAL.equals(lcl) || !foundByLocale[i]) {
          Content content = vbls.get(i).get(lcl);
          if (content!=null) {
            fallback.add(content);
            foundByLocale[i] = true;
          }
        }
      }
    }
    // Follow the master links of the variants selected so far, breadth first
    for (int i=0; i<fallback.size(); ++i) {
      Content master = fallback.get(i).getLink(MASTER);
      if (master!=null && !fallback.contains(master)) {
        fallback.add(master);
      }
    }
    return fallback;
  }

  @VisibleForTesting
  List<Map<Locale, Content>> variantsMaps(Collection<Content> contents) {
    List<Map<Locale, Content>> variantsByLocales = new ArrayList<>();
    for (Content content : contents) {
      // In this context contents are equivalent wrt. their variants sets,
      // so there may be "duplicates" in the input collection.  Omit them.
      if (!contains(variantsByLocales, content)) {
        variantsByLocales.add(sitesService.getContentSiteAspect(content).getVariantsByLocale());
      }
    }
    return variantsByLocales;
  }

  private boolean contains(Collection<Map<Locale, Content>> variantsByLocales, Content content) {
    for (Map<Locale, Content> vbl : variantsByLocales) {
      if (vbl.values().contains(content)) {
        return true;
      }
    }
    return false;
  }

  private Collection<Locale> deriveLocales(Locale locale) {
    // LinkedHashSet to preserve order and omit duplicates
    LinkedHashSet<Locale> locales = new LinkedHashSet<>();
    locales.add(new Locale(locale.getLanguage(), locale.getCountry(), locale.getVariant()));
    locales.add(new Locale(locale.getLanguage(), locale.getCountry()));
    locales.add(new Locale(locale.getLanguage()));
    locales.add(GLOBAL);
    return locales;
  }

  private static String resourceBundleBaseName(String name) {
    name = resourceBundleNameWithoutSuffix(name);
    int index = name.indexOf('_');
    return index<0 ? name : name.substring(0, index);
  }

  private static Locale localeFromName(String name) {
    name = resourceBundleNameWithoutSuffix(name);
    if (name.contains("_")) {
      String localeStr = name.substring(name.indexOf('_') + 1);
      return LocaleUtils.toLocale(localeStr);
    }
    return GLOBAL;
  }

  private static String resourceBundleNameWithoutSuffix(String name) {
    if (!name.endsWith(RESOURCE_BUNDLE_NAME_SUFFIX)) {
      throw new IllegalArgumentException("Unexpected resource bundle name \"" + name +"\", don't know how to extract base name.");
    }
    return name.substring(0, name.length() - RESOURCE_BUNDLE_NAME_SUFFIX.length());
  }

  /**
   * Initialize locales.
   * <p>
   * If the bundle has no locale yet, derive it from its name.
   * The name must follow the pattern of our frontend workflow:
   * basename[_&lt;locale&gt;].properties
   */
  private static void initializeLocales(Collection<Content> bundles, Collection<Content> changed) {
    for (Content bundle : bundles) {
      if (StringUtils.isEmpty(bundle.getString(LOCALE))) {
        bundle.set(LOCALE, localeFromName(bundle.getName()).toLanguageTag());
        changed.add(bundle);
      }
    }
  }

  /**
   * Constitute the master hierarchy according to the bundles' locales.
   */
  private static void masterizeBundles(Content rootBundle, Collection<Content> bundles, Collection<Content> checkedOutByThis, Collection<Content> changed) {
    DirectedGraph<Content> localeGraph = new DirectedGraph<>(new LocaleComparator(rootBundle), bundles);
    recMasterizeBundles(localeGraph.roots(), localeGraph, changed, checkedOutByThis);
  }

  /**
   * Recursively masterize the bundles.
   * <p>
   * The master version of a child bundle depends on whether we change the
   * master bundle here.  In order to know that, we must process the master
   * first and cannot simply iterate over all bundles arbitrarily.  So we do
   * it recursively, starting from the masters.
   */
  private static void recMasterizeBundles(Collection<Content> bundles, DirectedGraph<Content> localeGraph, Collection<Content> changed, Collection<Content> checkedOutByThis) {
    for (Content bundle : bundles) {
      Collection<Content> masters = localeGraph.heads(bundle);
      assert masters.size() <= 1 : "Multiple master locales, bug in LocaleComparator or in DirectedGraph!";
      Content master = masters.isEmpty() ? null : masters.iterator().next();
      Version masterVersion = null;
      if (master!=null) {
        // I guess this masterVersion logic needs some explanation:
        // We have checked out all affected bundles proactively, in order to
        // do the whole masterization kind of transactionally.  If we do not
        // actually change the master though, we will revert it afterwards.
        boolean willBeReverted = checkedOutByThis.contains(master) && !changed.contains(master);
        masterVersion = willBeReverted ? revertedVersion(master) : master.getWorkingVersion();
      }
      if (updateMasterReference(bundle, master, masterVersion)) {
        changed.add(bundle);
      }

      recMasterizeBundles(localeGraph.tails(bundle), localeGraph, changed, checkedOutByThis);
    }
  }

  private static boolean updateMasterReference(Content bundle, Content master, Version masterVersion) {
    // Update only properties that actually changed, in order to spare
    // events, invalidations and duplicate versions.

    HashMap<String, Object> masterProperties = new HashMap<>();
    if (!Objects.equals(master, bundle.getLink(MASTER))) {
      masterProperties.put(MASTER, master!=null ? Collections.singletonList(master) : Collections.emptyList());
    }

    int mv = masterVersion!=null ? IdHelper.parseVersionId(masterVersion.getId()) : Integer.MIN_VALUE;
    if (mv!=bundle.getInt(MASTER_VERSION)) {
      masterProperties.put(MASTER_VERSION, mv);
    }

    boolean hasChanged = !masterProperties.isEmpty();
    if (hasChanged) {
      bundle.setProperties(masterProperties);
    }
    return hasChanged;
  }

  private static void checkAreBundles(Collection<Content> contents) {
    for (Content content : contents) {
      checkIsA(content, CM_RESOURCE_BUNDLE);
    }
  }

  private static void checkIsA(Content content, String typeName) {
    if (content==null || !content.getType().isSubtypeOf(typeName) || !content.isInProduction()) {
      throw new IllegalArgumentException(content + " is no " + typeName + " or deleted.");
    }
  }

  private static Version revertedVersion(Content content) {
    assert content.isCheckedOut() : "Illegal internal usage of getRevertVersion";
    Version version = content.getCheckedOutVersion();
    if (version == null) {
      version = content.getWorkingVersion();
    }
    return version;
  }

  /**
   * Ensure that all the contents are checked out by the current user.
   * <p>
   * Returns only those contents that are checked out by this method and thus
   * enables the invoker to preserve or restore the previous document state.
   *
   * @return the contents that have been checked out in this method (a subset of the incoming contents)
   * @throws IllegalStateException if a content neither is nor can be checked out by the current user
   */
  @NonNull
  private static Collection<Content> checkOut(@NonNull Collection<Content> contents) {
    Collection<Content> toBeCheckedIn = new HashSet<>();
    for (Content content : contents) {
      if (content.isCheckedIn()) {
        try {
          content.checkOut();
          toBeCheckedIn.add(content);
        } catch (Exception e) {
          toBeCheckedIn.forEach(Content::revert);
          throw new IllegalStateException("Cannot checkout " + content + ", probably due to a concurrent modification", e);
        }
      } else if (!content.isCheckedOutByCurrentSession()) {
        toBeCheckedIn.forEach(Content::revert);
        throw new IllegalStateException("Cannot checkout " + content + ", already checked out by other user");
      }
    }
    return toBeCheckedIn;
  }

  private static void checkinOrRevert(Collection<Content> checkedOutByThis, Collection<Content> changed) {
    for (Content content : checkedOutByThis) {
      if (changed.contains(content)) {
        content.checkIn();
      } else {
        content.revert();
      }
    }
  }
}

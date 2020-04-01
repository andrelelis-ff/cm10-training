package com.coremedia.blueprint.coderesources;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.user.User;
import com.coremedia.cap.util.DeveloperPaths;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;

/**
 * Lookup the theme for contents.
 * <p>
 * The optional developer argument of some methods allows to switch the actual
 * result to a developer specific variant for work-in-progress frontend
 * development.
 */
public class ThemeService {
  private static final String CM_THEME = "CMTheme";
  private static final String CM_THEME_TEMPLATESETS = "templateSets";
  private static final String CM_THEME_VIEWREPOSITORYNAME = "viewRepositoryName";
  private static final String CM_NAVIGATION = "CMNavigation";
  private static final String CM_NAVIGATION_THEME = "theme";

  private final TreeRelation<Content> treeRelation;


  // --- Construct and configure ------------------------------------

  public ThemeService(@NonNull TreeRelation<Content> treeRelation) {
    this.treeRelation = treeRelation;
  }


  // --- Features ---------------------------------------------------

  /**
   * Returns the navigation's theme.
   * <p>
   * If the navigation has no theme, its parent's theme is returned.
   * Returns null if there is no theme up the navigation hierarchy.
   *
   * @param content must be of type CMNavigation
   * @param developer Use the developer's variant rather than the production theme.
   */
  @Nullable
  public Content theme(@NonNull Content content, @Nullable User developer) {
    // Technically this would work even if content was no CMNavigation,
    // but for other types the result would depend on the content being in
    // the tree relation, which would not make sense.
    // Better make the contract obvious, and accept only CMNavigation.
    checkIsA(content, CM_NAVIGATION);
    Content theme = directTheme(Lists.reverse(treeRelation.pathToRoot(content)), null);
    return developer==null || theme==null ? theme : new DeveloperPaths(developer).substitute(theme);
  }

  /**
   * Returns the first theme found in the given fallback contents list.
   * <p>
   * In the plain CMNavigation content world the fallback logic is backed by
   * the TreeRelation.  You do not need to care about it, but simply use
   * {@link #theme(Content, User)}.
   * <p>
   * However, if you use Navigation implementations that are not backed by
   * content, you cannot simply delegate down to this content service.  This
   * method enables you to control the fallback logic by providing a
   * precomputed fallback list according to your model.
   *
   * @param contents must be of type CMNavigation
   * @param developer Use the developer's variant rather than the production theme.
   */
  @Nullable
  public Content directTheme(@NonNull List<Content> contents, @Nullable User developer) {
    for (Content content : contents) {
      if (content!=null && content.getType().isSubtypeOf(CM_NAVIGATION)) {
        Content theme = content.getLink(CM_NAVIGATION_THEME);
        if (theme!=null) {
          return developer==null ? theme : new DeveloperPaths(developer).substitute(theme);
        }
      }
    }
    return null;
  }

  /**
   * Returns the theme's template sets.
   *
   * @param theme the theme
   * @param developer Use the developer's variants rather than the theme's original templates.
   */
  public List<Content> templateSets(@NonNull Content theme, @Nullable User developer) {
    checkIsA(theme, CM_THEME);
    if (developer==null) {
      return theme.getLinks(CM_THEME_TEMPLATESETS);
    } else {
      DeveloperPaths dev = new DeveloperPaths(developer);
      return dev.substitute(dev.substitute(theme).getLinks(CM_THEME_TEMPLATESETS));
    }
  }

  /**
   * Returns the theme's view repository name.
   * <p>
   * This is deprecated, since it uses the theme's viewRepositoryName property,
   * which assumes some template set to exist apart from this theme.  Nowadays,
   * a theme should be self contained and bring its own templates in the
   * templateSets property.
   *
   * @deprecated Enhance your theme with templates and use {@link #templateSets(Content, User)}
   */
  @Deprecated
  public String viewRepositoryName(@NonNull Content theme, @Nullable User developer) {
    checkIsA(theme, CM_THEME);
    if (developer!=null) {
      theme = new DeveloperPaths(developer).substitute(theme);
    }
    return theme.getString(CM_THEME_VIEWREPOSITORYNAME);
  }

  /**
   * Returns the developer's variant of a theme related content.
   * <p>
   * Or the original content if there is no development variant.
   */
  @NonNull
  public Content developerVariant(@NonNull Content content, @Nullable User developer) {
    return developer==null ? content : new DeveloperPaths(developer).substitute(content);
  }

  /**
   * Maps the given originalPath to a user (frontend developer) specific path.
   * <p>
   * The result path may not yet exist in the content repository.  The result
   * is null if the computed path would not be a development path.
   */
  @Nullable
  public String developerPath(@NonNull String originalPath, @Nullable User developer) {
    return developer==null ? originalPath : new DeveloperPaths(developer).substitutePath(originalPath);
  }


  // --- internal ---------------------------------------------------

  private static void checkIsA(Content content, String contentType) {
    if (!content.getType().isSubtypeOf(contentType)) {
      throw new IllegalArgumentException(content + " is no " + contentType);
    }
  }

}

package com.coremedia.livecontext.view;

import com.coremedia.blueprint.common.contentbeans.AbstractPage;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.blueprint.common.layout.PageGridRow;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.livecontext.fragment.FragmentContextProvider;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.objectserver.view.TextView;
import com.coremedia.objectserver.view.ViewException;
import com.coremedia.objectserver.view.ViewUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Optional.empty;

/**
 * A programmed view to retrieve all fragments from a page and deliver it as a json
 */
public class PrefetchFragmentsView implements TextView {

  private static final Logger LOG = LoggerFactory.getLogger(PrefetchFragmentsView.class);

  private static final String DEFAULT_VIEW_IDENTIFIER = "DEFAULT";

  private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

  private static final Gson GSON = new Gson();
  private static final String MATRIX_SEPERATOR = ";";
  private static final String PAGE_KEY_PROPERTY = "pageKey";
  private static final String FRAGMENTS_PROPERTY = "fragments";
  private static final String FRAGMENT_KEY_PROPERTY = "fragmentKey";
  private static final String PAYLOAD_PROPERTY = "payload";
  private static final String DEFAULT_CHANNEL_VIEW = "asFragment";

  @Inject
  private PrefetchFragmentsConfigReader configReader;

  @Override
  public void render(Object bean, String view, Writer out, @NonNull HttpServletRequest request,
                     @NonNull HttpServletResponse response) {
    try {
      Page page = getPage(bean, request);

      JsonArray fragmentsJson = new JsonArray();

      renderPredefinedViews(bean, page, request, response, fragmentsJson);

      renderPlacements(page, request, response, fragmentsJson);

      writeResponse(view, out, request, response, page, fragmentsJson);
    } catch (RuntimeException e) {
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      } catch (IOException e1) {
        LOG.debug("Cannot send error to client.", e1);
      }
    }
  }

  @NonNull
  private Page getPage(Object bean, @NonNull HttpServletRequest request) {
    if (bean instanceof Page) {
      return (Page) bean;
    }

    Object pageObject = request.getAttribute(ContextHelper.ATTR_NAME_PAGE);
    if (pageObject instanceof Page) {
      return (Page) pageObject;
    }

    throw new IllegalArgumentException(pageObject + " from request attribute '" + ContextHelper.ATTR_NAME_PAGE +
            "' is not a Page object.");
  }

  private void writeResponse(String view, @NonNull Writer out, @NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response, @NonNull Page page,
                             @NonNull JsonArray fragmentsJson) {
    JsonObject rootJson = new JsonObject();
    rootJson.addProperty(PAGE_KEY_PROPERTY, createPageKey(request));
    rootJson.add(FRAGMENTS_PROPERTY, fragmentsJson);

    try {
      response.setContentType(JSON_CONTENT_TYPE);
      out.write(GSON.toJson(rootJson));
    } catch (IOException e) {
      ViewUtils.rethrow(e, page, view, this);
    }
  }

  /**
   * Renders the views configured by the {@link PrefetchFragmentsView#configReader} and adds it to the given json array
   *
   * @param fragmentsJson the given json array
   */
  private void renderPredefinedViews(@NonNull Object bean, @NonNull Page page, @NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response, @NonNull JsonArray fragmentsJson) {
    //for content pages use the bean otherwise page
    Object myBean = bean instanceof CMLinkable ? bean : page;
    getPredefinedViews(bean, page).stream()
            .map(view -> DEFAULT_VIEW_IDENTIFIER.equals(view) ? "" : view)
            .map(view -> renderView(myBean, nullToEmpty(view), request, response))
            .forEach(viewJson -> viewJson.ifPresent(fragmentsJson::add));
  }

  /**
   * Reads the predefined views out of the livecontext settings document
   *
   * @param bean the current self
   * @param page the current page
   * @return a list of views that should be included
   */
  @NonNull
  private Collection<String> getPredefinedViews(@NonNull Object bean, @NonNull Page page) {
    return configReader.getPredefinedViews(bean, page);
  }

  /**
   * Renders the given view of the given bean as the payload property of the returned json object.
   *
   * @param bean the given bean
   * @param view the given view
   * @return the json object with the rendered view as the payload property.
   */
  @NonNull
  private static Optional<JsonObject> renderView(@NonNull Object bean, @NonNull String view,
                                                 @NonNull HttpServletRequest request,
                                                 @NonNull HttpServletResponse response) {
    String payload;
    try {
      payload = getPayload(bean, view, request, response);
    } catch (ViewException e) {
      LOG.warn("Exception when rendering the view {} of the bean {} ({})", view, bean, e.getMessage());
      return empty();
    }
    JsonObject jsonObject = new JsonObject();
    String fragmentKey = createFragmentKey(bean, view, request);
    jsonObject.addProperty(FRAGMENT_KEY_PROPERTY, createFragmentKey(bean, view, request));
    jsonObject.addProperty(PAYLOAD_PROPERTY, payload);
    LOG.trace("Render predefined view as prefetched json: {}", fragmentKey);
    return Optional.of(jsonObject);
  }

  /**
   * Renders the given placement as the payload property of the returned json object.
   *
   * @param placement the given placement
   * @return the json object with the rendered view as the payload property.
   */
  @NonNull
  private Optional<JsonObject> renderPlacement(@NonNull PageGridPlacement placement,
                                               @NonNull HttpServletRequest request,
                                               @NonNull HttpServletResponse response) {
    String placementView = getPlacementView((Page) request.getAttribute(ContextHelper.ATTR_NAME_PAGE), placement).orElse("");
    String payload;
    try {
      payload = getPayload(placement, placementView, request, response);
    } catch (ViewException e) {
      LOG.warn("Exception when rendering the view {} of the bean {} ({})", placementView, placement, e.getMessage());
      return empty();
    }

    JsonObject jsonObject = new JsonObject();
    String fragmentKey = createFragmentKey(placement, placementView, request);
    jsonObject.addProperty(FRAGMENT_KEY_PROPERTY, fragmentKey);
    jsonObject.addProperty(PAYLOAD_PROPERTY, payload);
    LOG.trace("Render placement as prefetched json: {}", fragmentKey);
    return Optional.of(jsonObject);
  }

  /**
   * Reads the predefined view out of the livecontext settings document for the given page and placement
   *
   * @param page      the given page
   * @param placement the given placement
   * @return the predefined view for the placement
   */
  @NonNull
  private Optional<String> getPlacementView(@NonNull Page page, @NonNull PageGridPlacement placement) {
    String placementName = placement.getName();
    return configReader.getPlacementView(page, placementName);
  }

  /**
   * Renders the placements of the given page
   *
   * @param page          the given page
   * @param fragmentsJson the given json array
   */
  private void renderPlacements(@NonNull AbstractPage page, @NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response, @NonNull JsonArray fragmentsJson) {
    PageGrid pageGrid = page.getPageGrid();
    if (pageGrid == null) {
      return;
    }

    pageGrid.getRows().stream()
            .map(PageGridRow::getEditablePlacements)
            .flatMap(List::stream)
            .map(placement -> renderPlacement(placement, request, response)) // We don't know the view of the placement.
            .forEach(placementJson -> placementJson.ifPresent(fragmentsJson::add));
  }

  /**
   * renders the view of the given bean and returns the payload.
   *
   * @param bean the given bean
   * @param view the given view
   */
  @NonNull
  private static String getPayload(@NonNull Object bean, @NonNull String view, @NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response) {

    String effectiveView = view.isBlank() ? determineView(bean) : view;

    Writer out = new StringWriter();
    ViewUtils.render(bean, effectiveView, out, request, response);

    return out.toString().trim();
  }

  @NonNull
  private static String createFragmentKey(@NonNull Object bean, @NonNull String view, @NonNull ServletRequest request) {
    String placement = bean instanceof PageGridPlacement ? ((PageGridPlacement) bean).getName() : "";
    return createPageKey(request) + MATRIX_SEPERATOR +
            "view=" + view + MATRIX_SEPERATOR +
            "placement=" + nullToEmpty(placement).trim();
  }

  @NonNull
  private static String createPageKey(@NonNull ServletRequest request) {
    FragmentParameters parameters = FragmentContextProvider.getFragmentContext(request).getParameters();
    return createPageKeyFromParameters(parameters);
  }

  @NonNull
  @VisibleForTesting
  static String createPageKeyFromParameters(@NonNull FragmentParameters parameters) {
    // The ImmutableMap guarantees a deterministic iteration order and Null-hostility.
    // For further information on guarantees @see com.google.common.collect.ImmutableCollection
    Map<String, String> parameterMap = ImmutableMap.of(
            "externalRef", nullToEmpty(parameters.getExternalRef()),
            "categoryId", nullToEmpty(parameters.getCategoryId()),
            "productId", nullToEmpty(parameters.getProductId()),
            "pageId", nullToEmpty(parameters.getPageId())
    );

    return Joiner.on(MATRIX_SEPERATOR)
            .withKeyValueSeparator("=")
            .join(parameterMap);
  }

  /**
   * A channel in combination with the default view should always be rendered as full pagegrid (without header and footer).
   * This can be done by using the view "asFragment".
   * see also FragmentHandler#normalizedPageFragmentView()
   * @param bean the render model
   * @return a magic "normalized" default view "asFragment" or empty string;
   */
  private static String determineView(Object bean) {
    if (bean instanceof CMChannel) {
      return DEFAULT_CHANNEL_VIEW;
    }
    if (bean instanceof Page) {
      Page page = (Page) bean;
      if (page.getContent() instanceof CMChannel) {
        return DEFAULT_CHANNEL_VIEW;
      }
    }
    return "";
  }

}

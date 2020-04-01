package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.cae.action.webflow.WebflowActionState;
import com.coremedia.blueprint.cae.web.i18n.ResourceBundleInterceptor;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cae.webflow.FlowRunner;
import com.coremedia.cae.webflow.ModelHelper;
import com.coremedia.cap.user.User;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.view.substitution.SubstitutionRegistry;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Basic implementation of a WebflowHandler
 */
public abstract class WebflowHandlerBase extends PageHandlerBase {

  private FlowRunner flowRunner;
  private ResourceBundleInterceptor resourceBundleInterceptor;

  // --- spring config -------------------------------------------------------------------------------------------------

  @Required
  public void setFlowRunner(FlowRunner flowRunner) {
    this.flowRunner = flowRunner;
  }

  public FlowRunner getFlowRunner() {
    return flowRunner;
  }

  @Required
  public void setResourceBundleInterceptor(ResourceBundleInterceptor resourceBundleInterceptor) {
    this.resourceBundleInterceptor = resourceBundleInterceptor;
  }

  /**
   * This method must be implemented by extending classes and return an instance of {@link WebflowActionState}
   * (or an extending class)
   *
   * @param action         The action that represents the webflow
   * @param webFlowOutcome the outcome of the webflow
   * @param flowId         the flow id
   * @param flowViewId     the flow view id
   * @return the {@link WebflowActionState} instance
   */
  protected abstract WebflowActionState getWebflowActionState(CMAction action, ModelAndView webFlowOutcome, String flowId, String flowViewId);

  // --- Handlers ------------------------------------------------------------------------------------------------------

  /**
   * Handle the request.
   * May be called by different handler methods with different URI patterns.
   * <p>
   * Delegates to Webflow handling if necessary.
   *
   * @param contentBean the action document to handle the request for
   * @param navigation  the context of the contentbean
   * @param action      the action name
   * @param request     the servletRequest
   * @param response    the servletResponse
   * @return a valid {@link ModelAndView}
   */
  protected ModelAndView handleRequestInternal(ContentBean contentBean,
                                               Navigation navigation,
                                               String action,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
    if (contentBean instanceof CMAction) {
      CMAction actionBean = (CMAction) contentBean;
      // if name doesn't match the action or if no context available: return "not found"
      if (action.equals(getVanityName(actionBean)) && navigation != null) {
        // create the page that holds the action
        Page page = asPage(navigation, actionBean, UserVariantHelper.getUser(request));
        // try to handle the action
        if (actionBean.isWebFlow()) {
          // it's a webflow action
          return handleAsWebFlow(actionBean, page, request, response);
        } else {
          // unknown action type. return the surrounding page only.
          return createModel(page);
        }
      }
    }

    return HandlerHelper.notFound();
  }

  /**
   * Handles a {@link com.coremedia.blueprint.common.contentbeans.CMAction} as a webflow
   *
   * @param action The action that represents the webflow
   * @param page   the current page
   */
  private ModelAndView handleAsWebFlow(CMAction action, Page page, HttpServletRequest request, HttpServletResponse response) {
    //the interceptor runs too late for the Webflow backing bean validators, causing errors (HTTP 500) down the road.
    //register localization context manually here so that translated strings work from within the webflow.
    User developer = UserVariantHelper.getUser(request);
    resourceBundleInterceptor.registerResourceBundleForPage(page, developer, request, response);

    // make the page available to the webflow
    ModelAndView result = createModel(page);

    String flowId = action.getId();
    ModelAndView webFlowOutcome = flowRunner.run(flowId, result, request, response);
    if (webFlowOutcome != null) {
      String flowViewId = (String) webFlowOutcome.getModel().get(ModelHelper.FLOWVIEWID_NAME);

      // register the webflow's outcome as an action substitution
      WebflowActionState webflowActionState = getWebflowActionState(action, webFlowOutcome, flowId, flowViewId);
      SubstitutionRegistry.register(action.getId(), webflowActionState, webFlowOutcome);

      // merge both ModelAndViews. Merging the model objects wouldn't be absolutely necessary
      // because these are also contained in webflowActionState. But this is done for backward compatibility
      result.getModelMap().mergeAttributes(webFlowOutcome.getModel());
      result.setViewName(webFlowOutcome.getViewName());
      return webFlowOutcome;
    } else {
      // the response has been handled by webflow engine directly (e.g. by sending a redirect)
      return null;
    }
  }
}

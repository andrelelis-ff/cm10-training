package com.coremedia.blueprint.cae.action.subscription;

import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.objectserver.view.substitution.Substitution;
import com.coremedia.objectserver.web.links.Link;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

/**
 * Handler that handles a newsletter subscription action.
 * Note: This isn't fully implemented here but should serve as an action example only.
 */
@Link
@RequestMapping
public class SubscriptionHandler {

  private static final String SUBSCRIBE_URI = "/subscribe";
  private static final String UNSUBSCRIBE_URI = "/unsubscribe";
  private static final String RETURN_PARAMETER = "return";

  private static final String ACTION_ID = "com.coremedia.subscription";
  private static final String FORM_MODELATTRIBUTE = "subscriptionForm";

  // -------------- form ----------------

  /**
   * Builds the initial subscription form for a {@link CMAction} with id {@link #ACTION_ID}
   */
  @Substitution(value = ACTION_ID, modelAttribute = FORM_MODELATTRIBUTE)
  public SubscriptionForm createSubscriptionBean(CMAction original, HttpServletRequest request) {
    SubscriptionForm result = new SubscriptionForm();
    String email = getSubscribedAddress(request.getSession(false));
    result.setEmail(email);
    result.setSubscribed(email != null);
    return result;
  }


  /**
   * Registers the validator for subscription form
   */
  @InitBinder(FORM_MODELATTRIBUTE)
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(new SubscriptionFormValidator());
  }

  // -------------- handler ----------------

  /**
   * Handles subscription request
   */
  @PostMapping(value=SUBSCRIBE_URI)
  public RedirectView handleSubscription(@RequestParam(value=RETURN_PARAMETER, required=true) String redirectUri,
                                         @ModelAttribute(FORM_MODELATTRIBUTE) @Valid SubscriptionForm form, BindingResult binding,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) throws IOException  {

    if( !binding.hasErrors() ) {
      // perform (fake) subscription
      doSubscribe(session, form.getEmail());
      // add a success message to be displayed
      redirectAttributes.addFlashAttribute("subscriptionMessage", new DefaultMessageSourceResolvable(new String[] {"subscription.subscribed"}, "You are subscribed now"));
    }
    else {

      // there are validation errors. Don't subscribe but render the errors.
      // Hack: By adding a well known attribute to the flash attributes list, the binding errors will survive
      // the upcoming redirect
      redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX+FORM_MODELATTRIBUTE, binding);
    }

    // redirect to origin page
    return new RedirectView(redirectUri);
  }



  /**
   * Handles unsubscription request
   */
  @PostMapping(value=UNSUBSCRIBE_URI)
  public RedirectView handleUnsubscription(@RequestParam(value=RETURN_PARAMETER, required=true) String redirectUri,
                                         HttpSession session, RedirectAttributes redirectAttributes) throws IOException  {
    doUnsubscribe(session);
    redirectAttributes.addFlashAttribute("subscriptionMessage", new DefaultMessageSourceResolvable(new String[] {"subscription.subscribed"}, "You are no longer subscribed"));
    // redirect to origin page
    return new RedirectView(redirectUri);
  }


  // -------------- link ----------------

  /**
   * Builds a link pointing to the subscription handler. The necessary "return" link parameter needs to be an
   * uri to be redirected to after subscription
   */
  @Link(type=SubscriptionForm.class, parameter=RETURN_PARAMETER, uri= SUBSCRIBE_URI)
  public UriComponents createSubscriptionLink(SubscriptionForm bean, Map<String,Object> parameters) {
    // create a "subscribe" or an "unsubscribe" link depending on the current state
    UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(bean.isSubscribed() ? UNSUBSCRIBE_URI : SUBSCRIBE_URI);
    return uri.queryParam(RETURN_PARAMETER, (String) parameters.get(RETURN_PARAMETER)).build();
  }

  // ===================================

  /**
   * Performs subscription internally:
   * FAKE implementation: Just store the email in the current user's session
   */
  private void doSubscribe(HttpSession session, String email) {
    session.setAttribute(getClass().getName()+".email", email);
  }

  /**
   * Performs subscription internally:
   */
  private void doUnsubscribe(HttpSession session) {
    session.removeAttribute(getClass().getName()+".email");
  }

  /**
   * Provides the email address of the current user in case that she/he is already subscribed. null, otherwise.
   */
  private String getSubscribedAddress(HttpSession session) {
    return session == null ? null : (String) session.getAttribute(getClass().getName()+".email");
  }
}


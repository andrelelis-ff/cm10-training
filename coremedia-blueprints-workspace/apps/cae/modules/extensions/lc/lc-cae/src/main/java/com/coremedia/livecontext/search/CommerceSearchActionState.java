package com.coremedia.livecontext.search;

import com.coremedia.blueprint.cae.action.CMActionState;
import com.coremedia.blueprint.common.contentbeans.CMAction;


/**
 * The current state of the search action. Holds the action form as well as the action result
 *
 * @cm.template.api
 */
public class CommerceSearchActionState implements CMActionState {

  private final CMAction action;

  public CommerceSearchActionState(CMAction action) {
    this.action = action;
  }

  @Override
  public CMAction getAction() {
    return action;
  }

  public String toString() {
    return getClass().getName();
  }
}

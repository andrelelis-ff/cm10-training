package com.coremedia.blueprint.cae.action;

import com.coremedia.blueprint.common.contentbeans.CMAction;

/**
 * Interface to be implemented by all beans that represent an action's current state (e.g. an action form or action result)
 * and that are represented in the content by a {@link CMAction}.
 *
 * @cm.template.api
 */
public interface CMActionState {

  /**
   * @return Provides the content (bean) that represents the action
   * @cm.template.api
   */
  CMAction getAction();
}

package com.coremedia.blueprint.cae.action.webflow;

import com.coremedia.blueprint.cae.action.CMActionState;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.dispatch.HasCustomType;
import com.coremedia.dispatch.Type;
import com.coremedia.dispatch.TypeImpl;
import com.coremedia.dispatch.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an {@link com.coremedia.blueprint.common.contentbeans.CMAction webflow action}'s execution result / action state.
 * It holds the action's state as {@link #getModel named beans} as well as the {@link #getFlowViewId() flow view id}.
 * In addition it gives the rendering engine a special hint where to look for a template for a specific state.
 * The template location is computed from the flow id and the flow view id. For instance,
 * a flow id "com.mycompany.MyFlow" with a flow view name "myView" is translated (depending
 * on the concrete view engine) into a template path "[...]/templates/com.mycompany/MyFlow.myView.jsp"
 *
 * @cm.template.api
 */
public class WebflowActionState implements HasCustomType, CMActionState {

  private final CMAction action;

  private final String flowViewId;
  private final String flowId;
  private final Map<String, Object> model;
  private final Type type;

  /**
   * @param action   The original action
   * @param model    The action's result as named beans
   * @param flowId   The flow id
   * @param flowView The flow view name, e.g. "success"
   */
  public WebflowActionState(CMAction action, Map<String, Object> model, String flowId, String flowView) {
    this.flowId = flowId; // assuming that flowId consists of alphanumeric characters an "." only
    this.flowViewId = flowView;
    this.action = action;
    this.model = model == null ? new HashMap<>() : new HashMap<>(model);

    // build the type hierarchy that shall be used by the view engine for the template lookup
    List<String> typeHierarchy = new ArrayList<>();
    typeHierarchy.add(flowId); // the pseudo type of this flow id
    typeHierarchy.addAll(Types.fromClass(getClass()).getHierarchy()); // the types of this class
    this.type = new TypeImpl(typeHierarchy);
  }

  /**
   * @return The view name to be used for rendering the action result
   * @cm.template.api
   */
  public String getFlowViewId() {
    return flowViewId;
  }

  @Override
  public CMAction getAction() {
    return action;
  }

  /**
   * @return The action's outcome as named beans
   */
  public Map<String, Object> getModel() {
    return model;
  }

  @Override
  public Type getCustomType() {
    return type;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[flowId=" + flowId + ", flowViewId=" + flowViewId + ", type=" + type + "]";
  }
}

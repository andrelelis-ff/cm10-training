package com.coremedia.blueprint.translate.workflow;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentObject;
import com.coremedia.cap.workflow.Process;
import com.coremedia.cap.workflow.Task;
import com.coremedia.cap.workflow.plugin.LongActionBase;

import java.util.Collection;
import java.util.List;

/**
 * A template action used in the example translation workflow.
 * By default, this action does nothing.
 */
@SuppressWarnings("unused")
public class SendToTranslationServiceAction extends LongActionBase {
  private static final long serialVersionUID = -8884595235161300715L;

  private String derivedContentsVariable;
  private String masterContentObjectsVariable;

  /**
   * Return the name of the process variable that stores the list of contents
   * for which a translation should be generated.
   *
   * @return the name of the process variable
   */
  public String getDerivedContentsVariable() {
    return derivedContentsVariable;
  }

  /**
   * Return the name of the process variable that stores the list of contents
   * for which a translation should be generated.
   *
   * @param derivedContentsVariable the name of the process variable
   */
  public void setDerivedContentsVariable(String derivedContentsVariable) {
    this.derivedContentsVariable = derivedContentsVariable;
  }

  /**
   * Return the name of the process variable containing the source contents objects.
   *
   * @return the name of the process variable
   */
  public String getMasterContentObjectsVariable() {
    return masterContentObjectsVariable;
  }

  /**
   * Set the name of the process variable containing the source contents objects.
   *
   * @param masterContentObjectsVariable the name of the process variable
   */
  public void setMasterContentObjectsVariable(String masterContentObjectsVariable) {
    this.masterContentObjectsVariable = masterContentObjectsVariable;
  }

  @Override
  public Object extractParameters(Task task) {
    Process process = task.getContainingProcess();

    List<Content> derivedContents = process.getLinks(derivedContentsVariable);
    List<ContentObject> masterContentObjects = process.getLinksAndVersions(masterContentObjectsVariable);

    return new Parameters(derivedContents, masterContentObjects);
  }

  @Override
  protected Object doExecute(Object params) {

    Parameters parameters = (Parameters) params;

    if (parameters.derivedContents.isEmpty()) {
      return null;
    }

    //  implement the sending / receiving of contents to / from a translation agency

    return parameters.derivedContents;
  }

  private static final class Parameters {
    final Collection<Content> derivedContents;
    final Collection<ContentObject> masterContentObjects;

    public Parameters(final Collection<Content> derivedContents,
                      final Collection<ContentObject> masterContentObjects) {
      this.derivedContents = derivedContents;
      this.masterContentObjects = masterContentObjects;
    }
  }

}

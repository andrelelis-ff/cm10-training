package com.coremedia.blueprint.personalization.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.cap.content.Content;
import com.coremedia.id.IdScheme;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.rulelang.InvalidSyntaxException;
import com.coremedia.personalization.rulelang.SelectionRulesProcessor;
import com.coremedia.personalization.rulelang.XMLCoDec;
import com.coremedia.xml.Markup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * The bean corresponding to the <code>CMSelectionRules</code> document type. It selects its
 * content by applying the selection rules stored in the associated document to the
 * active user's profile.
 */
public class CMSelectionRulesImpl extends CMSelectionRulesBase {

  /*
   * DEVELOPER NOTE
   * You are invited to change this class by adding additional methods here.
   * Add them to the interface {@link com.coremedia.blueprint.personalization.contentbeans.CMSelectionRules} to make them public.
   */

  private static final Logger LOG = LoggerFactory.getLogger(CMSelectionRulesImpl.class);

  private ContextCollection contextCollection;
  private IdScheme contentIdScheme;

  /**
   * Sets the context collection instances against which the selection rules are to be executed.
   *
   * @param contextCollection the context-collection bean to be used
   */
  @Required
  public void setContextCollection(final ContextCollection contextCollection) {
    this.contextCollection = contextCollection;
  }

  /**
   * Sets the id scheme to be used to resolve the ids of selected content against.
   *
   * @param contentIdScheme the content-id scheme to be used
   */
  @Required
  public void setContentIdScheme(IdScheme contentIdScheme) {
    if (contentIdScheme == null) {
      throw new IllegalArgumentException("property contentIdScheme must not be null");
    }
    this.contentIdScheme = contentIdScheme;
  }

  /**
   * Returns the list of teasers selected by the rules defined in the
   * 'rules' document property when applied to the profile of the current
   * user.
   *
   * @return list of teasers
   */
  @Override
  public List<CMTeasable> getItemsUnfiltered() {
    try {
      final SelectionRulesProcessor selectionRulesProcessor = getSelectionRuleProcessor();
      if (selectionRulesProcessor != null) {
        final List<Content> selectedContent = selectionRulesProcessor.processAndFetchContent(contextCollection, contentIdScheme);
        LOG.debug("rules \"{}\" evaluated to {}", getRules(), selectedContent);
        if (!selectedContent.isEmpty()) {
          return createBeansFor(selectedContent, CMTeasable.class);
        }
      }
    } catch (final Exception ex) {
      LOG.error("An error occurred while processing '" + getRules() + "'.", ex);
    }

    // if we reach this line, the rules didn't select any content
    return getDefaultContent();
  }

  /**
   * Returns a SelectionRulesProcessor initialized with the rules defined in the 'rules' property of
   * the document corresponding to this bean. The SelectionRulesProcessor can be cached via dataviews to
   * prevent recompiling of the rules string.
   * <p/>
   * {@link #getItems()} uses the returned processor to determine which content items to return.
   * <p/>
   * The method will return null in case the rules property is empty or cannot be evaluated.
   *
   * @return a SelectionRuleProcessor instance
   */
  public SelectionRulesProcessor getSelectionRuleProcessor() {
    LOG.debug("Returning a SelectionRuleProcessor."); // cheap way to debug the dataview
    try {
      final Markup rulesAsMarkup = getRules();
      if (rulesAsMarkup != null) {
        final String rules = XMLCoDec.getXMLRulesAsString(rulesAsMarkup.asXml());
        if (rules != null && rules.length() > 0) {
          try {
            return new SelectionRulesProcessor(rules);
          } catch (InvalidSyntaxException e) {
            if(LOG.isInfoEnabled()) {
              LOG.info("Selection rules of content " + getContentId() + " are invalid  (" + rules + "), cause: " + e.getMessage());
            }
          }
        }
      }
      // we reach this line only if there aren't any rules
      LOG.debug("no selection rules provided");
    } catch (final Exception e) {
      LOG.error("An error occurred while processing '" + getRules() + "'.", e);
    }
    return null;
  }

}

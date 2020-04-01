package com.coremedia.blueprint.elastic.social.cae;


import com.coremedia.blueprint.common.contentbeans.CMAction;

/**
 * Serves the Action documents for the ES webflow.
 */
public interface ElasticSocialActions {
  CMAction getFlowLogin();
  CMAction getFlowLogout();
  CMAction getFlowUserDetails();
}

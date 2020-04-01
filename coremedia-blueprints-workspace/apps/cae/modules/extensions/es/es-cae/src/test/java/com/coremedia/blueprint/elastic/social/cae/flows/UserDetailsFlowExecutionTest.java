package com.coremedia.blueprint.elastic.social.cae.flows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.webflow.config.FlowDefinitionResource;
import org.springframework.webflow.config.FlowDefinitionResourceFactory;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowBuilderContext;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;

import javax.security.auth.login.LoginException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsFlowExecutionTest extends AbstractXmlFlowExecutionTests {

  @Mock
  private UserDetailsHelper userDetailsHelper;

  @Mock
  private PasswordPolicy passwordPolicy;

  @Mock
  private FlowUrlHelper flowUrlHelper;

  protected FlowDefinitionResource getResource(FlowDefinitionResourceFactory resourceFactory) {
    return resourceFactory.createClassPathResource("/com/coremedia/blueprint/es/webflow/com.coremedia.blueprint.es.webflow.UserDetails.xml", UserDetailsFlowExecutionTest.class);
  }

  protected void configureFlowBuilderContext(MockFlowBuilderContext builderContext) {
    builderContext.registerBean("userDetailsHelper", userDetailsHelper);
    builderContext.registerBean("passwordPolicy", passwordPolicy);
    builderContext.registerBean("webflowUrlHelper", flowUrlHelper);
  }

  @Test
  public void testStartUserDetailsFlow() {
    MutableAttributeMap input = new LocalAttributeMap();
    MockExternalContext context = new MockExternalContext();

    startFlow(input, context);

    assertCurrentStateEquals("bpUserDetails");
    assertTrue(getRequiredFlowAttribute("bpUserDetails") instanceof UserDetails);
  }

  @Test
  public void testSaveUserSuccess() throws LoginException {
    setCurrentState("userDetailsForm");

    final UserDetails userDetails = new UserDetails();
    userDetails.setUsername("hoschi");
    userDetails.setGivenname("horst");
    userDetails.setSurname("schmidt");
    userDetails.setEmailAddress("abcd@defg.hi");
    getFlowScope().put("bpUserDetails", userDetails);
    getFlowScope().put("authorName", null);
    when(userDetailsHelper.save(eq(userDetails), any(RequestContext.class), nullable(CommonsMultipartFile.class))).thenReturn(true);

    MockExternalContext context = new MockExternalContext();
    context.setEventId("saveUser");
    resumeFlow(context);
    assertCurrentStateEquals("bpUserDetails");
  }
}

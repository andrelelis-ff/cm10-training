package com.coremedia.blueprint.elastic.social.cae.flows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.webflow.config.FlowDefinitionResource;
import org.springframework.webflow.config.FlowDefinitionResourceFactory;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockFlowBuilderContext;
import org.springframework.webflow.test.execution.AbstractXmlFlowExecutionTests;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetFlowExecutionTest extends AbstractXmlFlowExecutionTests {

  @Mock
  private PasswordResetHelper passwordResetHelper;


  @Before
  public void init() {
  }

  protected FlowDefinitionResource getResource(FlowDefinitionResourceFactory resourceFactory) {
    return resourceFactory.createClassPathResource("/com/coremedia/blueprint/es/webflow/com.coremedia.blueprint.es.webflow.PasswordReset.xml", PasswordResetFlowExecutionTest.class);
  }

  protected void configureFlowBuilderContext(MockFlowBuilderContext builderContext) {
    builderContext.registerBean("passwordResetHelper", passwordResetHelper);
  }

  @Test
  public void startPasswordResetFlow() {
    MutableAttributeMap input = new LocalAttributeMap();
    MockExternalContext context = new MockExternalContext();
    startFlow(input, context);

    assertCurrentStateEquals("bpPasswordReset");
    assertTrue(getRequiredFlowAttribute("bpPasswordReset") instanceof PasswordReset);
  }
}
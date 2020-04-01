package com.coremedia.blueprint.nuggad.sources;

import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.ContextCollectionImpl;
import com.coremedia.personalization.context.PropertyProfile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;

public class NuggAdSourceTest {
  private NuggAdSource source;
  private List<String> parameters;

  @Before
  public void setUp() throws Exception {
    source = new NuggAdSource();
    source.setContextName("nuggad");
    parameters = Arrays.asList("age", "gender", "employmentStatus", "personalIncome", "householdIncome", "householdSize", "education", "householdResponsability", "mainIncomeEarner");
    source.setParameterNames(parameters);
  }

  @Test
  public void testPreHandle() throws Exception {
    Cookie nuggAIArray = new Cookie("nuggAIArray", "4,2,2,4,4,2,9,1,1");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(nuggAIArray);
    MockHttpServletResponse response = new MockHttpServletResponse();
    ContextCollection contextCollection = new ContextCollectionImpl();
    source.preHandle(request, response, contextCollection);
    PropertyProfile profile = (PropertyProfile) contextCollection.getContext("nuggad");
    Assert.assertNotNull(profile);
    for (String parameter : parameters) {
      Assert.assertTrue(profile.getPropertyNames().contains(parameter));
    }
  }

  @Test
  public void testToString() throws Exception {
    Assert.assertEquals("[com.coremedia.blueprint.nuggad.sources.NuggAdSource, contextName=nuggad, cookieName=nuggAIArray]", source.toString());
  }
}

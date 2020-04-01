package com.coremedia.blueprint.cae.action.search;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link SearchFormBean}
 */
@RunWith(Parameterized.class)
public class SearchFormBeanTest {

  private String sourceString, expectedEscapedString;

  /**
   * Creates a collection of Object arrays containing {source string, expected escaped string}.
   *
   * @return A collection of Object arrays containing {source string, expected escaped string}.
   */
  @Parameterized.Parameters
  public static Collection data() {
    return Arrays.asList(new Object[][]{
            {null, null},
            {"alert()", "alert\\(\\)"},
            {"London alert()", "London alert\\(\\)"},
            {"Blueprint {23}", "Blueprint \\{23\\}"},
            {"!Blueprint - 4", "\\!Blueprint \\- 4"},
            {"^", "\\^"},
            {"London && Blueprint", "London \\&& Blueprint"},
            {"Blueprint && (London || Hamburg)", "Blueprint \\&& \\(London \\|| Hamburg\\)"}
    });
  }

  /**
   * Constructor for this parametrized test.
   *
   * @param sourceString          The source query string to escape.
   * @param expectedEscapedString The expected escaped query string.
   */
  public SearchFormBeanTest(String sourceString, String expectedEscapedString) {
    this.sourceString = sourceString;
    this.expectedEscapedString = expectedEscapedString;
  }

  @Test
  public void testGetQueryEscaped() throws Exception {
    SearchFormBean searchFormBean = new SearchFormBean();
    searchFormBean.setQuery(sourceString);
    assertEquals(expectedEscapedString, searchFormBean.getQueryEscaped());
  }
}

package com.coremedia.blueprint.cae.search.solr;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Collections;

import static com.coremedia.blueprint.cae.search.solr.SolrSearchFormatHelper.formatLocalParameters;
import static org.junit.Assert.*;

public class SolrSearchFormatHelperTest {

  @Test
  public void testFormatLocalParameters() {
    assertEquals("", formatLocalParameters(Collections.emptyMap()));
    assertEquals("{! foo=bar}", formatLocalParameters(Collections.singletonMap("foo", "bar")));
    assertEquals("{! one=1 two=2}", formatLocalParameters(ImmutableMap.of("one", "1", "two", "2")));
    assertEquals("{! foo='}\\'_\\'{'}", formatLocalParameters(ImmutableMap.of("foo", "}'_'{")));
  }
}

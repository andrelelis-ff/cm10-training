package com.coremedia.blueprint.analytics.elastic.cae;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:/com/coremedia/blueprint/analytics/elastic/cae/EsAlxCaeApplicationContextTest.xml",
        "classpath:/spring/test/dummy-views.xml",
        "classpath:/com/coremedia/cap/common/xml/uapi-xml-services.xml",
})
@TestPropertySource(properties = "elastic.core.persistence=memory")
public class EsAlxCaeApplicationContextTest {

  @Test
  public void canLoadApplicationContext() {
    // if control flow ends up here, we're done
  }

}

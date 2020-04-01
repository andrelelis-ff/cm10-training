package com.coremedia.blueprint.coderesources;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CodeResourcesCacheKeyTest {
  @Mock
  private Content c0, c1, c2, c3, c4, js, theme;

  @Mock
  private ContentType cmnavigation;

  @Mock
  private TreeRelation<Content> treeRelation;

  @Before
  public void setup() {
    when(cmnavigation.isSubtypeOf("CMNavigation")).thenReturn(true);
    wire();
    when(c1.getLinks("javaScript")).thenReturn(Collections.singletonList(js));
    when(c3.getLinks("javaScript")).thenReturn(Collections.singletonList(js));
    when(c1.getLink("theme")).thenReturn(theme);
    when(c2.getLink("theme")).thenReturn(theme);
  }

  /**
   * Contract: theme is inherited from parent channels, code applies only to
   * a channel itself.
   */
  @Test
  public void testInitCodeCarriers() {
    CodeCarriers cc = new CodeCarriers();
    CodeResourcesCacheKey.initCodeCarriers(cc, c0, "javaScript", treeRelation);
    assertNull(cc.getThemeCarrier());
    assertNull(cc.getCodeCarrier());

    cc = new CodeCarriers();
    CodeResourcesCacheKey.initCodeCarriers(cc, c1, "javaScript", treeRelation);
    assertEquals(c1, cc.getThemeCarrier());
    assertEquals(c1, cc.getCodeCarrier());

    cc = new CodeCarriers();
    CodeResourcesCacheKey.initCodeCarriers(cc, c2, "javaScript", treeRelation);
    assertEquals(c2, cc.getThemeCarrier());
    assertNull(cc.getCodeCarrier());

    cc = new CodeCarriers();
    CodeResourcesCacheKey.initCodeCarriers(cc, c3, "javaScript", treeRelation);
    assertEquals(c2, cc.getThemeCarrier());
    assertEquals(c3, cc.getCodeCarrier());

    cc = new CodeCarriers();
    CodeResourcesCacheKey.initCodeCarriers(cc, c4, "javaScript", treeRelation);
    assertEquals(c2, cc.getThemeCarrier());
    assertNull(cc.getCodeCarrier());
  }


  // --- internal ---------------------------------------------------

  private void wire() {
    Content[] cs = new Content[] {c0, c1, c2, c3, c4};
    for (int i=5; --i>=0; when(cs[i].getLinks("javaScript")).thenReturn(Collections.emptyList()));
    for (int i=5; --i>=0; when(cs[i].getType()).thenReturn(cmnavigation));
    for (int i=5; --i>=0; when(treeRelation.pathToRoot(cs[i])).thenReturn(asList(copyOfRange(cs, 0, i+1))));
  }
}

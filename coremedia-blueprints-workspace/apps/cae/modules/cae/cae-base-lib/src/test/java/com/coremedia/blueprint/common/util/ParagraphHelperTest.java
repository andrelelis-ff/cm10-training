package com.coremedia.blueprint.common.util;

import com.coremedia.blueprint.common.util.pagination.PagingRuleType;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import com.coremedia.xml.MarkupUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ParagraphHelperTest {
  private Markup markup;

  @Before
  public void setUp() throws Exception {
    markup = MarkupFactory.fromString("<div xmlns:xlink='http://www.w3.org/1999/xlink' xmlns='http://www.coremedia.com/2003/richtext-1.0'><p>I am markup</p><p>Me too</p></div>");
  }


  @Test
  public void testCreateParagraphs1() throws Exception {
    List<Markup> markups = ParagraphHelper.createParagraphs(markup);
    Assert.assertEquals(2, markups.size());
    Assert.assertEquals("I am markup", MarkupUtil.asPlainText(markups.get(0)).trim());
    Assert.assertEquals("Me too", MarkupUtil.asPlainText(markups.get(1)).trim());
  }

  @Test
  public void testCreateParagraphs2() throws Exception {
    List<Markup> markups = ParagraphHelper.createParagraphs(markup, 2);
    Assert.assertEquals(1, markups.size());
    Assert.assertEquals("I am markup\n\nMe too", MarkupUtil.asPlainText(markups.get(0)).trim());
  }

  @Test
  public void testCreateParagraphs3() throws Exception {
    List<Markup> markups = ParagraphHelper.createParagraphs(markup, 1, "CharactersCountAndNextParagraphRule");
    Assert.assertEquals(2, markups.size());
    Assert.assertEquals("I am markup", MarkupUtil.asPlainText(markups.get(0)).trim());
    Assert.assertEquals("Me too", MarkupUtil.asPlainText(markups.get(1)).trim());
  }

  @Test
  public void testCreateParagraphs4() throws Exception {
    List<Markup> markups = ParagraphHelper.createParagraphs(markup, 1, PagingRuleType.DelimitingBlockCountRule);
    Assert.assertEquals(2, markups.size());
    Assert.assertEquals("I am markup", MarkupUtil.asPlainText(markups.get(0)).trim());
    Assert.assertEquals("Me too", MarkupUtil.asPlainText(markups.get(1)).trim());

  }
}

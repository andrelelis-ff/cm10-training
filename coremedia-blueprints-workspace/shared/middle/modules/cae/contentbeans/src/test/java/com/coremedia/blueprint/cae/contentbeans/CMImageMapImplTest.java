package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.cae.web.taglib.ImageFunctions;
import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMImageMap;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CMImageMapImplTest extends ContentBeanTestBase {

  private CMImageMap imageMap;
  private CMPicture picture;
  private CMArticle article;

  @Before
  public void setUp() throws Exception {
    imageMap = getContentBean(96);
    picture = getContentBean(20);
    article = getContentBean(2);
  }

  @Test
  public void get() {
    assertNotNull(imageMap);

    assertEquals(picture, imageMap.getPicture());
    assertEquals(article, imageMap.getTarget());

    List<Map<String,Object>> imageMapAreas = imageMap.getImageMapAreas();
    assertEquals(1, imageMapAreas.size());
    Map<String, Object> imageMapArea = imageMapAreas.get(0);
    assertEquals("rect", imageMapArea.get(ImageFunctions.SHAPE));
    assertEquals("one", imageMapArea.get(ImageFunctions.ALT));
    assertEquals(article, imageMapArea.get(ImageFunctions.LINKED_CONTENT));
    assertEquals("100,100,200,200", imageMapArea.get(ImageFunctions.COORDS));
    assertEquals(ImmutableList.of(new Point2D.Double(100, 100), new Point2D.Double(200, 200)),
            imageMapArea.get(ImageFunctions.COORDS_AS_POINTS));

  }
}

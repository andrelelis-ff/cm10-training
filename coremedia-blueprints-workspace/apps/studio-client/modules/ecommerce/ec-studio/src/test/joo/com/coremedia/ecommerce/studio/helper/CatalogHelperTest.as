package com.coremedia.ecommerce.studio.helper {
import flexunit.framework.TestCase;

public class CatalogHelperTest extends TestCase{
  public function CatalogHelperTest() {
  }

  internal var catalogHelper:CatalogHelper = CatalogHelper.getInstance();

  [Test]
  public function testGetToken():void {
    assertStrictlyEquals(undefined, catalogHelper.getToken("asdf"));
    assertStrictlyEquals("/category/", catalogHelper.getToken("vendor:///catalog/category/TEST"));
    assertStrictlyEquals("/product/", catalogHelper.getToken("vendor:///catalog/product/techId:TEST"));
  }

  [Test]
  public function testGetAliasIdFromId():void {
    assertEquals("alias", catalogHelper.getCatalogAliasFromId("vendor:///catalog/category/catalog:alias;TEST"));
    assertEquals("catalog", catalogHelper.getCatalogAliasFromId("vendor:///catalog/category/catalog:;TEST"));
    assertEquals("catalog", catalogHelper.getCatalogAliasFromId("vendor:///catalog/category/TEST"))
  }

  [Test]
  public function testGetExternalIdFromId():void {
    assertEquals("TEST", catalogHelper.getExternalIdFromId("vendor:///catalog/category/catalog:alias;TEST"));
    assertEquals("catalog:;TEST", catalogHelper.getExternalIdFromId("vendor:///catalog/category/catalog:;TEST"));
    assertEquals("TEST", catalogHelper.getExternalIdFromId("vendor:///catalog/category/TEST"))
  }
}
}

package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.common.contentbeans.CMChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Calendar;

public class ValidFromPropertyConverterTest {
  private CMChannel content;

  @Before
  public void setUp() throws Exception {
    Calendar validFrom = Calendar.getInstance();
    validFrom.set(Calendar.YEAR, 2008);
    validFrom.set(Calendar.MONTH, Calendar.JANUARY);
    validFrom.set(Calendar.DAY_OF_MONTH, 28);

    content = Mockito.mock(CMChannel.class);
    Mockito.when(content.getValidFrom()).thenReturn(validFrom);
    Calendar validTo = (Calendar) validFrom.clone();
    validTo.set(Calendar.YEAR, 2009);
    Mockito.when(content.getValidTo()).thenReturn(validTo);
  }

  @Test
  public void testConvertValueForNull() throws Exception {
    ValidFromPropertyConverter con = new ValidFromPropertyConverter();
    Object o = con.convertValue(null);
    Assert.assertTrue(o instanceof Calendar);
    Calendar cal = (Calendar) o;
    Assert.assertEquals(1970, cal.get(Calendar.YEAR));
  }

  @Test
  public void testConvertValue() throws Exception {
    ValidFromPropertyConverter con = new ValidFromPropertyConverter();
    Object o = con.convertValue(content.getValidFrom());
    Assert.assertTrue(o instanceof Calendar);
    Calendar cal = (Calendar) o;
    Assert.assertEquals(2008, cal.get(Calendar.YEAR));
  }

  @Test
  public void testConvertValueWrongType() throws Exception {
    ValidFromPropertyConverter con = new ValidFromPropertyConverter();
    try {
      con.convertValue(content);
    } catch (IllegalArgumentException e) {

    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testConvertType() throws Exception {
    Assert.assertEquals(Calendar.class, new ValidFromPropertyConverter().convertType(this.getClass()));
  }
}

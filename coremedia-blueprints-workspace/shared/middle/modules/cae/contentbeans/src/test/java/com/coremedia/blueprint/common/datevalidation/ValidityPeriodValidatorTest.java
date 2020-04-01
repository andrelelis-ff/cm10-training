package com.coremedia.blueprint.common.datevalidation;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.testing.ContentBeanTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * There are five content items
 * 1. Valid from 1.1.2000 - 31.12.2005
 * 2. Valid from 1.1.2001 - 31.12.2006
 * 3. Valid from 1.1.2002 - 31.12.2007
 * 4. Valid from 1.1.2003 - 31.12.2008
 * 5. Valid from 1.1.2004 - 31.12.2009
 * <p/>
 * Test will run with these dates as now
 * 1. 15.3.1999
 * 2. 15.3.2001
 * 3. 15.3.2005
 * 4. 15.3.2007
 * 5. 15.3.2010
 */
class ValidityPeriodValidatorTest {

  private List<CMLinkable> itemsUnfiltered;

  private ValidityPeriodValidator validator;

  @BeforeEach
  void setUp() {
    itemsUnfiltered = newArrayList(
            mockLinkable(2000, 0, 1, 2005, 11, 31),
            mockLinkable(2001, 0, 1, 2006, 11, 31),
            mockLinkable(2002, 0, 1, 2007, 11, 31),
            mockLinkable(2003, 0, 1, 2008, 11, 31),
            mockLinkable(2004, 0, 1, 2009, 11, 31),
            mockLinkableFrom(2012, 3, 14, mock(CMLinkable.class))
    );
  }

  @SuppressWarnings("SameParameterValue")
  private CMLinkable mockLinkable(int fromYear, int fromMonth, int fromDay, int toYear, int toMonth, int toDay) {
    CMLinkable linkable = mock(CMLinkable.class);
    linkable = mockLinkableFrom(fromYear, fromMonth, fromDay, linkable);
    linkable = mockLinkableTo(toYear, toMonth, toDay, linkable);
    return linkable;
  }

  private CMLinkable mockLinkableTo(int toYear, int toMonth, int toDay, CMLinkable linkable) {
    Calendar validTo = Calendar.getInstance();
    validTo.set(Calendar.YEAR, toYear);
    validTo.set(Calendar.MONTH, toMonth);
    validTo.set(Calendar.DAY_OF_MONTH, toDay);
    when(linkable.getValidTo()).thenReturn(validTo);
    initTime(validTo);
    return linkable;
  }

  private CMLinkable mockLinkableFrom(int fromYear, int fromMonth, int fromDay, CMLinkable linkable) {
    Calendar validFrom = Calendar.getInstance();
    validFrom.set(Calendar.YEAR, fromYear);
    validFrom.set(Calendar.MONTH, fromMonth);
    validFrom.set(Calendar.DAY_OF_MONTH, fromDay);
    when(linkable.getValidFrom()).thenReturn(validFrom);
    initTime(validFrom);
    return linkable;
  }

  private ValidityPeriodValidator preparePreviewDate(int day, int month, int year) {
    ContentBeanTestBase.setUpPreviewDate(new MockHttpServletRequest(), REQUEST_ATTRIBUTE_PREVIEW_DATE, year, month, day);
    return new ValidityPeriodValidator();
  }

  private static void initTime(Calendar now) {
    now.set(Calendar.HOUR_OF_DAY, 0);
    now.set(Calendar.MINUTE, 0);
    now.set(Calendar.SECOND, 0);
    now.set(Calendar.MILLISECOND, 0);
  }

  /**
   * Method: filterList(List<Content> source, Calendar now)
   */
  @Test
  void testFilterListCase1() {
    validator = preparePreviewDate(15, Calendar.MARCH, 1999);

    List<CMLinkable> validItems = new ArrayList<>(validator.filterList(itemsUnfiltered));

    assertThat(validItems).isEmpty();
  }

  /**
   * Method: filterList(List<Content> source, Calendar now)
   */
  @Test
  void testFilterListCase2() {
    validator = preparePreviewDate(15, Calendar.MARCH, 2001);

    List<CMLinkable> validItems = new ArrayList<>(validator.filterList(itemsUnfiltered));

    assertThat(validItems).hasSize(2);
  }

  /**
   * Method: filterList(List<Content> source, Calendar now)
   */
  @Test
  void testFilterListCase3() {
    validator = preparePreviewDate(15, Calendar.MARCH, 2005);

    List<CMLinkable> validItems = newArrayList(validator.filterList(itemsUnfiltered));

    assertThat(validItems).hasSize(5);
  }

  /**
   * Method: filterList(List<Content> source, Calendar now)
   */
  @Test
  void testFilterListCase4() {
    validator = preparePreviewDate(15, Calendar.MARCH, 2007);

    List<CMLinkable> validItems = newArrayList(validator.filterList(itemsUnfiltered));

    assertThat(validItems).hasSize(3);
  }

  /**
   * Method: filterList(List<Content> source, Calendar now)
   */
  @Test
  void testFilterListCase5() {
    validator = preparePreviewDate(15, Calendar.MARCH, 2010);

    List<CMLinkable> validItems = new ArrayList<>(validator.filterList(itemsUnfiltered));

    assertThat(validItems).isEmpty();
  }

  @Test
  void testFilterListCase6() {
    List<CMLinkable> validItems;

    validator = preparePreviewDate(15, Calendar.APRIL, 2012);
    validItems = new ArrayList<>(validator.filterList(itemsUnfiltered));
    assertThat(validItems).hasSize(1);

    validator = preparePreviewDate(13, Calendar.APRIL, 2012);
    validItems = new ArrayList<>(validator.filterList(itemsUnfiltered));
    assertThat(validItems).isEmpty();
  }

  /**
   * Method: filterList(List<Content> source, Calendar now)
   */
  @Test
  void testValidate() {
    validator = preparePreviewDate(15, Calendar.MARCH, 2007);

    assertThat(validator.validate(itemsUnfiltered.get(0))).isFalse();
    assertThat(validator.validate(itemsUnfiltered.get(1))).isFalse();
    assertThat(validator.validate(itemsUnfiltered.get(2))).isTrue();
    assertThat(validator.validate(itemsUnfiltered.get(3))).isTrue();
    assertThat(validator.validate(itemsUnfiltered.get(4))).isTrue();
  }
}

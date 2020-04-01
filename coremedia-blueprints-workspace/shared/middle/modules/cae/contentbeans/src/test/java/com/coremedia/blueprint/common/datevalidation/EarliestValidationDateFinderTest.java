package com.coremedia.blueprint.common.datevalidation;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EarliestValidationDateFinderTest {

  private CMLinkable linkable;

  @BeforeEach
  void setUp() {
    Calendar validFrom = Calendar.getInstance();
    validFrom.set(Calendar.YEAR, 2010);
    validFrom.set(Calendar.MONTH, Calendar.JANUARY);
    validFrom.set(Calendar.DAY_OF_MONTH, 1);

    linkable = mock(CMLinkable.class);
    when(linkable.getValidFrom()).thenReturn(validFrom);
    Calendar validTo = (Calendar) validFrom.clone();
    validTo.set(Calendar.YEAR, 2011);
    when(linkable.getValidTo()).thenReturn(validTo);
  }

  /**
   * Method: apply(Content content)
   * now is in between both dates --> when validTo is reached we need to invalidate
   */
  @Test
  void testInBetween() {
    Calendar now = GregorianCalendar.getInstance();
    now.set(Calendar.YEAR, 2010);
    now.set(Calendar.MONTH, Calendar.MARCH);
    //validFrom: 01.01.2010
    //validTo: 01.01.2011

    Optional<Calendar> result = EarliestValidationDateFinder.findNextDate(now, linkable);

    assertThat(result).contains(linkable.getValidTo());
  }

  /**
   * Method: apply(Content content)
   * now is before both dates --> when validFrom is reached we need to invalidate
   */
  @Test
  void testBefore() {
    Calendar now = GregorianCalendar.getInstance();
    now.set(Calendar.YEAR, 2008);
    now.set(Calendar.MONTH, Calendar.MARCH);
    //validFrom: 01.01.2010
    //validTo: 01.01.2011

    Optional<Calendar> result = EarliestValidationDateFinder.findNextDate(now, linkable);

    assertThat(result).contains(linkable.getValidFrom());
  }

  /**
   * Method: apply(Content content)
   * now is after both dates --> null
   */
  @Test
  void testAfter() {
    Calendar now = GregorianCalendar.getInstance();
    now.set(Calendar.YEAR, 2014);
    now.set(Calendar.MONTH, Calendar.MARCH);
    //validFrom: 01.01.2010
    //validTo: 01.01.2011

    Optional<Calendar> result = EarliestValidationDateFinder.findNextDate(now, linkable);

    assertThat(result).isNotPresent();
  }
}

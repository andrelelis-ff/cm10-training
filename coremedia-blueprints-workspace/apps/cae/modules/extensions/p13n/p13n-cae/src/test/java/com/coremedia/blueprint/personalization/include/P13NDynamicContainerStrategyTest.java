package com.coremedia.blueprint.personalization.include;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.layout.Container;
import com.coremedia.blueprint.personalization.contentbeans.CMP13NSearch;
import com.coremedia.blueprint.personalization.contentbeans.CMSelectionRules;
import com.coremedia.cache.Cache;
import com.coremedia.cap.multisite.SitesService;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Inject;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class P13NDynamicContainerStrategyTest {

  @Mock
  private Container outerContainer;

  @Mock
  private Container innerContainer;

  @Mock
  private Container secondInnerContainer;

  @Mock
  private CMP13NSearch persoSearch;

  @Mock
  private CMSelectionRules persoContent;

  @Mock
  private SettingsService settingsService;

  @Mock
  private SitesService sitesService;

  @Mock
  private Cache cache;

  private final P13NDynamicContainerStrategy testling = new P13NDynamicContainerStrategy(settingsService, sitesService, cache);

  @Test
  public void testEmptyContainer() {
    assertThat(testling.containsP13NItemRecursively(Collections.emptyList())).isFalse();
  }

  @Test
  public void testEmptyNestedContainers() {
    when(outerContainer.getItems()).thenReturn(Collections.singletonList(innerContainer));
    when(innerContainer.getItems()).thenReturn(Collections.emptyList());
    assertThat(testling.containsP13NItemRecursively(outerContainer.getItems())).isFalse();
  }

  @Test
  public void testEmptyCyclicContainers() {
    when(outerContainer.getItems()).thenReturn(ImmutableList.of(innerContainer, innerContainer));
    when(innerContainer.getItems()).thenReturn(Collections.singletonList(outerContainer));
    assertThat(testling.containsP13NItemRecursively(outerContainer.getItems())).isFalse();
  }

  @Test
  public void testNegativeSimpleContainer() {
    when(outerContainer.getItems()).thenReturn(ImmutableList.of("foo", "bar"));
    assertThat(testling.containsP13NItemRecursively(outerContainer.getItems())).isFalse();
  }

  @Test
  public void testNegativeSimpleNestedContainer() {
    when(outerContainer.getItems()).thenReturn(ImmutableList.of("foo", innerContainer, innerContainer, "bar"));
    when(innerContainer.getItems()).thenReturn(ImmutableList.of("inner1", "inner2"));
    assertThat(testling.containsP13NItemRecursively(outerContainer.getItems())).isFalse();
  }

  @Test
  public void testFirstNegativeSecondPositiveNestedContainer() {
    when(outerContainer.getItems()).thenReturn(ImmutableList.of("foo", innerContainer, secondInnerContainer, "bar"));
    when(innerContainer.getItems()).thenReturn(ImmutableList.of("inner1", "inner2"));
    when(secondInnerContainer.getItems()).thenReturn(ImmutableList.of(persoContent));
    assertThat(testling.containsP13NItemRecursively(outerContainer.getItems())).isTrue();
  }

  @Test
  public void testPositiveSimpleContainer1() {
    when(outerContainer.getItems()).thenReturn(ImmutableList.of("foo", "bar", persoSearch));
    assertThat(testling.containsP13NItemRecursively(outerContainer.getItems())).isTrue();
  }

  @Test
  public void testPositiveSimpleContainer2() {
    when(outerContainer.getItems()).thenReturn(ImmutableList.of("foo", persoContent, "bar"));
    assertThat(testling.containsP13NItemRecursively(outerContainer.getItems())).isTrue();
  }

  @Test
  public void testPositiveSimpleNestedContainer1() {
    when(outerContainer.getItems()).thenReturn(ImmutableList.of("foo", innerContainer, "bar"));
    when(innerContainer.getItems()).thenReturn(ImmutableList.of(persoSearch, persoSearch, "inner1", "inner2"));
    assertThat(testling.containsP13NItemRecursively(outerContainer.getItems())).isTrue();
  }

  @Test
  public void testPositiveSimpleNestedContainer2() {
    when(outerContainer.getItems()).thenReturn(ImmutableList.of("foo", innerContainer, "bar"));
    when(innerContainer.getItems()).thenReturn(ImmutableList.of("inner1", persoContent, "inner2"));
    assertThat(testling.containsP13NItemRecursively(outerContainer.getItems())).isTrue();
  }

  @Test
  public void testPositiveSimpleNestedContainer3() {
    when(outerContainer.getItems()).thenReturn(ImmutableList.of("foo", innerContainer, persoSearch, "bar"));
    when(innerContainer.getItems()).thenReturn(ImmutableList.of("inner1", "inner2", persoContent));
    assertThat(testling.containsP13NItemRecursively(outerContainer.getItems())).isTrue();
  }
}

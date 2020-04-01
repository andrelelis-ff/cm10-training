package com.coremedia.blueprint.common.util;

import com.coremedia.blueprint.common.contentbeans.CMGallery;
import com.coremedia.blueprint.common.layout.Container;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContainerFlattenerTest {

  @Mock
  private Container outerCollection;

  @Mock
  private Container innerCollection;

  @Mock
  private CMGallery gallery;

  @Test
  public void testEmptyCollection() {
    when(outerCollection.getItems()).thenReturn(Collections.emptyList());
    List<String> result = ContainerFlattener.flatten(outerCollection, String.class);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testEmptyNestedCollections() {
    when(outerCollection.getItems()).thenReturn(Collections.singletonList(innerCollection));
    when(innerCollection.getItems()).thenReturn(Collections.emptyList());
    List<String> result = ContainerFlattener.flatten(outerCollection, String.class);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testEmptyCyclicCollections() {
    when(outerCollection.getItems()).thenReturn(Collections.singletonList(innerCollection));
    when(innerCollection.getItems()).thenReturn(Collections.singletonList(outerCollection));
    List<String> result = ContainerFlattener.flatten(outerCollection, String.class);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testSimpleCollection() {
    when(outerCollection.getItems()).thenReturn(ImmutableList.of("foo", "bar"));
    List<String> result = ContainerFlattener.flatten(outerCollection, String.class);
    assertEquals(ImmutableList.of("foo", "bar"), result);
  }

  @Test
  public void testBadTypedCollection() {
    when(outerCollection.getItems()).thenReturn(ImmutableList.of("foo", 1, 2, "bar"));
    List<String> strings = ContainerFlattener.flatten(outerCollection, String.class);
    assertEquals(ImmutableList.of("foo", "bar"), strings);
    List<Integer> ints = ContainerFlattener.flatten(outerCollection, Integer.class);
    assertEquals(ImmutableList.of(1, 2), ints);
  }

  @Test
  public void testSimpleNestedCollections() {
    when(outerCollection.getItems()).thenReturn(ImmutableList.of("foo", innerCollection, "bar"));
    when(innerCollection.getItems()).thenReturn(ImmutableList.of("inner1", "inner2"));
    List<String> result = ContainerFlattener.flatten(outerCollection, String.class);
    assertEquals(ImmutableList.of("foo", "inner1", "inner2", "bar"), result);
  }

  @Test
  public void testCyclesAndDuplicatesAndTypes() {
    when(outerCollection.getItems()).thenReturn(ImmutableList.of(outerCollection, "1", 11, innerCollection, "7", "3", "8", outerCollection, "9", innerCollection, "10"));
    when(innerCollection.getItems()).thenReturn(ImmutableList.of(innerCollection, 11, outerCollection, "2", "3", "1", "4", true, outerCollection, "5", innerCollection, "6"));
    List<String> result = ContainerFlattener.flatten(outerCollection, String.class);
    assertEquals(ImmutableList.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), result);
  }
}

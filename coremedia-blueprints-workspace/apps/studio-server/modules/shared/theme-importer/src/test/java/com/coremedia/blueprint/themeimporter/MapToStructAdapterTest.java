package com.coremedia.blueprint.themeimporter;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.Map;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MapToStructAdapterTest {

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private StructService structService;

  @Mock
  private StructBuilder structBuilder;

  @Mock
  private Struct struct;

  @Mock
  private ContentType contentType;

  private MapToStructAdapter adapter;

  @Before
  public void setUp() {
    adapter = new MapToStructAdapter(structService, contentRepository);
    when(contentRepository.getContentContentType()).thenReturn(contentType);
    when(structService.createStructBuilder()).thenReturn(structBuilder);
    when(structBuilder.build()).thenReturn(struct);
  }
  
  @Test
  public void testGetStructWithEmptyMap() {
    assertSame(struct, adapter.getStruct(ImmutableMap.of()));

    InOrder inOrder = inOrder(structBuilder);
    inOrder.verify(structBuilder).build();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testGetStructWithBasicTypes() {
    Content someContent = mock(Content.class);
    Calendar someDate = mock(Calendar.class);
    Map<String, Object> json = ImmutableMap.of(
            "string", "def",
            "integer", 5,
            "boolean", true,
            "link", someContent,
            "date", someDate
    );

    assertSame(struct, adapter.getStruct(json));

    InOrder inOrder = inOrder(structBuilder);
    inOrder.verify(structBuilder).declareString("string", Integer.MAX_VALUE, "def");
    inOrder.verify(structBuilder).declareInteger("integer", 5);
    inOrder.verify(structBuilder).declareBoolean("boolean", true);
    inOrder.verify(structBuilder).declareLink("link", contentType, someContent);
    inOrder.verify(structBuilder).declareDate("date", someDate);
    inOrder.verify(structBuilder).build();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testGetStructMultipleCalls() {
    assertSame(struct, adapter.getStruct(ImmutableMap.of("string", "def")));
    assertSame(struct, adapter.getStruct(ImmutableMap.of()));

    InOrder inOrder = inOrder(structBuilder);
    inOrder.verify(structBuilder).declareString("string", Integer.MAX_VALUE, "def");
    inOrder.verify(structBuilder, times(2)).build();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testGetStructWithNestedObject() {
    Map<String, Object> json = ImmutableMap.of(
            "subJson", ImmutableMap.of(
                    "string", "def"
            ),
            "subJson2", ImmutableMap.of(
                    "subSubJson2", ImmutableMap.of(
                            "integer", 5
                    )
            )
    );

    assertSame(struct, adapter.getStruct(json));

    InOrder inOrder = inOrder(structBuilder);
    inOrder.verify(structBuilder).enter("subJson");
    inOrder.verify(structBuilder).declareString("string", Integer.MAX_VALUE, "def");
    inOrder.verify(structBuilder).up();
    inOrder.verify(structBuilder).enter("subJson2");
    inOrder.verify(structBuilder).enter("subSubJson2");
    inOrder.verify(structBuilder).declareInteger("integer", 5);
    inOrder.verify(structBuilder, times(2)).up();
    inOrder.verify(structBuilder).build();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testGetStructWithArray() {
    Content someContent = mock(Content.class);
    Content someOtherContent = mock(Content.class);
    Calendar someDate = mock(Calendar.class);
    Calendar someOtherDate = mock(Calendar.class);
    Map<String, Object> json = ImmutableMap.of(
            "stringList", ImmutableList.of("a", "b"),
            "integerList", ImmutableList.of(1, 2),
            "booleanList", ImmutableList.of(true, false),
            "linkList", ImmutableList.of(someContent, someOtherContent),
            "dateList", ImmutableList.of(someDate, someOtherDate)
    );

    assertSame(struct, adapter.getStruct(json));

    InOrder inOrder = inOrder(structBuilder);
    inOrder.verify(structBuilder).declareStrings("stringList", Integer.MAX_VALUE, ImmutableList.of());
    inOrder.verify(structBuilder).add("stringList", "a");
    inOrder.verify(structBuilder).add("stringList", "b");
    inOrder.verify(structBuilder).declareIntegers("integerList", ImmutableList.of());
    inOrder.verify(structBuilder).add("integerList", 1);
    inOrder.verify(structBuilder).add("integerList", 2);
    inOrder.verify(structBuilder).declareBooleans("booleanList", ImmutableList.of());
    inOrder.verify(structBuilder).add("booleanList", true);
    inOrder.verify(structBuilder).add("booleanList", false);
    inOrder.verify(structBuilder).declareLinks("linkList", contentType, ImmutableList.of());
    inOrder.verify(structBuilder).add("linkList", someContent);
    inOrder.verify(structBuilder).add("linkList", someOtherContent);
    inOrder.verify(structBuilder).declareDates("dateList", ImmutableList.of());
    inOrder.verify(structBuilder).add("dateList", someDate);
    inOrder.verify(structBuilder).add("dateList", someOtherDate);
    inOrder.verify(structBuilder).build();
    inOrder.verifyNoMoreInteractions();
  }
}

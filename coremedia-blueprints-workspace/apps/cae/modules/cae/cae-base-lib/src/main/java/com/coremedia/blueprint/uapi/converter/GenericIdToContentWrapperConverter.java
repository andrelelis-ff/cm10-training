package com.coremedia.blueprint.uapi.converter;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.wrapper.ContentWrapper;
import com.coremedia.cap.content.wrapper.TypedCapStructWrapperFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GenericIdToContentWrapperConverter implements GenericConverter {
  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    return new HashSet<>(
            Arrays.asList(
                    new ConvertiblePair(Integer.class, ContentWrapper.class),
                    new ConvertiblePair(String.class, ContentWrapper.class),
                    new ConvertiblePair(ContentWrapper.class, Integer.class),
                    new ConvertiblePair(ContentWrapper.class, String.class)));
  }

  @Override
  public Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {
    if (source == null) {
      return null;
    }

    if (targetType == null) {
      throw new IllegalArgumentException("Target type must be given, for converting a \"" + source.getClass().getName() + "\"");
    }

    if (!(source instanceof ContentWrapper)) {
      return convertToContentWrapper(source, targetType);
    }

    return convertFromContentWrapper((ContentWrapper) source, targetType);
  }

  private ContentWrapper convertToContentWrapper(Object source, TypeDescriptor targetType) {
    String contentId = parseId(source, targetType);
    Content content = contentRepository.getContent(contentId);

    return createTypedContent(content, targetType);
  }

  private ContentWrapper createTypedContent(Content untypedContent, TypeDescriptor targetType) {
    return (ContentWrapper)typedCapStructWrapperFactory.createTypedAccessWrapper(targetType.getType(), untypedContent);
  }

  private String parseId(Object source, TypeDescriptor targetType) {
    try {
      int id = source instanceof String ? Integer.parseInt((String) source) : (Integer) source;
      return IdHelper.formatContentId(id);
    }
    catch (NumberFormatException | ClassCastException e) {
      // invalid number
      // -> is handled as "bad request" in DefaultHandlerExceptionResolver
      throw new TypeMismatchException(source, targetType.getType(), e);
    }
  }

  @SuppressWarnings("UnusedParameters")
  private Object convertFromContentWrapper(ContentWrapper source, TypeDescriptor targetType) {
    throw new UnsupportedOperationException();
  }

  @Required
  public void setTypedCapStructWrapperFactory(TypedCapStructWrapperFactory typedCapStructWrapperFactory) {
    this.typedCapStructWrapperFactory = typedCapStructWrapperFactory;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  private TypedCapStructWrapperFactory typedCapStructWrapperFactory;
  private ContentRepository contentRepository;
}

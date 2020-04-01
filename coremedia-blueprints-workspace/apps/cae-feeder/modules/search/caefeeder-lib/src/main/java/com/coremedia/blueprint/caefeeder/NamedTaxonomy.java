package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Value object that holds a {@link Content} that represents a taxonomy with the value of its {@link CMTaxonomy#VALUE}
 * property.
 */
public class NamedTaxonomy {

  @NonNull
  private final Content content;
  private final String name;

  public NamedTaxonomy(@NonNull Content content) {
    this.content = requireNonNull(content);
    this.name = content.getString(CMTaxonomy.VALUE);
  }

  @NonNull
  public Content getContent() {
    return content;
  }

  @Nullable
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NamedTaxonomy that = (NamedTaxonomy) o;
    return Objects.equals(content, that.content) &&
           Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, name);
  }

  @Override
  public String toString() {
    return IdHelper.parseContentId(content.getId()) + ':' + name;
  }

}

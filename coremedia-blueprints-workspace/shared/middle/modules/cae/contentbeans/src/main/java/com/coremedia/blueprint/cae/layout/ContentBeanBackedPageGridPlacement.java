package com.coremedia.blueprint.cae.layout;

import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGrid;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridPlacement;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.base.pagegrid.ContentBackedStyle;
import com.coremedia.blueprint.base.pagegrid.ContentBackedStyleGrid;
import com.coremedia.blueprint.base.pagegrid.PageGridConstants;
import com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords;
import com.coremedia.blueprint.base.pagegrid.TableLayoutData;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriod;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator;
import com.coremedia.blueprint.common.layout.HasPageGrid;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.blueprint.common.util.ContainerFlattener;
import com.coremedia.blueprint.viewtype.ViewtypeService;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.AssumesIdentity;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContentBeanBackedPageGridPlacement implements PageGridPlacement, AssumesIdentity {

  private static final String CONTENT_PROPERTIES_PROPERTY = "properties";

  private ValidationService<Linkable> validationService;
  private ValidityPeriodValidator visiblityValidator;
  private ContentBackedPageGridService contentBackedPageGridService;
  private ViewtypeService viewtypeService;

  private HasPageGrid bean;
  private int row;
  private int columns;

  /**
   * Simple list logic:
   * Count starts with 0,
   * does not consider col spans and row spans but refers 1:1 to the items of the row itself.
   */
  private int colIndex;


  // --- construction -----------------------------------------------

  public ContentBeanBackedPageGridPlacement(HasPageGrid bean,
                                            int row, int columns, int colIndex,
                                            ContentBackedPageGridService contentBackedPageGridService,
                                            ValidationService<Linkable> validationService,
                                            ValidityPeriodValidator visibilityValidator,
                                            ViewtypeService viewtypeService) {
    this.bean = bean;
    this.row = row;
    this.columns = columns;
    this.colIndex = colIndex;
    this.contentBackedPageGridService = contentBackedPageGridService;
    this.validationService = validationService;
    this.visiblityValidator = visibilityValidator;
    this.viewtypeService = viewtypeService;
  }

  /**
   * Only for dataviews
   */
  @SuppressWarnings("UnusedDeclaration")
  public ContentBeanBackedPageGridPlacement() {
  }


  // --- PageGridPlacement ------------------------------------------


  public CMNavigation getNavigation() {
    if (bean instanceof CMNavigation) {
      return (CMNavigation) bean;
    }
    return bean.getContext();
  }

  @Override
  public int getNumCols() {
    return columns;
  }

  @Override
  public boolean isEditable() {
    return getLayout().isEditable();
  }

  @Override
  public String getViewTypeName() {
    Content viewType = getDelegate().getViewtype();
    return viewType==null ? null : viewtypeService.getLayout(viewType);
  }

  @Override
  public List<? extends Linkable> getItems() {
    List<AnnotatedLinkWrapper> unfiltered = getItemsUnfiltered();
    List<Linkable> filtered = unfiltered.stream()
            .filter(visiblityValidator::validate)
            .map(AnnotatedLinkWrapper::getTargetBean)
            .collect(Collectors.toList());
    if (validationService == null) {
      return filtered;
    }
    return validationService.filterList(filtered);
  }

  @Override
  public List<Linkable> getFlattenedItems() {
    return ContainerFlattener.flatten(this, Linkable.class);
  }

  @Override
  public String getName() {
    Content section = getDelegate().getSection();
    return section == null ? PageGridConstants.MAIN_PLACEMENT_NAME : section.getName();
  }

  @Override
  public String getPropertyName() {
    return String.format("%s.%s-%s",
            CONTENT_PROPERTIES_PROPERTY,
            PageGridContentKeywords.PAGE_GRID_STRUCT_PROPERTY,
            getName());
  }

  public String getStructPropertyName() {
    return contentBackedPageGridService.getStructPropertyName();
  }

  @Override
  public int getCol() {
    // Do not confuse with the simple list-related colIndex.
    // getCol takes account of row spans and col spans.
    return getLayout().getCol();
  }

  @Override
  public int getColspan() {
    return getLayout().getColspan();
  }

  @Override
  public int getWidth() {
    return getLayout().getWidth();
  }

  @Override
  public Map<String, Object> getAdditionalProperties() {
    return getContentBackedStyle().getAdditionalProperties();
  }

  // --- Dataviews --------------------------------------------------

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ContentBeanBackedPageGridPlacement that = (ContentBeanBackedPageGridPlacement) o;

    // prevent that DataView is loading placement from wrong pagegrid
    ContentBackedPageGridService thisPGService = contentBackedPageGridService;
    ContentBackedPageGridService thatPGService = that.contentBackedPageGridService;
    if (thisPGService != thatPGService) {
      return false;
    }

    if (colIndex != that.colIndex) {
      return false;
    }
    if (row != that.row) {
      return false;
    }
    //noinspection RedundantIfStatement
    if (bean != null ? !bean.equals(that.bean) : that.bean != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = (bean != null ? bean.hashCode() : 0);
    result = 31 * result + row;
    result = 31 * result + colIndex;

    // prevent that DataView is loading placement from wrong pagegrid
    result = (contentBackedPageGridService != null) ? 31 * result + contentBackedPageGridService.hashCode() : result;

    return result;
  }

  @Override
  public void assumeIdentity(Object bean) {
    ContentBeanBackedPageGridPlacement other = (ContentBeanBackedPageGridPlacement) bean;
    validationService = other.validationService;
    contentBackedPageGridService = other.contentBackedPageGridService;
    viewtypeService = other.viewtypeService;
    visiblityValidator = other.visiblityValidator;
    this.bean = other.bean;
    row = other.row;
    colIndex = other.colIndex;
  }

  /**
   * Do not use.
   * <p/>
   * Public only for Dataviews.
   * Retrieves the items of this PageGridPlacement
   */
  public List<AnnotatedLinkWrapper> getItemsUnfiltered() {
    return getDelegate().getExtendedItems().stream()
            .map(AnnotatedLinkWrapper::new)
            .filter(al -> al.getTarget() != null)
            .filter(al -> al.getTarget().isInProduction())
            .collect(Collectors.toList());
  }

  private ContentBackedPageGridPlacement getDelegate() {
    String sectionName = getContentBackedStyle().getSection().getName();
    return getContentBackedPageGrid().getPlacements().get(sectionName);
  }

  private TableLayoutData getLayout() {
    ContentBackedStyle style = getContentBackedStyle();
    return style.getLayout();
  }

  private ContentBackedStyle getContentBackedStyle() {
    ContentBackedStyleGrid styleGrid = getContentBackedPageGrid().getStyleGrid();
    List<ContentBackedStyle> styleRow = styleGrid.getRow(row);
    return styleRow.get(colIndex);
  }

  private ContentBackedPageGrid getContentBackedPageGrid() {
    return contentBackedPageGridService.getContentBackedPageGrid(bean.getContent());
  }

  private ContentBeanFactory getContentBeanFactory() {
    if (bean != null) {
      return bean.getContentBeanFactory();
    } else {
      throw new IllegalStateException("cannot determine content bean factory");
    }
  }

  private class AnnotatedLinkWrapper implements ValidityPeriod {

    @Nullable
    private Content target;

    @Nullable
    private Linkable targetBean;

    @Nullable
    private final Calendar visibleFrom;

    @Nullable
    private final Calendar visibleTo;

    AnnotatedLinkWrapper(@NonNull Struct annotatedLink) {
      target = CapStructHelper.getLink(annotatedLink, PageGridContentKeywords.ANNOTATED_LINK_LIST_TARGET_PROPERTY_NAME);
      targetBean = getContentBeanFactory().createBeanFor(target, Linkable.class);
      visibleFrom = CapStructHelper.getDate(annotatedLink, PageGridContentKeywords.ANNOTATED_LINK_LIST_VISIBLE_FROM_PROPERTY_NAME);
      visibleTo = CapStructHelper.getDate(annotatedLink, PageGridContentKeywords.ANNOTATED_LINK_LIST_VISIBLE_TO_PROPERTY_NAME);
    }

    @Nullable
    Content getTarget() {
      return target;
    }

    @Nullable
    Linkable getTargetBean() {
      return targetBean;
    }

    @Nullable
    @Override
    public Calendar getValidFrom() {
      return visibleFrom;
    }

    @Nullable
    @Override
    public Calendar getValidTo() {
      return visibleTo;
    }
  }
}

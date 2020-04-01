package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.coderesources.CodeResourcesModel;
import com.coremedia.blueprint.common.contentbeans.CMAbstractCode;
import com.coremedia.blueprint.common.contentbeans.MergeableResources;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;

import java.util.Collections;
import java.util.List;

public class MergeableResourcesImpl implements MergeableResources {
  private final ContentBeanFactory contentBeanFactory;
  private final DataViewFactory dataViewFactory;

  private final CodeResourcesModel codeResourcesModel;

  public MergeableResourcesImpl(CodeResourcesModel codeResourcesModel,
                                ContentBeanFactory contentBeanFactory,
                                DataViewFactory dataViewFactory) {
    this.codeResourcesModel = codeResourcesModel;
    this.contentBeanFactory = contentBeanFactory;
    this.dataViewFactory = dataViewFactory;
  }

  @Override
  public CodeResourcesModel getCodeResourceModel() {
    return codeResourcesModel;
  }

  @Override
  public List<CMAbstractCode> getMergeableResources() {
    List<?> codeResources = codeResourcesModel.getLinkTargetList();
    for (Object item : codeResources) {
      if (item instanceof Iterable) {
        @SuppressWarnings("unchecked")
        List<CMAbstractCode> list = contentBeanFactory.createBeansFor((Iterable<Content>) item, CMAbstractCode.class);
        return dataViewFactory!=null ? dataViewFactory.loadAllCached(list, null) : list;
      }
    }
    // impl note: In developer mode there are no mergeable resources at all,
    // and we always end up here.
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return getClass().getName() + "[" + codeResourcesModel + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MergeableResourcesImpl that = (MergeableResourcesImpl) o;

    return codeResourcesModel != null ? codeResourcesModel.equals(that.codeResourcesModel) : that.codeResourcesModel == null;
  }

  @Override
  public int hashCode() {
    return codeResourcesModel != null ? codeResourcesModel.hashCode() : 0;
  }

}

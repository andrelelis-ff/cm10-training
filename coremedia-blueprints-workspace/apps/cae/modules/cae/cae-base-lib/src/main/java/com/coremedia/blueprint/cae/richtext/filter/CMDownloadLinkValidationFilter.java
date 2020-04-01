package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.blueprint.common.contentbeans.CMDownload;
import com.coremedia.xml.Filter;
import org.xml.sax.Attributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Checks if given object is a CMDownload and if it contains a blob.
 */
public class CMDownloadLinkValidationFilter extends LinkValidationFilter {

  @Override
  protected boolean isValid(Attributes atts) {
    Object o = fetchBean(atts);
    if (o instanceof CMDownload) {
      return ((CMDownload)o).getData() != null;
    }
    // this filter shall only avoid null data in CMDownloads
    return true;
  }

  @Override
  public Filter getInstance(HttpServletRequest request, HttpServletResponse response) {
    CMDownloadLinkValidationFilter instance = new CMDownloadLinkValidationFilter();
    instance.setDataViewFactory(getDataViewFactory());
    instance.setIdProvider(getIdProvider());
    instance.setValidationService(getValidationService());
    return instance;
  }
}

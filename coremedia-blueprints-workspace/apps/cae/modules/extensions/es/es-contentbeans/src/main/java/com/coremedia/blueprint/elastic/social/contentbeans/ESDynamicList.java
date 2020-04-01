package com.coremedia.blueprint.elastic.social.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMDynamicList;

import java.util.List;

/**
 * Generated interface for beans of document type "ESDynamicList".
 */
public interface ESDynamicList extends CMDynamicList<Count> {

  /*
   * DEVELOPER NOTE
   * Change the methods to narrow the public interface
   * of the {@link com.coremedia.blueprint.elastic.social.contentbeans.ESDynamicListImpl} implementation bean.
   */

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'ESDynamicList'
   */
  String CONTENTTYPE_ESDYNAMICLIST = "ESDynamicList";

  /**
   * Returns the value of the document property "channel"
   * @return the value
   */
  List<? extends CMChannel> getChannel();

  /**
   * Returns the value of the document property "interval"
   * @return the value
   */
  String getInterval();

  /**
   * Returns the value of the document property "aggregationType"
   * @return the value
   */
  String getAggregationType();
}

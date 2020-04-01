package com.coremedia.blueprint.elastic.social.contentbeans;


import com.coremedia.blueprint.cae.contentbeans.CMDynamicListImpl;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.cap.content.Content;

import java.util.List;

/**
 * Generated base class for beans of document type "ESDynamicList".
 */
public abstract class ESDynamicListBase extends CMDynamicListImpl<Count> implements ESDynamicList {

  /*
   * DEVELOPER NOTE
   * Change {@link com.coremedia.blueprint.elastic.social.contentbeans.ESDynamicListImpl} instead of this class.
   */

  /**
   * Returns the value of the document property "channel"
   * @return the value of the document property "channel"
   */
  @Override
  public List<? extends CMChannel> getChannel() {
    List<Content> contents = getContent().getLinks("channel");
    return createBeansFor(contents, CMChannel.class);
  }

  /**
   * Returns the value of the document property "interval"
   * @return the value of the document property "interval"
   */
  @Override
  public String getInterval() {
    return getContent().getString("interval");
  }


  /**
   * Returns the value of the document property "aggregationType"
   * @return the value of the document property "aggregationType"
   */
  @Override
  public String getAggregationType() {
    return getContent().getString("aggregationType");
  }

}

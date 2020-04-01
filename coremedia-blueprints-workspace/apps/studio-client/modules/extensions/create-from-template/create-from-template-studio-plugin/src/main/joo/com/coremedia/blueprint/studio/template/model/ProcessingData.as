package com.coremedia.blueprint.studio.template.model {
import com.coremedia.cap.content.Content;
import com.coremedia.ui.data.impl.BeanImpl;

/**
 * Data wrapper that contains all user input of the dialog.
 */
public class ProcessingData extends BeanImpl {
  //mandatory dialog properties
  public static const FOLDER_PROPERTY:String = "folder";
  public static const NAME_PROPERTY:String = "name";

  //dialog properties
  public static const SKIP_INITIALIZERS:String = "skipInitializers";

  private var content:Content;

  //property is used to remember all documents that have been touched.
  private var additionalContent:Array = [];

  public function ProcessingData() {
  }

  public function doSkipInitializers():Boolean {
    return get(SKIP_INITIALIZERS);
  }

  public function addAdditionalContent(c:Content):void {
    if(additionalContent.indexOf(c) === -1) {
      additionalContent.push(c);
    }
  }

  /**
   * Returns the folder the content has been created into.
   * @return
   */
  public function getFolder():Content {
    return get(FOLDER_PROPERTY);
  }

  public function getName():String {
    return get(NAME_PROPERTY);
  }

  public function setContent(c:Content):void {
    this.content = c;
  }

  public function getContent():Content {
    return content;
  }

  public function getExtendedPath(content:Content):String {
    return getName() ? content.getPath() + "/" + getName() : content.getPath();
  }


  override public function toString():String {
    var value:String = 'Processing Data: ' + content + ', skipInitializers:' + doSkipInitializers()
            + ', additionalContent:' + additionalContent.length;
    return value;
  }
}
}

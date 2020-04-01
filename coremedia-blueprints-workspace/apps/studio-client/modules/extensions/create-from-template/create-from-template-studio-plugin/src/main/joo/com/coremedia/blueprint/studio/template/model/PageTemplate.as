package com.coremedia.blueprint.studio.template.model {
import com.coremedia.cap.content.Content;

/**
 * Data wrapper that contains all information about a template.
 */
public class PageTemplate {
  private var descriptor:Content;
  private var folder:Content;
  private var page:Content;

  public function PageTemplate(folder:Content, descriptor:Content) {
    this.descriptor = descriptor;
    this.folder = folder;
  }

  public function getDescriptor():Content {
    return descriptor;
  }

  /**
   * Sets the page document this initializer is working on.
   * @param page
   */
  public function setPage(page:Content):void {
    this.page = page;
  }

  public function getPage():Content {
    return page;
  }

  public function getFolder():Content {
    return folder;
  }

  public function toString():String {
    return "PagegridInitializer '" + folder.getName() + "'";
  }
}
}
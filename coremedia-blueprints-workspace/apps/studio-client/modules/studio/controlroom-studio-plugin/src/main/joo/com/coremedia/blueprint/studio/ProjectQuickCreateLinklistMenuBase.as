package com.coremedia.blueprint.studio {

import com.coremedia.cms.editor.sdk.quickcreate.QuickCreateLinklistMenuBase;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.controlroom.project.components.ProjectContentToolbar;
import com.coremedia.cms.editor.sdk.editorContext;

public class ProjectQuickCreateLinklistMenuBase extends QuickCreateLinklistMenuBase {

  public function ProjectQuickCreateLinklistMenuBase(config:ProjectQuickCreateLinklistMenuBase = null) {
    super(config);
  }

  protected function updateProject(content:Content):void {
    var projectContentToolbarCmp:ProjectContentToolbar = findParentByType(ProjectContentToolbar.xtype) as ProjectContentToolbar;
    if (projectContentToolbarCmp) {
      // TODO: Quick fix, needs better solution
      projectContentToolbarCmp.project.addContents([content]);
    }
    editorContext.getContentTabManager().openDocument(content);
  }
}
}

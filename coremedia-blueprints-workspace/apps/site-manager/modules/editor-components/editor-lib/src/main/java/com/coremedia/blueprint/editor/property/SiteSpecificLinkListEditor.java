package com.coremedia.blueprint.editor.property;

import hox.corem.editor.Editor;
import hox.corem.editor.common.FilteringChainedListModel;
import hox.corem.editor.common.SortingChainedListModel;
import hox.corem.editor.proxy.ConcreteResourceHolder;
import hox.corem.editor.proxy.DocumentModel;
import hox.corem.editor.proxy.DocumentTypeModel;
import hox.corem.editor.proxy.FolderModel;
import hox.corem.editor.proxy.LinkListModel;
import hox.corem.editor.proxy.ResourceHolder;
import hox.corem.editor.toolkit.property.FolderLinkListEditor;
import hox.corem.editor.toolkit.property.Property;

import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class SiteSpecificLinkListEditor extends FolderLinkListEditor {

  private Set<String> siteSpecificPaths = new LinkedHashSet<>();

  public void setSiteSpecificPaths(String newPath) {
    StringTokenizer st = new StringTokenizer(newPath, ",");
    while (st.hasMoreTokens()) {
      siteSpecificPaths.add(st.nextToken());
    }
  }

  public SiteSpecificLinkListEditor() {
    super();

    // overwrite folderModel and inject my own implementation, which allows multiple folders
    this.folderModel = new MultiFolderListModel();
  }

  @Override
  public void setPropertyModel(LinkListModel model) {
    super.setPropertyModel(model);
    ((MultiFolderListModel) folderModel).addFolder(folderPath);
    for (String siteSpecificPath : siteSpecificPaths) {
      siteSpecificPath = SiteSpecificEditorStrategy.getSiteSpecificFolderPath(getContext(), siteSpecificPath);
      if (siteSpecificPath != null) {
        ((MultiFolderListModel) folderModel).addFolder(siteSpecificPath);
      }
    }
  }

  /**
   * Copy from hox.corem.editor.toolkit.property.FolderLinkListEditor.FolderListModel, extends to overwrite in FolderLinkListEditor
   * <p/>
   * One folder will have to be already configured in the editor.xml, but you may add additional folders via addFolder(FolderModel folder).
   */
  protected class MultiFolderListModel extends FolderListModel {
    private Set<FolderModel> folders = new LinkedHashSet<>();

    public void addFolder(String path) {
      // add client folder for a test
      FolderModel root = Editor.getEditor().getResourceFactory().getRootFolder();
      FolderModel folder;
      try {
        folder = (FolderModel) root.pathLookup(path);
        if (folder != null) {
          if (folders.add(folder)) {
            // only register listener if the set didn't contain the element
            folder.addFolderModelListener(folderListener);
            updateView();
          }
        } else {
          Property.getLog().error("No folder with path " + path);
        }
      } catch (Exception e) {
        Property.getLog().error("Unexpected error", e);
      }
    }


    /**
     * called from super class -> ignore!
     * folders are added by addFolder instead
     */
    @Override
    protected void setFolder(FolderModel folder) {
    }

    @Override
    public void detach() {
      // remove folderListener first, super clears the object reference!
      for (FolderModel folder : folders) {
        folder.removeFolderModelListener(folderListener);
      }

      super.detach();
      folders.clear();
      folders = null;

      folderListener = null;
    }

    /**
     * Update the items on the right side after changes.
     * <p/>
     * mostly a COPY from the inherited class
     */
    @Override
    protected void updateView() {
      if (folders.size() <= 0) {
        return;
      }

      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          // avoid conflicts with parallel detaching
          if (model == null) {
            return;
          }

          // do not update if right side is not visible
          if (!rightPanel.isVisible()) {
            return;
          }

          removeAllElements();
          addElements();
        }
      });
    }

    private void addElements() {
      try {
        for (FolderModel folder : folders) {
          ListModel content = folder.getContentSynchronously();

          if (Property.getLog().isDebugEnabled()) {
            Property.getLog().debug("Found " + content.getSize() + " candidates for FolderLinkListEditor before filtering");
          }

          content = new SortingChainedListModel(new FilteringChainedListModel(content, getFolderListPredicate()),
                  getFolderListComparator());

          for (int i = 0; i < content.getSize(); i++) {
            DocumentModel dm = (DocumentModel) content.getElementAt(i);
            DocumentTypeModel linkListType = getDocumentType();
            if (dm.isReadable() && (linkListType == null || dm.getResourceType().isSubtypeOf(linkListType))) {
              ResourceHolder rh = new ConcreteResourceHolder(dm);
              if (model.indexOf(rh) == -1) {
                addElement(rh);
              }
            }
          }
        }
      } catch (Exception e) {
        Property.getLog().error("Error building folder model", e);
      }

      if (Property.getLog().isDebugEnabled()) {
        Property.getLog().debug("Had " + getSize() + " candidates of type " + documentType.getName() +
                " for FolderLinkListEditor after filtering");
      }
    }
  }
}

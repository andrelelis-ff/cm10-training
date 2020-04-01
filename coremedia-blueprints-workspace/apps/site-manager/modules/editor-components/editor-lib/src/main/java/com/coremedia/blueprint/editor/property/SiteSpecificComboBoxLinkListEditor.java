package com.coremedia.blueprint.editor.property;

import hox.corem.editor.Editor;
import hox.corem.editor.proxy.DocumentListModel;
import hox.corem.editor.proxy.FolderModel;
import hox.corem.editor.proxy.LinkListModel;
import hox.corem.editor.proxy.ResourceModel;
import hox.corem.editor.toolkit.property.ComboBoxLinkListEditor;
import hox.corem.editor.toolkit.property.Property;

import javax.swing.ListModel;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * <p>A ComboBoxLinkListEditor displays one linked resource in a ComboBox.  The Resources shown in the ComboBox
 * are determined by the path parameter which points to a CoreMedia folder. If a site specific path is given
 * document will be loaded from there as well. Otherwise only the configured folder is used.
 * All documents in this folder can be selected and thus be linked.</p>
 * <p>A ComboBoxLinkListEditor will accept a configurable LinkListRenderer.</p>
 * <p>Configure in <b>editor.xml</b> like this:</p>
 * <pre>
 * &lt;Property name="TestLinkList" editorClass="com.coremedia.blueprint.editor.property.SiteSpecificComboBoxLinkListEditor" path="/" siteSpecificPaths="/Sites">
 *   &lt;LinkListRenderer class="hox.corem.editor.toolkit.property.ImageLinkListRenderer" Property="TestBlob"/>
 * &lt;/Property>
 * </pre>
 */
public class SiteSpecificComboBoxLinkListEditor extends ComboBoxLinkListEditor {

  private Set<String> siteSpecificPaths = new LinkedHashSet<>();
  private Set<FolderModel> siteSpecificFolders = new LinkedHashSet<>();

  /**
   * Set the site specific path where to find the documents to whom we may refer to.
   * @param newPath a comma separated list of paths
   */
  public void setSiteSpecificPaths(String newPath) {
    StringTokenizer st = new StringTokenizer(newPath, ",");
    while (st.hasMoreTokens()) {
      siteSpecificPaths.add(st.nextToken());
    }
  }

  protected void addSiteSpecificFolder(FolderModel newFolder) {
    if (siteSpecificFolders.add(newFolder)) {
      // only register listener if the set didn't contain the element
      newFolder.addFolderModelListener(folderListener);
      updateView();
    }
  }

  /**
   * Returns the items of the combobox.
   * <p/>
   * These items are filtered by the {@link #getPredicate()} later on.
   */
  @Override
  protected ListModel getItems() {
    DocumentListModel items = new DocumentListModel();
    if (folder!=null) {
      for (int i = 0; i < folder.getContentSynchronously().getSize(); i++) {
        items.addElement((ResourceModel) folder.getContentSynchronously().getElementAt(i));
      }
    }
    for (FolderModel siteSpecificFolder : siteSpecificFolders) {
      for (int i = 0; i < siteSpecificFolder.getContentSynchronously().getSize(); i++) {
        items.addElement((ResourceModel) siteSpecificFolder.getContentSynchronously().getElementAt(i));
      }
    }
    return items;
  }

  @Override
  public void release() {
    super.release();
    for (FolderModel folder : siteSpecificFolders) {
      folder.removeFolderModelListener(folderListener);
    }
    siteSpecificFolders.clear();
    siteSpecificFolders = null;
  }

  @Override
  public void setPropertyModel(LinkListModel newModel) {
    for (String siteSpecificPath : siteSpecificPaths) {
      FolderModel root = Editor.getEditor().getResourceFactory().getRootFolder();
      try {
        siteSpecificPath = SiteSpecificEditorStrategy.getSiteSpecificFolderPath(getContext(), siteSpecificPath);
        if (siteSpecificPath != null) {
          FolderModel folder = (FolderModel) root.pathLookup(siteSpecificPath);
          if (folder != null) {
            addSiteSpecificFolder(folder);
          }
        }
      } catch (Exception e) {
        Property.getLog().info("Exception during folder lookup in ComboBoxLinkListEditor", e);
      }
    }
    super.setPropertyModel(newModel);
  }
}
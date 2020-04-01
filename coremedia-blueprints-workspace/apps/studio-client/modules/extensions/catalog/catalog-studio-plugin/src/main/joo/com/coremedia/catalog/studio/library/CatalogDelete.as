package com.coremedia.catalog.studio.library {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.results.DeleteResult;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.logging.Logger;

public class CatalogDelete {

  private var treeRelation:CatalogTreeRelation;
  private var contents:Array;
  private var callback:Function;

  public function CatalogDelete(catalogTreeRelation:CatalogTreeRelation, contents:Array, callback:Function = undefined) {
    this.treeRelation = catalogTreeRelation;
    this.contents = contents;
    this.callback = callback;
  }

  public function execute():void {
    var typeName:String = contents[0].getType().getName();

    //check if there is a parent that is linking to the content to be deleted
    //do not allow to delete the content unless the number of parent is less or equals "1".
    ValueExpressionFactory.createFromFunction(function ():Array {
      return treeRelation.getParents(contents[0]);
    }).loadValue(function (parents:Array):void {
      if (parents.length > 1 && contents[0].getType().isSubtypeOf(CatalogTreeRelation.CONTENT_TYPE_CATEGORY)) {
        Logger.error("Delete action for a catalog item found multiple parents for '"
        + contents[0].getName() + "' (e.g. " + parents[0].getName() + ")");
      }
      //there is exact one parent, so we unlink the node from it and delete it
      else {
        var repository:ContentRepository = contents[0].getRepository();
        var deletions:Array = [];
        var parentsToDelete:Array = [];
        var modifications:Array = [];

        for each(var source:Content in contents) {
          //check only the relation of categories
          if (source.getType().isSubtypeOf(CatalogTreeRelation.CONTENT_TYPE_CATEGORY)) {
            //unlink the category from the parent
            if (CatalogTreeRelationHelper.validateCheckoutState([parents[0]])) {
              modifications.push(CatalogTreeRelationHelper.removeCategoryChild(parents[0], source));
              //remember the folder for deletion
              parentsToDelete.push(source.getParent());
              deletions.push(source);
            }
          }
          else {
            deletions.push(source);
          }
        }
      }

      //finally delete the content
      if (deletions.length > 0) {
        ValueExpressionFactory.createFromFunction(function ():Boolean {
          for each(var deletedContent:Content in deletions) {
            if (deletedContent.isCheckedOutByCurrentSession()) {
              deletedContent.revert();
              return undefined;
            }
            editorContext.getWorkAreaTabManager().closeTab(deletedContent);
          }
          return true;
        }).loadValue(function ():void {
          repository.deleteAll(deletions, function (result:DeleteResult):void {
            //update checkin state
            for each(var modified:Content in modifications) {
              if (modified && !modified.isDeleted() && modified.isCheckedOut()) {
                modified.checkIn();
              }
            }
            //delete the parent folder too
            if(parentsToDelete.length > 0) {
              repository.deleteAll(parentsToDelete, function (parentDeletionResult:DeleteResult):void {
                //we pass the result of the children here
                callback(result);
              });
            }
            else {
              callback(result);
            }
          });
        });

      }
    });
  }
}
}
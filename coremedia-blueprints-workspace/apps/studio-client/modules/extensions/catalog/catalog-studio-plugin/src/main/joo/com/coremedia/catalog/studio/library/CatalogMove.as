package com.coremedia.catalog.studio.library {
import com.coremedia.cap.content.Content;

public class CatalogMove {
  private var treeRelation:CatalogTreeRelation;
  private var sources:Array;
  private var newParent:Content;
  private var callback:Function;

  public function CatalogMove(catalogTreeRelation:CatalogTreeRelation, sources:Array, newParent:Content, callback:Function = undefined) {
    this.treeRelation = catalogTreeRelation;
    this.sources = sources;
    this.newParent = newParent;
    this.callback = callback;
  }

  public function execute():void {
    var sourceParent:Content = treeRelation.getParent(sources[0]);
    if (CatalogTreeRelationHelper.validateCheckoutState(sources.concat(sourceParent).concat(newParent))) {
      var modifications:Array = [];

      // update linking
      for each (var source:Content in sources) {
        modifications.push(CatalogTreeRelationHelper.addCategoryChild(newParent, source));
        modifications.push(CatalogTreeRelationHelper.removeCategoryChild(sourceParent, source));
      }


      //move the content, we use the callback to check the check-in state afterwards
      CatalogTreeRelationHelper.updateLocation(sources, newParent, function ():void {
        //update checkin state
        for each(var modified:Content in modifications) {
          if (modified && modified.isCheckedOut()) {
            modified.checkIn();
          }
        }
        if (callback) {
          callback();
        }
      });
    }
  }

}
}

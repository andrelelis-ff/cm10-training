package com.coremedia.blueprint.studio.util {

import com.coremedia.ui.data.impl.RemoteService;
import com.coremedia.ui.util.EncodingUtil;

import ext.DateUtil;
import ext.data.Model;
import ext.data.NodeInterface;
import ext.event.Event;
import ext.tree.TreePanel;
import ext.util.Format;

import js.HTMLElement;

import mx.resources.ResourceManager;

/**
 * Common utility method for the studio.
 */
[ResourceBundle('com.coremedia.cms.editor.Editor')]
public class BlobMetadataUtil {

  public static function rowDblClick(tree:TreePanel, record:Model, tr:HTMLElement, rowIndex:Number, e:Event):void {
    if (record && record.data.leaf && record.data.url) {
      var url:String = RemoteService.calculateRequestURI(record.data.url);
      window.open(url);
    }
  }

  public static function convertDirectoryTree(files:Array):NodeInterface {
    var root:* = {
      expanded: true,
      visible: true,
      leaf: false,
      size: 0,
      directory: true,
      text: 'root',
      children: convertChildren(files)
    };
    return root;
  }

  private static function convertChildren(files:Array):Array {
    var result:Array = [];
    files.forEach(function (f:*):void {
      var node:Object = {
        text: f.name,
        time: f.time,
        size: f.size,
        leaf: !f.directory,
        url: f.url,
        children : convertChildren(f.children)
      };
      result.push(node);
    });
    return result;
  }

  public static function emptyRootNode():Object {
    return {
      expanded: true,
      visible: true,
      leaf: false,
      text: 'root',
      children: []
    };
  }

  public static function fileNameRenderer(value:*, metaData:*, record:*):String {
    return EncodingUtil.encodeForHTML(record.data.text);
  }

  public static function fileSizeRenderer(value:*, metaData:*, record:*):String {
    var directory:Boolean = !record.data.leaf;
    var size:Number = record.data.size;
    return directory ? '' : Format.fileSize(size);
  }

  public static function fileDateRenderer(value:*, metaData:*, record:*):String {
    if(record.data.time) {
      return DateUtil.format(record.data.time, ResourceManager.getInstance().getString('com.coremedia.cms.editor.Editor', 'dateFormat'));
    }

    return '';
  }
}
}

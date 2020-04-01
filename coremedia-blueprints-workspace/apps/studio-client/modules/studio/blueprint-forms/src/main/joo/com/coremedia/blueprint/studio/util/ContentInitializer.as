package com.coremedia.blueprint.studio.util {
import com.coremedia.blueprint.studio.TeaserOverlayConstants;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentProperties;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.TeaserOverlayManager;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.sites.SitesService;

import ext.Ext;

import joo.localeSupport;

import mx.resources.ResourceManager;

/**
 * Initializer settings for the blueprint project.
 */
[ResourceBundle('com.coremedia.cms.editor.sdk.actions.Actions')]
public class ContentInitializer {

  /**
   * Feature flag to enable teaser overlay by default.
   */
  private static const ENABLE_TEASER_OVERLAY_BY_DEFAULT:Boolean = false;

  /**
   * The registration of the initializers for the corresponding document types.
   */
  public static function applyInitializers():void {
    editorContext.registerContentInitializer("CMArticle", initArticle);
    editorContext.registerContentInitializer("CMAudio", initAudio);
    editorContext.registerContentInitializer("CMChannel", initChannel);
    editorContext.registerContentInitializer("CMCollection", initCollection);
    editorContext.registerContentInitializer("CMDownload", initTeasable);
    editorContext.registerContentInitializer("CMImageMap", initCMImageMap);
    editorContext.registerContentInitializer("CMLinkable", initCMLinkable);
    editorContext.registerContentInitializer("CMLocalized", initCMLocalized);
    editorContext.registerContentInitializer("CMTaxonomy", initTaxonomy);
    editorContext.registerContentInitializer("CMLocTaxonomy", initTaxonomy);
    editorContext.registerContentInitializer("CMMedia", initTeasable);
    editorContext.registerContentInitializer("CMPicture", initPicture);
    editorContext.registerContentInitializer("CMQueryList", initQueryList);
    editorContext.registerContentInitializer("CMTeasable", initTeaser);
    editorContext.registerContentInitializer("CMViewtype", initViewType);
    editorContext.registerContentInitializer("CMVideo", initVideo);
    editorContext.registerContentInitializer("CMSpinner", initSpinner);
    editorContext.registerContentInitializer("CMFolderProperties", initFolderProperties);
    editorContext.registerContentInitializer("CMTheme", Ext.emptyFn);
  }

  private static function initFolderProperties(content:Content):void {
    content.rename("_folderProperties");
    initCMLocalized(content);
  }

  private static function initSpinner(content:Content):void {
    var localSettings:Struct = content.getProperties().get("localSettings");
    localSettings.getType().addStructProperty("commerce");
    var commerceStruct:Struct = localSettings.get("commerce");
    commerceStruct.getType().addBooleanProperty("inherit", true);
    initCMLinkable(content);
  }

  private static function initViewType(content:Content):void {
    initCMLocalized(content);
    initializePropertyWithName(content, 'layout');
  }

  private static function initQueryList(content:Content):void {
    var localSettings:Struct = content.getProperties().get('localSettings');
    localSettings.getType().addIntegerProperty('limit', 10);
    initCMLinkable(content);
    initCMLocalized(content);
  }

  private static function initTeaser(content:Content):void {
    initializePropertyWithName(content, 'teaserTitle');
    initializeTeaserOverlay(content);
    initCMLinkable(content);
  }

  private static function initPicture(content:Content):void {
    initializePropertyWithName(content, 'title');
    initializePropertyWithName(content, 'alt');
    initCMLinkable(content);
  }

  private static function initAudio(content:Content):void {
    initializePropertyWithName(content, 'title');
    initCMLinkable(content);
  }

  private static function initVideo(content:Content):void {
    initializePropertyWithName(content, 'title');
    initCMLinkable(content);
  }

  private static function initTeasable(content:Content):void {
    initializePropertyWithName(content, 'title');
    initializeTeaserOverlay(content);
    initCMLinkable(content);
  }

  private static function initCollection(content:Content):void {
    initializePropertyWithName(content, 'teaserTitle');
    initCMLinkable(content);
  }

  private static function initTaxonomy(content:Content):void {
    initTeasable(content);
    initializePropertyWithName(content, 'value');
  }

  private static function initArticle(content:Content):void {
    if ((content.getProperties().get("title") as String).length < 1) {
      initializePropertyWithName(content, 'title');
    }
    initializeTeaserOverlay(content);
    initCMLinkable(content);
  }

  public static function initChannel(content:Content):void {
    initializePropertyWithName(content, 'title');
    initializePropertyWithName(content, 'segment');
    initCMLinkable(content);
  }

  public static function initCMLocalized(content:Content):void {
    var sitesService:SitesService = editorContext.getSitesService();
    var site:Site = sitesService.getSiteFor(content) || sitesService.getPreferredSite();
    var locale:String;
    if (site) {
      locale = site.getLocale().getLanguageTag();
    } else {
      locale = localeSupport.getLocale();
    }
    setProperty(content, 'locale', locale);
  }

  public static function initCMLinkable(content:Content):void {
    initCMLocalized(content);
  }

  private static function initCMImageMap(content:Content):void {
    initCMLinkable(content);
    var localSettings:Struct = content.getProperties().get('localSettings');
    localSettings.getType().addStructProperty("overlay");
    var overlay:* = localSettings.get("overlay");
    overlay.set("displayTitle", true);
  }

  public static function initializePropertyWithName(content:Content, property:String):void {
    //Only initialize if the name of the content is not "New content item"
    if (content.getName() != ResourceManager.getInstance().getString('com.coremedia.cms.editor.sdk.actions.Actions', 'Action_newContent_newDocumentName_text')) {
      setProperty(content, property, content.getName());
    }
  }

  private static function initializeTeaserOverlay(content:Content):void {
    // check feature flag
    if (!ENABLE_TEASER_OVERLAY_BY_DEFAULT) {
      return;
    }
    var manager:TeaserOverlayManager = TeaserOverlayManager.getInstance();
    manager.initializeTeaserOverlay(
            content,
            TeaserOverlayConstants.DEFAULT_SETTINGS_PATH,
            TeaserOverlayConstants.DEFAULT_STYLE_DESCRIPTOR_FOLDER_PATHS,
            TeaserOverlayConstants.DEFAULT_STYLE_NAME
    );
  }

  public static function setProperty(content:Content, property:String, value:Object):void {
    var properties:ContentProperties = content.getProperties();
    properties.set(property, value);
    content.flush();
  }
}
}

Extension point for solr-config
===============================

This extension point is yet unused.  We will refactor the blueprint solr-config
files later.

For a solr-config extension, start over with the module-template next to this readme:
*  Copy it into your extension, and rename it accordingly.
*  Add it to the <modules> of your extension.
*  Adjust the parent pom to your extension.
*  Activate the extension with the CoreMedia extension tool.
*  Populate the configsets directory with schema.xml files, following the
   directory structure of blueprint/modules/search/solr-config/src/main/app/configsets
   There is already an example schema.xml file for the cae configset, adjust or delete it.
   You can add schema.xml files for the other configsets (currently content and elastic).

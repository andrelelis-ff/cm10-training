import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.nio.charset.StandardCharsets
import java.nio.file.Files

ApplyConfigsetExtensions.main([
        "${project.properties['solr.tooling.srcConfigsets']}",
        "${project.properties['solr.tooling.extensions']}",
        "${project.properties['solr.tooling.targetConfigsets']}"
] as String[])


class ApplyConfigsetExtensions {
  // Attribute ordering hack
  // Must be a valid attribute name, alphabetically leading, and sufficiently unique for s&r
  private static final String ATTRIBUTE_ORDER_PREFIX = ':-------------------com.coremedia.tooling.applyConfigsetExtensions'
  private static final String ATTRIBUTE_ORDER_POSTFIX = '-'

  private static final String SCHEMA_IN_CONFIGSET = 'conf/schema.xml'
  private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()


  // --- main -------------------------------------------------------

  static void main(String[] args) {
    try {
      log('INFO', 'Apply extension configsets...', null)
      File srcConfigsets = new File(args[0])
      File extensions = new File(args[1])
      File targetConfigsets = new File(args[2])

      // arg checks
      if (!srcConfigsets.exists() || !srcConfigsets.isDirectory()) {
        log('WARN', "No configsets to be extended at ${args[0]}.  This looks like a configuration error.", null)
        return
      }
      String[] extensionDirs = extensions.exists() ? extensions.list() : null
      if (extensionDirs==null || extensionDirs.length==0) {
        // Impl note: INFO only, because it is likely that we will have a basic
        // product without configset extensions.
        log('INFO', "No configset extensions to apply at ${args[1]}", null)
        return
      }
      if (!targetConfigsets.exists()) {
        log('WARN', "${targetConfigsets} does not yet exist.  This may indicate a maven lifecycle mismatch with the coremedia-application-maven-plugin.  Make sure that the extended configsets are not overwritten again in a later execution.", null)
        if (!targetConfigsets.mkdirs()) {
          throw new IllegalStateException("Cannot create directory ${args[2]}")
        }
      }

      // Go!
      process(srcConfigsets, extensions, targetConfigsets)
      log('INFO', 'Applied extension configsets.', null)
    } catch (Exception e) {
      log('ERROR', 'Cannot apply configset extensions!', e)
    }
  }


  // --- internal ---------------------------------------------------

  private static void process(File srcConfigsets, File extensions, File targetConfigsets) throws ParserConfigurationException, IOException, SAXException, TransformerException {
    for (File configset : srcConfigsets.listFiles()) {  // NOSONAR not null for directories
      if (configset.isDirectory()) {
        File targetConfigset = new File(targetConfigsets, configset.getName())
        extendConfigset(configset, extensions, targetConfigset)
      }
    }
  }

  private static void extendConfigset(File srcConfigset, File extensions, File targetConfigset) throws ParserConfigurationException, IOException, SAXException, TransformerException {
    File srcSchemaFile = new File(srcConfigset, SCHEMA_IN_CONFIGSET)
    if (srcSchemaFile.exists()) {
      Document schemaDom = dbf.newDocumentBuilder().parse(srcSchemaFile)
      for (File extension : extensions.listFiles()) {  // NOSONAR not null for directories
        File extensionSchemaFile = new File(new File(new File(extension, 'configsets'), srcConfigset.getName()), SCHEMA_IN_CONFIGSET)
        if (extensionSchemaFile.exists()) {
          log('INFO', "Merge ${extensionSchemaFile.getAbsolutePath().substring(extensions.getAbsolutePath().length()+1)} into ${srcSchemaFile.getAbsolutePath().substring(srcSchemaFile.getAbsolutePath().indexOf('configsets'))}", null)
          Document extensionSchemaDom = dbf.newDocumentBuilder().parse(extensionSchemaFile)
          merge(schemaDom, extensionSchemaDom, extension.getName())
        }
      }

      File targetSchemaFile = new File(targetConfigset, SCHEMA_IN_CONFIGSET)
      // add/removeAttributeOrderPrefixes is only for human readability.
      // If this ever causes trouble, do not spend too much effort on it,
      // but just delete it.
      addAttributeOrderPrefixes(schemaDom)
      write(schemaDom, targetSchemaFile)
      removeAttributeOrderPrefixes(targetSchemaFile)
    }
  }

  private static void merge(Document schemaDom, Document mergeSchemaDom, String extension) {
    Element targetParent = schemaDom.getDocumentElement()
    NodeList mergeNodes = mergeSchemaDom.getDocumentElement().getChildNodes()
    appendTopLevelComment(schemaDom, targetParent, "EXTENSION ${extension}")
    for (int i=0; i<mergeNodes.getLength(); ++i) {
      targetParent.appendChild(schemaDom.importNode(mergeNodes.item(i), true))
    }
  }

  private static void appendTopLevelComment(Document nodeFactory, Node node, String msg) {
    node.appendChild(nodeFactory.createTextNode('\n  '))
    node.appendChild(nodeFactory.createComment(formatComment(msg)))
    node.appendChild(nodeFactory.createTextNode('\n  '))
  }

  private static String formatComment(String msg) {
    """ =====================================================================
       ${msg}
       ===================================================================== """.toString()
  }

  private static void write(Node node, File targetFile) throws TransformerException {
    targetFile.getParentFile().mkdirs()
    Transformer serializer = TransformerFactory.newInstance().newTransformer()
    Properties oprops = new Properties()
    oprops.put(OutputKeys.METHOD, 'xml')
    oprops.put(OutputKeys.OMIT_XML_DECLARATION, 'no')
    oprops.put(OutputKeys.ENCODING, 'UTF-8')
    serializer.setOutputProperties(oprops)
    serializer.transform(new DOMSource(node), new StreamResult(targetFile))
  }

  /**
   * Temporarily rename attributes for ordering
   * <p>
   * Java's XML APIs do not support control over the order of attributes.
   * (Neither does XML itself.)
   * As a matter of fact, though, the serializer writes attributes in
   * alphabetical order.  We exploit this behaviour in that we prepend the
   * attributes with prefixes to get them ordered in our preferred human
   * readable fashion.
   * The prefixes must be removed with {@link #removeAttributeOrderPrefixes}
   * afterwards.
   */
  private static addAttributeOrderPrefixes(Node dom) {
    prefixAttributesForOrdering(dom, 'field', ['name', 'type', 'indexed', 'stored', 'multiValued', 'useDocValuesAsStored'])
    prefixAttributesForOrdering(dom, 'dynamicField', ['name', 'type', 'indexed', 'stored', 'multiValued', 'useDocValuesAsStored'])
    prefixAttributesForOrdering(dom, 'fieldType', ['name', 'class', 'docValues', 'precisionStep', 'positionIncrementGap', 'autoGeneratePhraseQueries'])
  }

  private static void prefixAttributesForOrdering(Node node, String elementName, List<String> orderedNames) {
    if (node instanceof Element && elementName.equals(node.getNodeName())) {
      Element element = (Element)node
      int counter = 0
      for (String attributeName : orderedNames) {
        Attr attrNode = element.getAttributeNode(attributeName)
        if (attrNode != null) {
          element.removeAttribute(attributeName)
          element.setAttribute("${ATTRIBUTE_ORDER_PREFIX}${counter++}${ATTRIBUTE_ORDER_POSTFIX}${attributeName}", attrNode.getValue())
        }
      }
    }
    NodeList nodelist = node.getChildNodes()
    for (int i=nodelist.getLength(); --i>=0;) {
      prefixAttributesForOrdering(nodelist.item(i), elementName, orderedNames)
    }
  }

  private static void removeAttributeOrderPrefixes(File xmlFile) {
    String content = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8)
    content = content.replaceAll("${ATTRIBUTE_ORDER_PREFIX}[0-9]+${ATTRIBUTE_ORDER_POSTFIX}", '')
    Files.write(xmlFile.toPath(), content.getBytes(StandardCharsets.UTF_8))
  }

  private static void log(String level, String msg, Exception e) {
    System.out.println("[${level}] ApplyConfigsetExtensions: ${msg}")
    if (e != null) {
      // repeat message on System.err, because the stacktrace goes there.
      System.err.println("[${level}] ApplyConfigsetExtensions: ${msg}")
      e.printStackTrace()
    }
  }
}

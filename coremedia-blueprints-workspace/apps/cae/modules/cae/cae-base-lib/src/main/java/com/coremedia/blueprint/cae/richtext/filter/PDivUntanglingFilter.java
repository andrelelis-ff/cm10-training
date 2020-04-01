package com.coremedia.blueprint.cae.richtext.filter;

import com.coremedia.xml.Filter;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.List;

/**
 * An abstract Filter that handles block/flow context mismatches when embedding data
 * like link targets or images
 * <p>
 * If a block level snippet is to be embedded into a &lt;p&gt;, the &lt;p&gt; context
 * is closed before and recovered after the embedding.
 * <p>
 * The assertions in this class refer to internal logic and assume a correct
 * (i.e. wellformed) invocation of the filter methods.
 */
public abstract class PDivUntanglingFilter extends Filter implements FilterFactory {
  private static final Logger LOG = LoggerFactory.getLogger(PDivUntanglingFilter.class);

  @VisibleForTesting
  boolean strictNestedPCheck = false;

  /**
   * Counter to memorize markup nesting in which embedding is currently taking place
   */
  private int skipLevelsDuringEmbedding;
  private SaxElementStack elementStack = new SaxElementStack();

  /**
   * Buffer for attributes of the currently parsed withhold &lt;p&gt; tag.
   */
  private DelayedPData delayedP;


  // --- abstract ---------------------------------------------------

  /**
   * Decide whether the data denoted by this element is to be embedded
   * <p>
   * Contract: {@link #hasBlockLevel(String, Attributes)} and {@link #renderEmbeddedData(String, Attributes)}
   * are invoked only if mustEmbed is true for the tag/atts.  Thus, implementations
   * do not need to check the arguments again.
   */
  protected abstract boolean mustEmbed(String tag, Attributes atts);

  /**
   * Returns true if the data to be embedded will have block level
   * (e.g. starts with a &lt;div&gt;) or false if it will be a flow level
   * snippet (i.e. valid to be rendered inside a &lt;p&gt; element)
   */
  protected abstract boolean hasBlockLevel(String tag, Attributes atts);

  /**
   * Render the data to be embedded
   */
  protected abstract void renderEmbeddedData(String tag, Attributes atts);


  // --- Filter -----------------------------------------------------

  /**
   * Initialize this instance with default values when a document starts
   */
  @Override
  public void startDocument() throws SAXException {
    skipLevelsDuringEmbedding = 0;
    delayedP = null;
    elementStack.clear();
    super.startDocument();
  }

  /**
   * Possibly replace the element with embedded data
   * <p>
   * If required by {@link #mustEmbed(String, Attributes)}, replace this
   * element (incl. any children) by {@link #renderEmbeddedData(String, Attributes)}
   */
  @Override
  public void startElement(String namespaceUri, String localName, String qName, Attributes atts) throws SAXException {
    String tag = asTag(namespaceUri, localName, qName);
    if (skipLevelsDuringEmbedding==0) {
      if (mustEmbed(tag, atts)) {
        embedData(tag, atts);
        ++skipLevelsDuringEmbedding;
      } else {
        startDelayed();
        startOrDelay(namespaceUri, localName, qName, atts);
      }
    } else {
      String parentTag = elementStack.isEmpty() ? "(root)" : elementStack.top().asTag();
      LOG.warn("Cannot handle nested element {} in a {} with mode embedded. Ignore.", tag, parentTag);
      ++skipLevelsDuringEmbedding;
    }
  }

  /**
   * In case this method is called, and a &lt;p&gt; tag has been withheld, render the &lt;p&gt; tag with it's
   * original attributes. In either case, call the super implementation.
   */
  @Override
  public void endElement(String namespaceUri, String localName, String qName) throws SAXException {
    if (skipLevelsDuringEmbedding==0) {
      assert delayedP==null || isTag(namespaceUri, localName, qName, "p") : "delayed P should have been executed at the opening of this " + asTag(namespaceUri, localName, qName);
      if (delayedP==null || !delayedP.isTouched()) {
        // Regular closing of a tag
        startDelayed();
        super.endElement(namespaceUri, localName, qName);
        elementStack.pop();  // NOSONAR  Don't need the result here, but must sync the stack.
      } else {
        // Special case:
        // We have a delayed p which is to be closed now. I.e. it is empty.
        // It is also touched, i.e. it became empty by this filter.  Drop it.
        delayedP = null;
      }
    } else {
      assert skipLevelsDuringEmbedding>0 : "mismatching open/close counter while embedding data";
      --skipLevelsDuringEmbedding;
    }
  }

  /**
   * In case this method is called, and a &lt;p&gt; tag has been withheld, render the &lt;p&gt; tag with it's
   * original attributes. In either case, call the super implementation.
   */
  @Override
  public void characters(char[] text, int start, int length) throws SAXException {
    if (skipLevelsDuringEmbedding==0) {
      startDelayed();
      super.characters(text, start, length);
    }
  }

  /**
   * In case this method is called, and a &lt;p&gt; tag has been withheld, render the &lt;p&gt; tag with it's
   * original attributes. In either case, call the super implementation.
   */
  @Override
  public void ignorableWhitespace(char[] text, int start, int length) throws SAXException {
    if (skipLevelsDuringEmbedding==0) {
      startDelayed();
      super.ignorableWhitespace(text, start, length);
    }
  }

  /**
   * In case this method is called, and a &lt;p&gt; tag has been withheld, render the &lt;p&gt; tag with it's
   * original attributes. In either case, call the super implementation.
   */
  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    if (skipLevelsDuringEmbedding==0) {
      startDelayed();
      super.processingInstruction(target, data);
    }
  }

  /**
   * In case this method is called, and a &lt;p&gt; tag has been withheld, render the &lt;p&gt; tag with it's
   * original attributes. In either case, call the super implementation.
   */
  @Override
  public void skippedEntity(String name) throws SAXException {
    if (skipLevelsDuringEmbedding==0) {
      startDelayed();
      super.skippedEntity(name);
    }
  }

  @Override
  public void endDocument() throws SAXException {
    assert elementStack.isEmpty() : "Stack not empty at endDocument().  This indicates a bug in the LinkEmbedFilter.";
    super.endDocument();
  }


  // --- internal ---------------------------------------------------

  /**
   * Convenient check whether the tag matches the localName or qName of a Sax event.
   */
  private static boolean isTag(String uri, String localName, String qName, String tag) {
    return tag.equalsIgnoreCase(asTag(uri, localName, qName));
  }

  private static String asTag(String uri, String localName, String qName) {
    return "".equals(uri) ? qName : localName;
  }

  /**
   * If the element is a &lt;p&gt;, keep it in mind, else start it immediately.
   */
  private void startOrDelay(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if (isTag(uri, localName, qName, "p")) {
      assert delayedP==null : "Cannot delay nested elements";
      delayedP = new DelayedPData(new AttributesImpl(atts));
    } else {
      super.startElement(uri, localName, qName, atts);
      elementStack.push(new SaxElementData(uri, localName, qName, atts));
    }
  }

  /**
   * If there is a delayed &lt;p&gt;, start it now.
   */
  private void startDelayed() throws SAXException {
    if (delayedP != null) {
      super.startElement("", "", "p", delayedP.getAttributes());
      elementStack.push(new SaxElementData("", "", "p", delayedP.getAttributes()));
      delayedP = null;
    }
  }

  private void embedData(String tag, Attributes atts) throws SAXException {
    // Do we need a block context?
    boolean mustCloseP = hasBlockLevel(tag, atts);
    // Are we inside a p, i.e. in a flow context?
    List<SaxElementData> saxPContext = elementStack.subStack("p");
    if (saxPContext!=null && delayedP!=null) {
      logNestedP();
      // Nested Ps are an unspecified (strictly spoken illegal) state,
      // therefore we do not interfere.
      mustCloseP = false;
    }

    // Mediate mismatching block/flow context for embedding
    if (mustCloseP) {
      // If there is an open p, close it temporarily.
      if (saxPContext!=null) {
        for (int i=saxPContext.size()-1; i>=0; --i) {
          SaxElementData sed = saxPContext.get(i);
          super.endElement(sed.getNamespaceUri(), sed.getLocalName(), sed.getqName());
        }
      }
      // If we are inside a delayed p, mark it as affected by link embedding.
      // Do not start it, but keep it as the state to be recovered after embedding.
      if (delayedP!=null) {
        delayedP.touch();
      }
    } else {
      // If the embedding is an inline snippet, start a delayed p.
      // If there is no delayed p, there is nothing to do, because
      // an open p or an open div are both ok for an inline snippet.
      startDelayed();
    }

    // Now embed the data
    renderEmbeddedData(tag, atts);

    // Recover the temporarily closed p context.
    // If there is a delayed p, the state is already correct.
    // If there is a saxPContext, it has to be recovered.
    if (mustCloseP && saxPContext!=null) {
      int saxPContextSize = saxPContext.size();
      if (saxPContextSize>1) {
        // reopen the temporarily closed elements (p and possible children)
        for (SaxElementData sed : saxPContext) {
          super.startElement(sed.getNamespaceUri(), sed.getLocalName(), sed.getqName(), sed.getAtts());
        }
      } else {
        // There was an open p with no children yet.
        // Recover it as delayed and touched.  If it remains empty,
        // the embed link was the last data in the original p, and this
        // second part of the splitted p is to be omitted.
        assert saxPContextSize==1 : "unexpected saxPContent size: " + saxPContextSize;
        SaxElementData sed = saxPContext.get(0);
        startOrDelay(sed.getNamespaceUri(), sed.getLocalName(), sed.getqName(), sed.getAtts());
        assert delayedP!=null : "There must be a delayedP now.";
        delayedP.touch();
        elementStack.pop();
      }
    }
  }

  private void logNestedP() {
    if (strictNestedPCheck) {
      // If you are definitely sure that your richtext has no nested <p>s this
      // state indicates a bug in the filter chain, possibly in this particular
      // filter itself.  We use this mode for unit tests.
      assert false : "Our unit test content has no nested <p>s.";
    } else {
      // Generally, an invocation of a PDivUntanglingFilter with nested <p>s is
      // possible, e.g. by errors in preceding filters or invalid richtext values
      // in the content repository.  Warn and continue.
      LOG.warn("Encountered nested paragraphs, which is invalid.");
    }
  }

  private static class DelayedPData {
    private Attributes attributes;
    private boolean touched = false;

    DelayedPData(Attributes attributes) {
      this.attributes = attributes;
    }

    Attributes getAttributes() {
      return attributes;
    }

    boolean isTouched() {
      return touched;
    }

    void touch() {
      touched = true;
    }
  }
}

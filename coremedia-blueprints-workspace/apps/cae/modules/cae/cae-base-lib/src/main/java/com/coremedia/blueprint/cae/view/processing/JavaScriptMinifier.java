package com.coremedia.blueprint.cae.view.processing;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A {@link Minifier} implementation for Javascript. Uses the YUI-Compressor to minify JavaScript.
 */
public class JavaScriptMinifier implements Minifier {

  private static final Logger LOG = LoggerFactory.getLogger(JavaScriptMinifier.class);

  private int linebreak = -1;
  private boolean munge = true;
  private boolean verbose = false;
  private boolean preserveAllSemicolons = false;
  private boolean disableOptimization = false;

  // --- Spring Config -------------------------------------------------------------------------------------------------

  /**
   * Specifies the linebreak-param for the YUICompressor. From YUI-Documentation:
   * <p/>
   * Specify 0 to get a line break after each semi-colon in JavaScript, and after each rule in CSS.
   * <p/>
   * Default: -1 (no linebreak at all)
   *
   * @param linebreak the number of lines after which a linebreak will occur.
   */
  public void setLinebreak(int linebreak) {
    this.linebreak = linebreak;
  }

  public int getLinebreak() {
    return linebreak;
  }

  /**
   * Specifies the munge-param for the YUICompressor. From YUI-Documentation:
   * <p/>
   * noMunge: Minify only. Do not obfuscate local symbols.
   * obviously, the opposite happens here.
   * <p/>
   * Default: true
   *
   * @param munge if munge
   */
  public void setMunge(boolean munge) {
    this.munge = munge;
  }

  public boolean isMunge() {
    return munge;
  }

  /**
   * Specifies the verbose-param for the YUICompressor. From YUI-Documentation:
   * <p/>
   * Display informational messages and warnings.
   * <p/>
   * Default: false
   *
   * @param verbose if verbose
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public boolean isVerbose() {
    return verbose;
  }

  /**
   * Specifies the preserveAllSemicolons-param for the YUICompressor. From YUI-Documentation:
   * <p/>
   * Preserve unnecessary semicolons (such as right before a '}') This option is useful when compressed
   * code has to be run through JSLint (which is the case of YUI for example)
   * <p/>
   * Default: false
   *
   * @param preserveAllSemicolons if preserve Semicolons
   */
  public void setPreserveAllSemicolons(boolean preserveAllSemicolons) {
    this.preserveAllSemicolons = preserveAllSemicolons;
  }

  public boolean isPreserveAllSemicolons() {
    return preserveAllSemicolons;
  }

  /**
   * Specifies the preserveAllSemicolons-param for the YUICompressor. From YUI-Documentation:
   * <p/>
   * Disable all the built-in micro optimizations.
   * <p/>
   * Default: false
   *
   * @param disableOptimization if disableOpt
   */
  public void setDisableOptimization(boolean disableOptimization) {
    this.disableOptimization = disableOptimization;
  }

  public boolean isDisableOptimization() {
    return disableOptimization;
  }

  // --- Functionality -------------------------------------------------------------------------------------------------

  /**
   * Reads a given Javascript from the input reader and writes a compressed version to the output.
   * Will also write errors to the console if the Javascript is invalid (though processing might still work to a certain extend).
   * <p/>
   * Make sure that - if the JS comes com the @link com.coremedia.blueprint.common.contentbeans.CMAbstractCode Class or similar - the Markup has already been removed from the code.
   *
   * @param output the output
   * @param input  the input
   * @param name the name of the minified JavaScript. needed for error messages only.
   */
  @Override
  public void minify(Writer output, Reader input, String name) throws IOException {
    JavaScriptCompressor compressor = new JavaScriptCompressor(input, new ErrorReporterWrapper(name));
    compressor.compress(output, linebreak, munge, verbose, preserveAllSemicolons, disableOptimization);
  }

  //=== Internal =======================================================================================================

  /**
   * A Helper class required by the YUICompressor. Handles errors and writes them to the log file.
   */
  private static class ErrorReporterWrapper implements ErrorReporter {

    private String fileName;

    ErrorReporterWrapper(String fileName) {
      this.fileName = fileName;
    }

    @Override
    public void warning(String message, String sourceName,
                        int line, String lineSource, int lineOffset) {
      LOG.debug("YuiCompressor: FileName: {}, Line: {}, LineOffset: {}, LineSource: '{}': {}", fileName, line, lineOffset, lineSource, message);
    }

    @Override
    public void error(String message, String sourceName,
                      int line, String lineSource, int lineOffset) {
      LOG.debug("YuiCompressor: FileName: {}, Line: {}, LineOffset: {}, LineSource: '{}': {}", fileName, line, lineOffset, lineSource, message);
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName,
                                           int line, String lineSource, int lineOffset) {
      error(message, sourceName, line, lineSource, lineOffset);
      return new EvaluatorException(message);
    }
  }
}

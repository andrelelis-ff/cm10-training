package com.coremedia.blueprint.localization;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Development interceptor for the {@link ContentBundleResolver}.
 * <p>
 * Overrules the requested content bundle with the corresponding source file
 * in the blueprint workspace for faster frontend development roundtrips.
 * <p>
 * Do not use it in production setups!
 */
public class LocalResourcesBundleResolver implements BundleResolver {
  private static final Logger LOG = LoggerFactory.getLogger(LocalResourcesBundleResolver.class);

  private final BundleResolver defaultBundleResolver;
  private final StructService structService;
  private final File blueprintDir;

  public LocalResourcesBundleResolver(BundleResolver defaultBundleResolver, StructService structService, File blueprintDir) {
    this.defaultBundleResolver = defaultBundleResolver;
    this.structService = structService;
    this.blueprintDir = blueprintDir;
  }


  // --- BundleResolver ---------------------------------------------

  /**
   * Try to find a local resource bundle for the given bundle.
   * <p>
   * Pattern: content /Themes/corporate/corporate_de.properties is substituted by local file
   * ${coremedia.blueprint.project.directory}/modules/extensions/corporate/corporate-theme/src/corporate_de.properties
   * <p>
   * Falls back to default bundle resolving if no local bundle is found.
   */
  @Nullable
  @Override
  public Struct resolveBundle(@NonNull Content bundle) {
    File bundleFile = localFileFor(bundle);
    if (bundleFile != null) {
      try {
        return fileToStruct(bundleFile);
      } catch (IOException e) {
        LOG.error("Cannot handle local resource bundle {} for {}", bundleFile.getAbsolutePath(), bundle, e);
      }
    } else {
      LOG.info("No local resource bundle found for {}", bundle);
    }
    return defaultBundleResolver.resolveBundle(bundle);
  }


  // --- internal ---------------------------------------------------

  private File localFileFor(Content bundle) {
    Content parent = bundle.getParent();
    if (parent == null) {
      LOG.warn("Cannot derive a local resource bundle from {}, fallback to regular resolution", bundle.getPath());
      return null;
    }

    List<String> pathArcs = new ArrayList<>();
    pathArcs.addAll(Arrays.asList("modules", "frontend", "target", "resources"));
    pathArcs.addAll(bundle.getPathArcs());
    File fileInThemes = new File(blueprintDir, filepath(pathArcs));

    if (fileInThemes.exists() && !fileInThemes.isDirectory() && fileInThemes.canRead()) {
      return fileInThemes;
    }
    // Logging may be refined.  Info, because non-existence is likely
    // if somebody works with a partial workspace.
    LOG.info("File {} is not suitable as local resource bundle for {}, fallback to regular resolution", bundle.getName(), bundle.getPath());
    return null;
  }

  private Struct fileToStruct(File bundleFile) throws IOException {
    Properties bundleProperties = new Properties();
    try (InputStream is = new FileInputStream(bundleFile)) {
      bundleProperties.load(is);
    }
    StructBuilder structBuilder = structService.createStructBuilder();
    for (Map.Entry<Object, Object> entry : bundleProperties.entrySet()) {
      structBuilder.set(entry.getKey().toString(), entry.getValue());
    }
    return structBuilder.build();
  }

  private static String filepath(List<String> pathArcs) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < pathArcs.size(); ++i) {
      if (i > 0) {
        sb.append(File.separator);
      }
      String pathArc = pathArcs.get(i);
      if(pathArc.equals("Themes")){
        pathArc = pathArc.toLowerCase();
      }
      sb.append(pathArc);
    }
    return sb.toString();
  }
}

package com.coremedia.blueprint.themeimporter.client;

import com.coremedia.blueprint.coderesources.ThemeService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.themeimporter.ThemeImporter;
import com.coremedia.cap.user.User;
import com.coremedia.io.URLLoader;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.coremedia.blueprint.themeimporter.client.ThemeImporterInitializer.THEMEIMPORTER_CLEAN;
import static com.coremedia.blueprint.themeimporter.client.ThemeImporterInitializer.THEMEIMPORTER_DEVELOPMENT_MODE;
import static com.coremedia.blueprint.themeimporter.client.ThemeImporterInitializer.THEMEIMPORTER_EXITCODE;
import static com.coremedia.blueprint.themeimporter.client.ThemeImporterInitializer.THEMEIMPORTER_FOLDER;
import static com.coremedia.blueprint.themeimporter.client.ThemeImporterInitializer.THEMEIMPORTER_THEMES;

public abstract class AbstractThemeImporterClient implements CommandLineRunner, ApplicationContextAware {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractThemeImporterClient.class);

  private CapConnection capConnection;
  private ThemeImporter themeImporter;
  private ThemeService themeService;
  private Environment env;


  // --- Spring -----------------------------------------------------

  public AbstractThemeImporterClient(CapConnection capConnection,
                                     ThemeImporter themeImporter,
                                     ThemeService themeService) {
    this.capConnection = capConnection;
    this.themeImporter = themeImporter;
    this.themeService = themeService;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    env = applicationContext.getEnvironment();
  }

  @Override
  public void run(String... strings) {
    try {
      if (isDevelopmentMode()) {
        LOG.info("Theme importer runs in development mode.");
      }

      work();

      if (getExitCode().get() != 0) {
        LOG.warn("Done, with errors.");
      } else {
        LOG.info("Done.");
      }
    } catch (Exception e) {
      LOG.error("Failed!", e);
      getExitCode().set(20);
    }
  }

  protected static void runSpringApplication(SpringApplication springApplication, AtomicInteger exitCodeCallback) {
    try (ConfigurableApplicationContext applicationContext = springApplication.run()) {
      LOG.debug("Theme importer success");
    } catch (Exception e) {
      LOG.error("Theme importer failed", e);
      exitCodeCallback.set(20);
    }
  }


  // --- properties -------------------------------------------------

  private boolean isDevelopmentMode() {
    return getProperty(THEMEIMPORTER_DEVELOPMENT_MODE, Boolean.class).orElse(false);
  }

  private String getFolder() {
    String originalFolder = env.getProperty(THEMEIMPORTER_FOLDER);

    if (!isDevelopmentMode()) {
      return originalFolder;
    }

    User developer = capConnection.getSession().getUser();
    return themeService.developerPath(originalFolder, developer);
  }

  private boolean cleanBeforeImport() {
    return getProperty(THEMEIMPORTER_CLEAN, Boolean.class).orElse(false);
  }

  private List<String> getThemes() {
    return env.getProperty(THEMEIMPORTER_THEMES, List.class);
  }

  private AtomicInteger getExitCode() {
    return env.getProperty(THEMEIMPORTER_EXITCODE, AtomicInteger.class);
  }


  // --- internal ---------------------------------------------------

  private void work() {
    String targetFolder = getFolder();
    if (targetFolder == null) {
      LOG.error("No target folder, or no corresponding development folder");
      getExitCode().set(5);
      return;
    }

    LOG.info("Import themes to {}", targetFolder);
    Collection<URL> urls = collectThemeUrls(getThemes(), getExitCode());
    Collection<InputStream> streams = openStreams(urls);
    try {
      themeImporter.importThemes(targetFolder, streams, true, cleanBeforeImport());
    } finally {
      closeStreams(streams);
    }
  }

  private Collection<InputStream> openStreams(Collection<URL> urls) {
    List<InputStream> result = new ArrayList<>();
    for (URL url : urls) {
      InputStream inputStream = null;
      try {
        inputStream = url.openStream();
        result.add(inputStream);
      } catch (Exception e) {
        LOG.error("could not open stream on {}, skipping", url, e);
        if (inputStream != null) {
          closeStreams(Set.of(inputStream));
        }
      }
    }
    return result;
  }

  private void closeStreams(Collection<InputStream> streams) {
    for (InputStream stream : streams) {
      try {
        stream.close();
      } catch (Exception e) {
        LOG.warn("Cannot close input stream {}", stream, e);
      }
    }
  }

  private static Collection<URL> collectThemeUrls(List<String> themes, AtomicInteger exitCode) {
    return themes.stream()
            .map(theme -> {
              try {
                URL url = URLLoader.createUrl(theme);
                if (URLLoader.exists(url)) {
                  return url;
                } else {
                  LOG.warn("Cannot read theme {}, skipped.", theme);
                  exitCode.set(100);
                  return null;
                }
              } catch (Exception e) {
                LOG.warn("Cannot read theme {}, skipped.", theme, e);
                exitCode.set(100);
                return null;
              }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  @NonNull
  private <T> Optional<T> getProperty(@NonNull String key, @NonNull Class<T> targetType) {
    T property = env.getProperty(key, targetType);
    return Optional.ofNullable(property);
  }
}

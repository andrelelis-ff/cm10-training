package com.coremedia.blueprint.themeimporter.client;

import com.coremedia.blueprint.coderesources.ThemeService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.themeimporter.ThemeImporter;
import com.coremedia.cmdline.CommandLineClient;
import com.coremedia.cmdline.Credentials;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Imports Themes into the ContentRepository.
 * <p>
 * To import, start the theme-importer application which has the following
 * synopsis:<br>
 * <pre>{@code
 * cm import-themes -u &lt;user&gt; [other options] &lt;theme.zip&gt; ...
 * }</pre>
 * <p/>
 * <b>Options:</b>
 * <table>
 * <tr><td>-f, --folder</td><td>Folder within CoreMedia where themes are stored. Defaults to /Themes</td></tr>
 * <tr><td>-u, --user &lt;user name&gt;</td><td>the name of the CoreMedia user</td></tr>
 * <tr><td>-d, --domain &lt;domain&gt;</td><td>the domain of the user</td></tr>
 * <tr><td>-p, --password &lt;password&gt;</td><td>the users password</td></tr>
 * <tr><td>-url &lt;ior url&gt;</td><td>Content Server IOR URL to connect to</td></tr>
 * </table>
 */
@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class ThemeImporterClient extends AbstractThemeImporterClient {
  private static final ThemeImporterCommandLineParser CMD_LINE_PARSER = new ThemeImporterCommandLineParser();

  public ThemeImporterClient(CapConnection capConnection, ThemeImporter themeImporter, ThemeService themeService) {
    super(capConnection, themeImporter, themeService);
  }

// --- main -------------------------------------------------------

  public static void main(String[] args) {
    AtomicInteger exitCodeCallback = new AtomicInteger(CommandLineClient.ERROR_NONE);
    SpringApplication springApplication = createSpringApplication(args, exitCodeCallback);
    if (springApplication != null) {
      runSpringApplication(springApplication, exitCodeCallback);
    }
    System.exit(exitCodeCallback.get());
  }

  private static SpringApplication createSpringApplication(String[] args, AtomicInteger exitCodeCallback) {
    int commandLineExitCode = CMD_LINE_PARSER.parseCommandLine(args);
    if (commandLineExitCode != CommandLineClient.ERROR_NONE) {
      exitCodeCallback.set(commandLineExitCode);
      return null;
    }
    Credentials cred = CMD_LINE_PARSER.getCredentials();
    LoginInitializer loginInitializer = new LoginInitializer(cred.getIorUrl(), cred.getUser(), cred.getDomain(), cred.getPassword());
    ThemeImporterInitializer themeImporterInitializer = new ThemeImporterInitializer(
            CMD_LINE_PARSER.folder,
            CMD_LINE_PARSER.themes,
            CMD_LINE_PARSER.clean,
            CMD_LINE_PARSER.developmentMode,
            exitCodeCallback);
    SpringApplication springApplication = new SpringApplication(ThemeImporterClient.class);
    springApplication.addInitializers(loginInitializer, themeImporterInitializer);
    springApplication.setBannerMode(Banner.Mode.OFF);
    return springApplication;
  }
}

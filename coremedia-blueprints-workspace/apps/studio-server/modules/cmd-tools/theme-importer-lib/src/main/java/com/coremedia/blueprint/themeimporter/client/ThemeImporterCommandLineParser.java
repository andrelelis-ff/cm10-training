package com.coremedia.blueprint.themeimporter.client;

import com.coremedia.cmdline.CommandLineClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import edu.umd.cs.findbugs.annotations.NonNull;

class ThemeImporterCommandLineParser extends CommandLineClient {
  private static final String FOLDER_PARAMETER = "f";
  private static final String CLEAN_PARAMETER = "c";
  private static final String DEVELOPMENT_MODE_PARAMETER = "dm";

  String folder = ThemeImporterInitializer.REPOSITORY_FOLDER;
  boolean clean = false;
  boolean developmentMode = false;
  String[] themes;

  @SuppressWarnings({
        /* squid:S2209/AccessStaticViaInstance:
         * static vs. instance: Unless updating commons-cli there is no other way to use the OptionBuilder.
         */
          "AccessStaticViaInstance",
          "squid:S2209"
  })
  @Override
  protected void fillInOptions(Options options) {
    options.addOption(OptionBuilder.hasArg().withDescription("Folder within CoreMedia where themes are stored. Default is /Themes")
            .withLongOpt("folder")
            .create(FOLDER_PARAMETER));
    options.addOption(OptionBuilder.withDescription("Delete existing theme before import in order to get rid of obsolete code resources.")
            .withLongOpt("clean")
            .create(CLEAN_PARAMETER));
    options.addOption(OptionBuilder.withDescription("Development mode.  Creates a user (frontend developer) specific copy of the theme.")
            .withLongOpt("development-mode")
            .create(DEVELOPMENT_MODE_PARAMETER));
  }

  @Override
  @NonNull
  protected String getUsage() {
    return "cm import-themes -u <user> [other options] [-f <folder>] [-c] [-dm] <theme.zip> ...";
  }

  @Override
  protected boolean parseCommandLine(CommandLine commandLine) {
    if (commandLine.hasOption(FOLDER_PARAMETER)) {
      folder = commandLine.getOptionValue(FOLDER_PARAMETER);
    }
    clean = commandLine.hasOption(CLEAN_PARAMETER);
    developmentMode = commandLine.hasOption(DEVELOPMENT_MODE_PARAMETER);
    themes = commandLine.getArgs();
    if (themes == null || themes.length == 0) {
      getLogger().trace("Wrong argument for parameter t. Command line parsing marked as failure.");
      return false;
    }
    return true;
  }

  @Override
  protected boolean understandsVerbose() {
    return false;
  }
}

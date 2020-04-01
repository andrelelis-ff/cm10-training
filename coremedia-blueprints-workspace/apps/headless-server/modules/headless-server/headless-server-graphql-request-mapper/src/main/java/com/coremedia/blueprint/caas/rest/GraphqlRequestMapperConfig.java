package com.coremedia.blueprint.caas.rest;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix="caas-rest")
@DefaultAnnotation(NonNull.class)
@PropertySource("classpath:caas-rest.properties")
public class GraphqlRequestMapperConfig implements WebMvcConfigurer {
  private static final Logger LOG = LoggerFactory.getLogger(GraphqlRequestMapperConfig.class);

  // enabled by default
  private boolean jsltEnabled = true;

  public boolean isJsltEnabled() {
    return jsltEnabled;
  }

  public void setJsltEnabled(boolean jsltEnabled) {
    this.jsltEnabled = jsltEnabled;
  }

  @Bean
  public GraphqlApiListingScannerPlugin getScannerPlugin(@Qualifier("requestMappingMap") Map<UriTemplate, String> requestMappingMap,
                                                         @Qualifier("persistedQueries") Map<String,String> persistedQueries) {
    return new GraphqlApiListingScannerPlugin(requestMappingMap, persistedQueries);
  }

  @Bean
  public Map<UriTemplate, String> requestMappingMap(@Value("${caas-rest.query-mapping-pattern:classpath:graphql/rest-mapping/*.properties}") String queryMappingPattern) {

    Map<UriTemplate, String> requestMapping = new HashMap<>();
    try {

      var loader = new PathMatchingResourcePatternResolver();
      for(var resource : loader.getResources(queryMappingPattern)) {

        var fileName = resource.getFilename();
        try (var in = new LineNumberReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
          LOG.info("Loading query mappings from {}", resource.getFilename());
          String line;
          while ((line = in.readLine()) != null) {
            Map.Entry<UriTemplate, String> entry = stringToMapEntry(line);
            if (entry != null) {
              requestMapping.put(entry.getKey(), entry.getValue());
              LOG.info("Added rest mapping from {} to persisted query {}", entry.getKey(), entry.getValue());
            }
          }
        } catch (IOException e) {
          LOG.error("Error reading resource {}", resource.getFilename(), e);
        }

      }

    } catch (IOException e) {
      LOG.error("Error loading resource", e);
    }
    return requestMapping;

  }

  /**
   * Read properties from an input {@link String} and puts them in a {@link Map.Entry}.
   * IMPORTANT: The first value gets interpreted as a {@link String} and the value in the {@link Map.Entry}
   * and the second values as a {@link UriTemplate} and the key of the {@link Map.Entry}.
   *
   * @param inputLine A String containing two expressions, separated by a "=".
   * @return A {@link Map.Entry} containing the values from inputLine
   */
  @Nullable
  private static Map.Entry<UriTemplate, String> stringToMapEntry(String inputLine) {
    String[] splittedStrings = inputLine.split("=");
    String key;
    String value;
    if (splittedStrings.length == 2) {
      key = splittedStrings[1].trim();
      if (!key.startsWith("/")) {
        key = "/" + key;
        LOG.warn("Line does not start with a leading /, adding / automatically");
      }
      value = splittedStrings[0].trim();
      return Map.entry(new UriTemplate(key), value);
    }
    LOG.error("Ignoring malformed line: {}", inputLine);
    return null;
  }
}

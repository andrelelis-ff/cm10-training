package com.coremedia.blueprint.caas.rest;

import com.coremedia.caas.web.persistedqueries.PersistedQueryExecutor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.util.UriTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping(GraphqlRequestMapper.CONTROLLER_PREFIX)
@ApiIgnore
@DefaultAnnotation(NonNull.class)
public class GraphqlRequestMapper {
  public static final String CONTROLLER_PREFIX = "/caas/v1";
  private static final Logger LOG = LoggerFactory.getLogger(GraphqlRequestMapper.class);
  private static Expression DEFAULT_EXPRESSION;
  private static boolean DEFAULT_EXPRESSION_EXISTS = false;

  @Value("classpath:transformations/*.jslt")
  private Resource[] resources;

  static {
    try {
      DEFAULT_EXPRESSION = Parser.compileResource(transformationPath("default"));
      DEFAULT_EXPRESSION_EXISTS = true;
    } catch (Exception e) {
      LOG.info("No default transformation template found. ({})", e.getMessage());
      DEFAULT_EXPRESSION_EXISTS = false;
    }
  }

  private final boolean jsltEnabled;
  private final PersistedQueryExecutor persistedQueryExecutor;
  private final Map<UriTemplate, String> requestMappingMap;
  private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();


  @SuppressWarnings("findbugs:NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
  public GraphqlRequestMapper(PersistedQueryExecutor persistedQueryExecutor, Map<UriTemplate, String> requestMappingMap, GraphqlRequestMapperConfig graphqlRequestMapperConfig) {
    this.persistedQueryExecutor = persistedQueryExecutor;
    this.requestMappingMap = requestMappingMap;
    this.jsltEnabled = graphqlRequestMapperConfig.isJsltEnabled();
    LOG.info("JSLT transformation {}", jsltEnabled ? "enabled" : "disabled");
  }

  @GetMapping("/**")
  @Timed
  public Object handleGetRequest(ServletWebRequest servletWebRequest) {

    String requestPath = getRequestPath(servletWebRequest);
    Optional<Map.Entry<UriTemplate, String>> first = requestMappingMap.entrySet().stream()
            .filter(entry -> entry.getKey().matches(requestPath))
            .findFirst();
    if (first.isEmpty()) {
      LOG.debug("Endpoint {} not found", requestPath);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("The Endpoint %s was not found", requestPath));
    }

    Map.Entry<UriTemplate, String> uriTemplateEntry = first.get();
    Map<String, Object> requestVariables = new HashMap<>(uriTemplateEntry.getKey().match(requestPath));
    requestVariables.putAll(retrieveQueryParameters(servletWebRequest));
    Object graphqlResult = persistedQueryExecutor.execute(uriTemplateEntry.getValue(), requestVariables, servletWebRequest);

    return ((CompletableFuture) graphqlResult).thenApplyAsync(o -> transformResponse(uriTemplateEntry.getValue(), o));

  }

  private Object transformResponse(String queryId, Object graphqlResponse) {

    Object transformedResponse = graphqlResponse;
    ObjectNode jsonResponse = new ObjectMapper().valueToTree(graphqlResponse);
    boolean containsGraphQlErrors = jsonResponse.get("errors") != null;
    if (jsltEnabled) {
      Expression jslt = expressionCache.computeIfAbsent(queryId, s -> getExpression(queryId));
      if (jslt != null) {
        transformedResponse = jslt.apply(jsonResponse);
        if (containsGraphQlErrors) {
          transformedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(transformedResponse);
        } else if (isSecondLevelEmpty(jsonResponse)) {
          return ResponseEntity.status(404).build();
        }
      }
    }
    return transformedResponse;

  }

  private static boolean isSecondLevelEmpty(ObjectNode objectNode) {
    Iterator<Map.Entry<String, JsonNode>> dataIterator = objectNode.get("data").fields();
    while (dataIterator.hasNext()) {
      JsonNode firstLevel = dataIterator.next().getValue();
      if (firstLevel.isNull()) {
        return true;
      } else {
        Iterator<Map.Entry<String, JsonNode>> secondLevelFields = firstLevel.fields();
        while (secondLevelFields.hasNext()) {
          Map.Entry<String, JsonNode> secondLevel = secondLevelFields.next();
          if (secondLevel.getValue().isNull()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Nullable
  private Expression getExpression(String name) {
    Expression expression = null;
    if (existsValueInResources(name)) {
      expression = Parser.compileResource(transformationPath(name));
    } else if (DEFAULT_EXPRESSION_EXISTS) {
      expression = DEFAULT_EXPRESSION;
    }
    return expression;
  }

  private boolean existsValueInResources(String name) {
    return Arrays.stream(resources)
            .filter(Resource::exists)
            .anyMatch(resource ->
                    (name + ".jslt").equals(resource.getFilename()));
  }

  private static String transformationPath(String name) {
    return "transformations/" + name + ".jslt";
  }

  private static Map<String, Object> retrieveQueryParameters(ServletWebRequest servletWebRequest) {
    return servletWebRequest.getParameterMap().entrySet().stream()
            .map(entry -> Map.entry(entry.getKey(), (entry.getValue().length == 1) ? entry.getValue()[0] : entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static String getRequestPath(ServletWebRequest servletWebRequest) {
    return servletWebRequest.getRequest().getServletPath().replace(CONTROLLER_PREFIX, "");
  }
}

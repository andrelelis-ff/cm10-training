package com.coremedia.blueprint.caas.rest;

import com.fasterxml.classmate.TypeResolver;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriTemplate;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingScannerPlugin;
import springfox.documentation.spi.service.contexts.DocumentationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@DefaultAnnotation(NonNull.class)
public class GraphqlApiListingScannerPlugin implements ApiListingScannerPlugin {

  @SuppressWarnings("squid:S4784")
  private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\s*\\$(\\p{Alnum}+\\s*):(\\s*\\[?\\s*\\p{Alpha}+\\!?\\]?\\!?)");
  private Map<UriTemplate, String> mappings;
  private Map<String, String> persistedQueries;

  public GraphqlApiListingScannerPlugin(Map<UriTemplate, String> mappings, Map<String, String> persistedQueries) {
    this.mappings = mappings;
    this.persistedQueries = persistedQueries;
  }

  @Override
  public List<ApiDescription> apply(DocumentationContext documentationContext) {
    return mappings.entrySet().stream()
            .map(entry -> {
              return new ApiDescription(
                      "caas",
                      GraphqlRequestMapper.CONTROLLER_PREFIX + entry.getKey().toString(),
                      "CoreMedia CaaS API Description",
                      Collections.singletonList(new OperationBuilder(s -> entry.getKey().toString())
                              .tags(Collections.singleton("REST Mappings for GraphQL"))
                              .method(HttpMethod.GET)
                              .notes(getDescription(entry))
                              .parameters(generateParameters(entry))
                              .responseMessages(responseMessages())
                              .build()),
                      false);
            })
            .collect(Collectors.toList());
  }

  @Nullable
  private String getDescription(Map.Entry<UriTemplate, String> entry) {
    String query = persistedQueries.get(entry.getValue());
    if ( query == null) {
      return null;
    }
    return query.replaceAll("[ ]", "&nbsp;");
  }

  private static Map<String, String> parseParameters(String query) {
    Matcher matcher = PARAMETER_PATTERN.matcher(query);
    Map<String, String> graphqlParametersMap = new HashMap<>();
    while (matcher.find()) {
      graphqlParametersMap.put(matcher.group(1), matcher.group(2));
    }
    return graphqlParametersMap;
  }

  private List<Parameter> generateParameters(Map.Entry<UriTemplate, String> entry) {
    List<Parameter> parameterList = new ArrayList<>();
    String graphqlQuery = persistedQueries.get(entry.getValue());
    if (graphqlQuery != null) {
      List<String> uriTemplateVariableNames = entry.getKey().getVariableNames();
      parameterList.addAll(uriTemplateVariableNames.stream()
              .map((String paramName) -> getParam(paramName, "path", true))
              .collect(Collectors.toList()));
      parameterList.addAll(parseParameters(graphqlQuery).entrySet().stream()
              .filter(graphqlEntry -> !uriTemplateVariableNames.contains(graphqlEntry.getKey()))
              .map(graphqlEntry -> getParam(graphqlEntry.getKey(), "query", isParameterRequired(graphqlEntry)))
              .collect(Collectors.toList()));
    }
    return parameterList;
  }

  private static boolean isParameterRequired(Map.Entry<String, String> graphqlEntry) {
    String graphqlType = graphqlEntry.getValue();
    boolean isParameterRequired = false;
    if ( (!graphqlType.contains("[") && graphqlType.contains("!")) || graphqlType.contains("]!")) {
      isParameterRequired = true;
    }
    return isParameterRequired;
  }

  private static Parameter getParam(String paramName, String parameterType, boolean required) {
    return new ParameterBuilder()
            .type(new TypeResolver().resolve(String.class))
            .name(paramName)
            .parameterType(parameterType)
            .parameterAccess("access")
            .required(required)
            .modelRef(new ModelRef("string"))
            .build();
  }

  private static Set<ResponseMessage> responseMessages() {
    return Set.of(generateResponseMessage(200, "Successfully received GraphQL response"),
            generateResponseMessage(400, "Bad Request"),
            generateResponseMessage(404, "Not found: Missing mapping or wrong url"),
            generateResponseMessage(500, "Internal Server Error")
    );
  }

  private static ResponseMessage generateResponseMessage(int code, String message) {
    return new ResponseMessageBuilder()
            .code(code)
            .message(message)
            .responseModel(new ModelRef("string"))
            .build();
  }

  @Override
  public boolean supports(DocumentationType documentationType) {
    return DocumentationType.SWAGGER_2.equals(documentationType);
  }
}

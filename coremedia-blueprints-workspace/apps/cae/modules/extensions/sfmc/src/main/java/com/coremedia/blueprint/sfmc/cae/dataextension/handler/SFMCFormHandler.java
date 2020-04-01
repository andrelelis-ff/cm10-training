package com.coremedia.blueprint.sfmc.cae.dataextension.handler;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.base.sfmc.contentlib.context.SFMCContextProvider;
import com.coremedia.blueprint.base.sfmc.libservices.context.SFMCContext;
import com.coremedia.blueprint.base.sfmc.libservices.dataextensions.DataExtensionEntry;
import com.coremedia.blueprint.base.sfmc.libservices.dataextensions.DataExtensionValueType;
import com.coremedia.blueprint.base.sfmc.libservices.dataextensions.SFMCDataExtensionService;
import com.coremedia.blueprint.base.sfmc.libservices.dataextensions.ValidationException;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.sfmc.cae.dataextension.DataExtensionEntryImpl;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.web.HandlerHelper;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping
public class SFMCFormHandler {

  private static final Logger LOG = LoggerFactory.getLogger(SFMCFormHandler.class);

  private static final String PREFIX_FORM_CONFIG = "formconfig";
  private static final String PREFIX_FORM_DATA = "formdata";
  private static final String FIELD_TYPE = "fieldType";
  private static final String PRIMARY_KEY = "primaryKey";
  private static final String PREFIX_FORM_FIELD_TYPES_CONFIG = PREFIX_FORM_CONFIG + "." + FIELD_TYPE;
  private static final String PREFIX_FORM_PRIMARY_KEY_CONFIG = PREFIX_FORM_CONFIG + "." + PRIMARY_KEY;
  private static final String FORM_NAME = PREFIX_FORM_CONFIG + ".form-name";
  private static final DataExtensionValueType DEFAULT_FIELD_TYPE = DataExtensionValueType.TEXT;


  private ContentBeanFactory contentBeanFactory;
  private SFMCContextProvider contextProvider;
  private SFMCDataExtensionService dataExtensionService;


  public SFMCFormHandler(@NonNull SFMCContextProvider contextProvider,
                         @NonNull SFMCDataExtensionService dataExtensionService,
                         @NonNull ContentBeanFactory contentBeanFactory) {
    this.contextProvider = contextProvider;
    this.dataExtensionService = dataExtensionService;
    this.contentBeanFactory = contentBeanFactory;
  }

  @PostMapping("/services/sfmc/dataextension/{segment}")
  public ModelAndView formPost(@RequestParam Map<String, String> data,
                               @PathVariable("segment") String segment,
                               HttpServletRequest request) {
    Optional<Site> site = SiteHelper.findSite(request);

    if (site.isEmpty()) {
      return HandlerHelper.badRequest("No site found for path segment " + segment);
    }

    Optional<SFMCContext> context = contextProvider.getContext(site.get());
    if (context.isEmpty()) {
      throw new RuntimeException("Can not resolve a context. Can't persist the given data");
    }

    Optional<String> formName = extractFormName(data);
    if (formName.isEmpty()) {
      return HandlerHelper.badRequest("Mandatory field " + FORM_NAME + " not found in the request.");
    }

    List<DataExtensionEntry> dataExtensionEntries = toDataExtensionEntries(data);

    boolean successful = pushToDataExtensionService(context.get(), formName.get(), dataExtensionEntries);

    if (successful) {
      Content siteRootDocument = site.get().getSiteRootDocument();
      CMChannel contentBean = contentBeanFactory.createBeanFor(siteRootDocument, CMChannel.class);
      return HandlerHelper.redirectTo(contentBean, null, HttpStatus.SEE_OTHER);
    }

    return HandlerHelper.badRequest();
  }

  private boolean pushToDataExtensionService(@NonNull SFMCContext context,
                                             @NonNull String formName,
                                             @NonNull List<DataExtensionEntry> dataExtensionEntries) {
    try {
      return dataExtensionService.pushToDataExtension(context, formName, dataExtensionEntries);
    } catch (ValidationException e) {
      LOG.warn("A technical error ocurred while pushing to Salesforce Marketing Cloud Data Extensions", e);
      return false;
    }
  }

  @NonNull
  private List<DataExtensionEntry> toDataExtensionEntries(@NonNull Map<String, String> data) {
    List<String> fieldNames = extractFieldNames(data);
    return fieldNames.stream()
                     .map(s -> toDataExtensionEntry(s, data))
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .collect(Collectors.toList());
  }

  @NonNull
  private Optional<DataExtensionEntry> toDataExtensionEntry(@NonNull String fieldName,
                                                            @NonNull Map<String, String> data) {
    String valueKey = PREFIX_FORM_DATA + "." + fieldName;
    if (!data.containsKey(valueKey)) {
      return Optional.empty();
    }

    String value = data.get(valueKey);

    DataExtensionValueType fieldType = getFieldType(fieldName, data);
    boolean isPrimary = evaluatePrimaryKey(fieldName, data);

    return Optional.of(new DataExtensionEntryImpl(fieldName, fieldType, value, isPrimary));
  }

  private boolean evaluatePrimaryKey(@NonNull String fieldName, @NonNull Map<String, String> data) {
    String primaryKey = PREFIX_FORM_PRIMARY_KEY_CONFIG + "." + fieldName;
    if (!data.containsKey(primaryKey)) {
      return false;
    }
    String primaryKeyFlag = data.get(primaryKey);
    return Boolean.valueOf(primaryKeyFlag);
  }

  @NonNull
  private DataExtensionValueType getFieldType(@NonNull String fieldName, @NonNull Map<String, String> data) {
    String fieldType = data.get(PREFIX_FORM_FIELD_TYPES_CONFIG + "." + fieldName);
    if (fieldType == null) {
      return DEFAULT_FIELD_TYPE;
    }

    return Arrays.stream(DataExtensionValueType.values())
                 .filter(dataExtensionValueType -> dataExtensionValueType.name().equalsIgnoreCase(fieldType))
                 .findFirst()
                 .orElse(DEFAULT_FIELD_TYPE);
  }

  @NonNull
  private List<String> extractFieldNames(@NonNull Map<String, String> data) {
    return data.keySet()
               .stream()
               .filter(s -> s.contains(PREFIX_FORM_DATA + "."))
               .map(s -> s.replaceFirst(PREFIX_FORM_DATA + ".", ""))
               .collect(Collectors.toList());
  }
  @NonNull
  private Optional<String> extractFormName(@NonNull Map<String, String> data) {
    return Optional.ofNullable(data.get(FORM_NAME));
  }
}

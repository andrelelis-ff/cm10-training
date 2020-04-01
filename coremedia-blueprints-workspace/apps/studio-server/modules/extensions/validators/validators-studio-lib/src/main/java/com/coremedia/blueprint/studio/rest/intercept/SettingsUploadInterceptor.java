package com.coremedia.blueprint.studio.rest.intercept;

import com.coremedia.blueprint.themeimporter.SettingsJsonToMapAdapter;
import com.coremedia.blueprint.themeimporter.MapToStructAdapter;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.rest.cap.intercept.InterceptorControlAttributes;
import com.coremedia.rest.cap.intercept.UploadedBlob;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * If the file to be uploaded is a settings JSON file map it to a settings document.
 */
public class SettingsUploadInterceptor extends ContentWriteInterceptorBase {

  private static final Pattern SETTINGS_JSON = Pattern.compile("(.+).settings$");

  private final String blobPropertyName;
  private final String settingsDocumentTypeName;
  private final String settingsStructPropertyName;
  private final SettingsJsonToMapAdapter settingsJsonToMapAdapter;
  private final MapToStructAdapter mapToStructAdapter;

  public SettingsUploadInterceptor(String blobPropertyName,
                                   String settingsDocumentTypeName,
                                   String settingsStructPropertyName,
                                   SettingsJsonToMapAdapter settingsJsonToMapAdapter,
                                   MapToStructAdapter mapToStructAdapter) {
    super();
    this.blobPropertyName = blobPropertyName;
    this.settingsDocumentTypeName = settingsDocumentTypeName;
    this.settingsStructPropertyName = settingsStructPropertyName;
    this.settingsJsonToMapAdapter = settingsJsonToMapAdapter;
    this.mapToStructAdapter = mapToStructAdapter;
  }

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    if (properties != null && properties.containsKey(blobPropertyName)) {
      Blob blob = (Blob) properties.get(blobPropertyName);

      if (blob instanceof UploadedBlob) {
        Matcher matcher = SETTINGS_JSON.matcher(request.getName());
        if (matcher.matches()) {
          request.setAttribute(InterceptorControlAttributes.DO_NOTHING, true);

          String documentName = matcher.group(1);

          Content parentFolder = request.getParent();
          ContentRepository repository = parentFolder.getRepository();

          String settingsJson;
          try {
            settingsJson = IOUtils.toString(blob.getInputStream(), StandardCharsets.UTF_8);
          } catch (IOException e) {
            throw new IllegalStateException("Error reading blob input stream", e);
          }
          Map<String, Object> json = settingsJsonToMapAdapter.getMap(settingsJson, parentFolder.getPath());
          Struct struct = mapToStructAdapter.getStruct(json);

          ContentType type = repository.getContentType(settingsDocumentTypeName);
          if (type == null) {
            throw new IllegalStateException(String.format("Cannot create settings document since there is no content type %s", settingsDocumentTypeName));
          }

          Content content = type.createByTemplate(parentFolder, documentName, "{3} ({1})", ImmutableMap.of(settingsStructPropertyName, struct));
          // Do not check in content as otherwise the problem described in CMS-14075 will apply.
          // Knowing how the client is implemented, this will still cause the content to be checked in.

          request.setAttribute(InterceptorControlAttributes.UPLOADED_DOCUMENTS, ImmutableList.of(content));
        }
      }
    }
  }

}

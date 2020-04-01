package com.coremedia.blueprint.studio.rest.intercept;

import com.coremedia.blueprint.studio.rest.intercept.word.DocContentHandler;
import com.coremedia.blueprint.studio.rest.intercept.word.DocTitleHandler;
import com.coremedia.blueprint.studio.rest.intercept.word.DocumentEntry;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.BlobService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.xml.Markup;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.apache.tika.sax.XHTMLContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This is a prototype implementation of a MS Word to CMArticle converter, implemented as
 * ContentWriteInterceptor to be executed during bulk uploads.
 *
 * Be aware that this kind of conversion will never be a 100 percent accurate: it's most likely
 * that the original Word document may have some special characters or formats that are not properly
 * converted to CoreMedia richtext.
 *
 * If you want to tweak these conversions in detail, you have to modify the DocContentHandler class for this.
 */
public class WordUploadInterceptor extends ContentWriteInterceptorBase {
  private static final Logger LOG = LoggerFactory.getLogger(WordUploadInterceptor.class);

  private static final String DOC_MIMETYPE = "application/msword";
  private static final String DOCX_MIMETYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
  private static final String OFFICE_OPEN_XML_MIMETYPE = "application/x-tika-ooxml";
  private static final List<String> INVALID_IMAGE_MIME_TYPES = Arrays.asList("bmp", "image/x-emf");

  private static final String PICTURE_KEY = "_picture";
  private static final String DATA_PROPERTY = "data";

  //content type properties
  private static final String PICTURE_CONTENT_TYPE = "CMPicture";
  private static final String PICTURE_BLOB_PROPETY_NAME = "data";

  private static final String TIKA_CONFIG =
          "<properties><service-loader initializableProblemHandler=\"ignore\"/></properties>";

  private final TikaConfig tikaConfig;
  private ContentRepository repository;

  public WordUploadInterceptor(ContentRepository repository) {
    super();
    this.repository = repository;
    TikaConfig config;
    try (InputStream is = new ByteArrayInputStream(TIKA_CONFIG.getBytes(UTF_8))) {
      config = new TikaConfig(is);
    } catch (SAXException | IOException | TikaException e) {
      LOG.warn("Error creating TikaConfig from: " + TIKA_CONFIG, e);
      config = TikaConfig.getDefaultConfig();
    }
    this.tikaConfig = config;
  }

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    if (properties.containsKey(DATA_PROPERTY)) {
      Object value = properties.get(DATA_PROPERTY);
      properties.remove(DATA_PROPERTY);   // remove data property since articles dont have it. data is only needed to transport blob to the interceptor
      if (value instanceof Blob) {
        Blob blob = (Blob) value;
        try {
          List<DocumentEntry> documentEntries = extract(blob.getInputStream(), blob.getContentType(), request.getName());
          properties.put("title", generateStringProperty("title", documentEntries));
          properties.put("detailText", generateMarkupProperty("detailText", documentEntries));
          properties.put("pictures", generatePicturesProperty(request.getParent(), documentEntries, request.getName()));
        } catch (Throwable e) {
          LOG.error("Error while extracting word file", e);
        }
      }
    }
  }

  protected List<Content> generatePicturesProperty(Content parent, List<DocumentEntry> documentEntries, String imageName) {
    List<Content> result = new ArrayList<>();
    for (DocumentEntry d : documentEntries) {
      if (d.getKey().equals(PICTURE_KEY)) {
        ContentType contentType = repository.getContentType(PICTURE_CONTENT_TYPE);
        Map<String, Object> properties = new HashMap<>();
        properties.put(PICTURE_BLOB_PROPETY_NAME, d.getValue());
        String uniqueFileName = resolveUniqueFilename(parent, imageName, d);
        Content picture = contentType.createByTemplate(parent, uniqueFileName, "{3} ({1})", properties);
        picture.checkIn();
        result.add(picture);
      }
    }

    return result;
  }

  protected String generateStringProperty(String propertyName, List<DocumentEntry> documentEntries) {
    String result = "";
    for (DocumentEntry d : documentEntries) {
      if (d.getKey().equals(propertyName) && d.getValue() instanceof String) {
        result = (String) d.getValue();
        break;
      }
    }
    return result;
  }

  protected Markup generateMarkupProperty(String propertyName, List<DocumentEntry> documentEntries) {
    Markup result = null;
    for (DocumentEntry d : documentEntries) {
      if (d.getKey().equals(propertyName) && d.getValue() instanceof Markup) {
        result = (Markup) d.getValue();
        break;
      }
    }
    return result;
  }

  @VisibleForTesting
  List<DocumentEntry> extract(InputStream in, MimeType contentType, String defaultTitle) throws IOException, MimeTypeParseException, TikaException, SAXException {
    byte[] bytes = IOUtils.toByteArray(in);
    ArrayList<DocumentEntry> result = new ArrayList<>();

    // extract paragraphs
    DocContentHandler wordextractor = new DocContentHandler();
    extractFromContentHandler(bytes, wordextractor);
    result.add(wordextractor.getDocumentEntry());


    // extract title
    DocTitleHandler titleExtractor = new DocTitleHandler(defaultTitle);
    extractFromContentHandler(bytes, titleExtractor);
    result.add(titleExtractor.getDocumentEntry());

    //extract pictures
    if (contentType.getBaseType().equals(DOC_MIMETYPE)) {
      extractDocImages(bytes, result);
    }
    else if (contentType.getBaseType().equals(DOCX_MIMETYPE) || contentType.getBaseType().equals(OFFICE_OPEN_XML_MIMETYPE)) {
      extractDocxImages(bytes, result);

    }
    return result;
  }

  private void extractFromContentHandler(byte[] bytes, ContentHandlerDecorator resolver) throws IOException, SAXException, TikaException {
    org.apache.tika.parser.Parser parser = new AutoDetectParser(tikaConfig);
    Metadata metadata = new Metadata();
    XHTMLContentHandler handler = new XHTMLContentHandler(resolver, metadata);
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    ParseContext context = new ParseContext();
    parser.parse(byteArrayInputStream, handler, metadata, context);
  }


  private void extractDocxImages(byte[] bytes, ArrayList<DocumentEntry> result) {
    try {
      ByteArrayInputStream byteArrayInputStream;
      byteArrayInputStream = new ByteArrayInputStream(bytes);
      XWPFDocument doc = new XWPFDocument(byteArrayInputStream);
      List<XWPFPictureData> allPictures = doc.getAllPictures();
      BlobService blobService = repository.getConnection().getBlobService();
      for (XWPFPictureData p : allPictures) {
        String mimeType = p.getPackagePart().getContentType();
        if (isValidMimeType(mimeType)) {
          byte[] rawContent = p.getData();
          Blob blob = blobService.fromBytes(rawContent, mimeType);
          result.add(new DocumentEntry(PICTURE_KEY, blob, p.getFileName()));
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to extract images: " + e.getMessage());
    }
  }

  private void extractDocImages(byte[] bytes, ArrayList<DocumentEntry> result) {
    try {
      ByteArrayInputStream byteArrayInputStream;
      byteArrayInputStream = new ByteArrayInputStream(bytes);
      HWPFDocument doc = new HWPFDocument(byteArrayInputStream);
      List<Picture> allPictures = doc.getPicturesTable().getAllPictures();
      BlobService blobService = repository.getConnection().getBlobService();
      for (Picture p : allPictures) {
        String mimeType = p.getMimeType();
        if (isValidMimeType(mimeType)) {
          byte[] rawContent = p.getRawContent();
          Blob blob = blobService.fromBytes(rawContent, mimeType);

          String name = null;
          try {
            name = p.getDescription();
          }
          catch(Exception e ) {
            //ignore
          }
          result.add(new DocumentEntry(PICTURE_KEY, blob, name));
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to extract images: " + e.getMessage());
    }
  }

  /**
   * Not all image can be processed by the Studio, so filter them
   *
   * @param mimeType the mime type of the image
   * @return true if the image should be imported
   */
  private boolean isValidMimeType(String mimeType) {
    return !INVALID_IMAGE_MIME_TYPES.contains(mimeType);
  }


  /**
   * Ensures that the file with the given name not exists yet.
   *
   * @param folder    The folder to create the unique name for.
   * @param imageName The default file name
   * @return The unique file name for the given folder.
   */
  private String resolveUniqueFilename(Content folder, String imageName, DocumentEntry entry) {
    String uniqueFilename = entry.getName();
    if (StringUtils.isEmpty(uniqueFilename)) {
      uniqueFilename = imageName;
    }
    else {
      uniqueFilename = imageName + " - " + entry.getName();
    }
    int index = 1;
    while (folder.getChild(uniqueFilename) != null) {
      uniqueFilename = uniqueFilename + " (" + index + ")";
      index++;
    }
    return uniqueFilename;
  }

}

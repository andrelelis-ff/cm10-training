package com.coremedia.blueprint.studio.rest.validation;

import com.coremedia.blueprint.base.links.UrlPathFormattingHelper;
import com.coremedia.blueprint.base.navigation.context.ContextStrategy;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.base.pagegrid.rest.ConfigurableDeadLinkValidator;
import com.coremedia.blueprint.base.pagegrid.rest.PlacementsValidator;
import com.coremedia.blueprint.base.pagegrid.rest.VisibilityValidator;
import com.coremedia.blueprint.base.rest.validators.AbstractCodeValidator;
import com.coremedia.blueprint.base.rest.validators.ArchiveValidator;
import com.coremedia.blueprint.base.rest.validators.AtLeastOneNotEmptyValidator;
import com.coremedia.blueprint.base.rest.validators.ChannelIsPartOfNavigationValidator;
import com.coremedia.blueprint.base.rest.validators.ChannelNavigationValidator;
import com.coremedia.blueprint.base.rest.validators.ChannelReferrerValidator;
import com.coremedia.blueprint.base.rest.validators.ChannelSegmentValidator;
import com.coremedia.blueprint.base.rest.validators.IsPartOfNavigationValidator;
import com.coremedia.blueprint.base.rest.validators.NotEmptyMarkupValidator;
import com.coremedia.blueprint.base.rest.validators.RootChannelSegmentValidator;
import com.coremedia.blueprint.base.rest.validators.SelfReferringLinkListValidator;
import com.coremedia.blueprint.base.rest.validators.TimelineValidator;
import com.coremedia.blueprint.base.rest.validators.ValidityValidator;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SiteModel;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.transform.TransformImageService;
import com.coremedia.image.ImageDimensionsExtractor;
import com.coremedia.rest.cap.CapRestServiceConfiguration;
import com.coremedia.rest.cap.config.StudioConfigurationProperties;
import com.coremedia.rest.cap.content.imagevariants.ImageCropSizeValidator;
import com.coremedia.rest.cap.validation.ContentTypeValidator;
import com.coremedia.rest.cap.validators.ContentLocaleMatchesSiteLocaleValidator;
import com.coremedia.rest.cap.validators.CrossSiteLinkValidator;
import com.coremedia.rest.cap.validators.DuplicateDerivedInSiteValidator;
import com.coremedia.rest.cap.validators.ImageMapAreasValidator;
import com.coremedia.rest.cap.validators.ImageMapOverlayConfigurationValidator;
import com.coremedia.rest.cap.validators.LinkListMaxLengthValidator;
import com.coremedia.rest.cap.validators.MasterVersionUpdatedValidator;
import com.coremedia.rest.cap.validators.SameMasterLinkValidator;
import com.coremedia.rest.cap.validators.SelfReferringStructLinkListValidator;
import com.coremedia.rest.cap.validators.SiteNameValidator;
import com.coremedia.rest.cap.validators.StructLinkListIndexValidator;
import com.coremedia.rest.cap.validators.StructLinkListMaxLengthValidator;
import com.coremedia.rest.validation.Severity;
import com.coremedia.rest.validators.EmailValidator;
import com.coremedia.rest.validators.ListMinLengthValidator;
import com.coremedia.rest.validators.NotEmptyValidator;
import com.coremedia.rest.validators.RegExpValidator;
import com.coremedia.rest.validators.UrlValidator;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@ImportResource(
        value = {
                "classpath:com/coremedia/cap/common/uapi-services.xml",
                "classpath:com/coremedia/mimetype/mimetype-service.xml",
                "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml",
                "classpath:/com/coremedia/blueprint/base/links/bpbase-links-services.xml",
                "classpath:/com/coremedia/cap/transform/transform-services.xml",
                "classpath:/com/coremedia/blueprint/base/pagegrid/impl/bpbase-pagegrid-services.xml"
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class)
@Import(CapRestServiceConfiguration.class)
@EnableConfigurationProperties(StudioConfigurationProperties.class)
class ValidatorsStudioConfiguration {

  @Bean
  ContentTypeValidator cmLocalizedValidator() {
    ContentTypeValidator cmLocalizedValidator = new ContentTypeValidator();
    cmLocalizedValidator.setContentType("CMLocalized");
    cmLocalizedValidator.setValidatingSubtypes(true);

    LinkListMaxLengthValidator linkListMaxLengthValidator = new LinkListMaxLengthValidator();
    linkListMaxLengthValidator.setProperty("master");
    cmLocalizedValidator.setValidators(Collections.singletonList(
            linkListMaxLengthValidator
    ));
    return cmLocalizedValidator;
  }

  @Bean
  ContentTypeValidator cmTeaserValidator() {
    ContentTypeValidator cmTeaserValidator = new ContentTypeValidator();
    cmTeaserValidator.setContentType("CMTeaser");
    cmTeaserValidator.setValidatingSubtypes(false);

    StructLinkListMaxLengthValidator structLinkListMaxLengthValidator = new StructLinkListMaxLengthValidator();
    structLinkListMaxLengthValidator.setProperty("targets");
    structLinkListMaxLengthValidator.setListPropertyName("links");
    cmTeaserValidator.setValidators(Collections.singletonList(
            structLinkListMaxLengthValidator
    ));
    return cmTeaserValidator;
  }

  @Bean
  ContentTypeValidator cmPictureValidator() {
    ContentTypeValidator cmPictureValidator = new ContentTypeValidator();
    cmPictureValidator.setContentType("CMPicture");
    cmPictureValidator.setValidatingSubtypes(true);

    NotEmptyValidator notEmptyValidator = new NotEmptyValidator();
    notEmptyValidator.setProperty("data");
    cmPictureValidator.setValidators(Collections.singletonList(
            notEmptyValidator
    ));
    return cmPictureValidator;
  }

  @Bean
  ImageMapAreasValidator cmImageMapAreasValidator(CapConnection connection) {
    ImageMapAreasValidator cmImageMapAreasValidator = new ImageMapAreasValidator();
    cmImageMapAreasValidator.setConnection(connection);
    cmImageMapAreasValidator.setContentType("CMImageMap");
    cmImageMapAreasValidator.setValidatingSubtypes(true);
    cmImageMapAreasValidator.setImagePropertyPath("pictures.data");
    cmImageMapAreasValidator.setStructProperty("localSettings");
    return cmImageMapAreasValidator;
  }

  @Bean
  ImageMapOverlayConfigurationValidator cmImageMapOverlayConfigurationValidator(CapConnection connection) {
    ImageMapOverlayConfigurationValidator cmImageMapOverlayConfigurationValidator = new ImageMapOverlayConfigurationValidator();
    cmImageMapOverlayConfigurationValidator.setConnection(connection);
    cmImageMapOverlayConfigurationValidator.setContentType("CMImageMap");
    cmImageMapOverlayConfigurationValidator.setValidatingSubtypes(true);
    cmImageMapOverlayConfigurationValidator.setStructProperty("localSettings");
    return cmImageMapOverlayConfigurationValidator;
  }

  @Bean
  SelfReferringLinkListValidator cmLinkListValidator(CapConnection connection) {
    SelfReferringLinkListValidator cmLinkListValidator = new SelfReferringLinkListValidator();
    cmLinkListValidator.setConnection(connection);
    cmLinkListValidator.setContentType("CMLinkable");
    cmLinkListValidator.setValidatingSubtypes(true);
    return cmLinkListValidator;
  }

  @Bean
  SelfReferringStructLinkListValidator cmStructLinkListValidator(CapConnection connection) {
    SelfReferringStructLinkListValidator cmStructLinkListValidator = new SelfReferringStructLinkListValidator();
    cmStructLinkListValidator.setConnection(connection);
    cmStructLinkListValidator.setContentType("CMLinkable");
    cmStructLinkListValidator.setValidatingSubtypes(true);
    return cmStructLinkListValidator;
  }

  @Bean
  StructLinkListIndexValidator cmQueryListIndexValidator(CapConnection connection) {
    StructLinkListIndexValidator cmQueryListIndexValidator = new StructLinkListIndexValidator();
    cmQueryListIndexValidator.setConnection(connection);
    cmQueryListIndexValidator.setContentType("CMQueryList");
    cmQueryListIndexValidator.setPropertyName("extendedItems");
    cmQueryListIndexValidator.setListPropertyName("links");
    cmQueryListIndexValidator.setIndexPropertyName("index");
    cmQueryListIndexValidator.setMaxLengthPropertyName("limit");
    cmQueryListIndexValidator.setPaginationPropertyName("loadMore");
    return cmQueryListIndexValidator;
  }

  @Bean
  ContentTypeValidator cmChannelValidator() {
    ContentTypeValidator cmChannelValidator = new ContentTypeValidator();
    cmChannelValidator.setContentType("CMChannel");
    cmChannelValidator.setValidatingSubtypes(true);

    cmChannelValidator.setValidators(Collections.singletonList(getTitleNotEmptyValidator()));
    return cmChannelValidator;
  }

  @Bean
  ChannelSegmentValidator cmChannelSegmentValidator(UrlPathFormattingHelper urlPathFormattingHelper,
                                                    CapConnection connection) {
    ChannelSegmentValidator cmChannelSegmentValidator = new ChannelSegmentValidator(urlPathFormattingHelper);
    cmChannelSegmentValidator.setConnection(connection);
    cmChannelSegmentValidator.setContentType("CMChannel");
    return cmChannelSegmentValidator;
  }

  @Bean
  RootChannelSegmentValidator cmChannelRootSegmentValidator(UrlPathFormattingHelper urlPathFormattingHelper,
                                                            SitesService sitesService,
                                                            CapConnection connection) {
    RootChannelSegmentValidator cmChannelRootSegmentValidator = new RootChannelSegmentValidator(urlPathFormattingHelper, sitesService);
    cmChannelRootSegmentValidator.setConnection(connection);
    cmChannelRootSegmentValidator.setContentType("CMChannel");
    cmChannelRootSegmentValidator.setValidatingSubtypes(true);
    return cmChannelRootSegmentValidator;
  }

  @Bean
  ArchiveValidator cmArchiveValidator(CapConnection connection) {
    ArchiveValidator cmArchiveValidator = new ArchiveValidator();
    cmArchiveValidator.setConnection(connection);
    cmArchiveValidator.setContentType("CMTemplateSet");
    cmArchiveValidator.setPropertyName("archive");
    return cmArchiveValidator;
  }

  @Bean
  ValidityValidator cmValidityValidator(CapConnection connection) {
    ValidityValidator cmValidityValidator = new ValidityValidator();
    cmValidityValidator.setConnection(connection);
    cmValidityValidator.setPropertyValidFrom("validFrom");
    cmValidityValidator.setPropertyValidTo("validTo");
    cmValidityValidator.setValidatingSubtypes(true);
    return cmValidityValidator;
  }

  @Bean
  VisibilityValidator cmVisibilityValidator(CapConnection connection,
                                            ContentBackedPageGridService contentBackedPageGridService) {
    VisibilityValidator cmVisibilityValidator = new VisibilityValidator();
    cmVisibilityValidator.setConnection(connection);
    cmVisibilityValidator.setContentType("CMChannel");
    cmVisibilityValidator.setValidatingSubtypes(true);
    cmVisibilityValidator.setPageGridService(contentBackedPageGridService);
    cmVisibilityValidator.setPropertyValidFrom("validFrom");
    cmVisibilityValidator.setPropertyValidTo("validTo");
    cmVisibilityValidator.setPropertyVisibleFrom("visibleFrom");
    cmVisibilityValidator.setPropertyVisibleTo("visibleTo");
    return cmVisibilityValidator;
  }

  @Bean
  ChannelNavigationValidator cmChannelNavigationValidator(CapConnection connection) {
    ChannelNavigationValidator cmChannelNavigationValidator = new ChannelNavigationValidator();
    cmChannelNavigationValidator.setConnection(connection);
    cmChannelNavigationValidator.setContentType("CMChannel");
    return cmChannelNavigationValidator;
  }

  @Bean
  ChannelIsPartOfNavigationValidator cmNotInNavigationValidator(CapConnection connection) {
    ChannelIsPartOfNavigationValidator cmNotInNavigationValidator = new ChannelIsPartOfNavigationValidator();
    cmNotInNavigationValidator.setConnection(connection);
    cmNotInNavigationValidator.setContentType("CMChannel");
    return cmNotInNavigationValidator;
  }

  @Bean
  IsPartOfNavigationValidator cmNotPartOfNavigationValidator(CapConnection connection,
                                                             ContextStrategy<Content, Content> contentContextStrategy) {
    IsPartOfNavigationValidator cmNotPartOfNavigationValidator = new IsPartOfNavigationValidator();
    cmNotPartOfNavigationValidator.setConnection(connection);
    cmNotPartOfNavigationValidator.setValidatingSubtypes(true);
    cmNotPartOfNavigationValidator.setContentType("CMLinkable");
    cmNotPartOfNavigationValidator.setContextStrategy(contentContextStrategy);
    return cmNotPartOfNavigationValidator;
  }

  @Bean
  ChannelReferrerValidator cmChannelReferrerValidator(CapConnection connection) {
    ChannelReferrerValidator cmChannelReferrerValidator = new ChannelReferrerValidator();
    cmChannelReferrerValidator.setConnection(connection);
    cmChannelReferrerValidator.setContentType("CMChannel");
    return cmChannelReferrerValidator;
  }

  @Bean
  AbstractCodeValidator cmAbstractCodeValidator(CapConnection connection) {
    AbstractCodeValidator cmAbstractCodeValidator = new AbstractCodeValidator();
    cmAbstractCodeValidator.setConnection(connection);
    cmAbstractCodeValidator.setContentType("CMAbstractCode");
    cmAbstractCodeValidator.setValidatingSubtypes(true);
    return cmAbstractCodeValidator;
  }

  /**
   * All Document Types with title property not empty validation
   */
  @Bean
  ContentTypeValidator cmArticleValidator() {
    ContentTypeValidator cmArticleValidator = new ContentTypeValidator();
    cmArticleValidator.setContentType("CMArticle");
    cmArticleValidator.setValidatingSubtypes(true);

    NotEmptyMarkupValidator notEmptyMarkupValidator = new NotEmptyMarkupValidator();
    notEmptyMarkupValidator.setProperty("detailText");
    cmArticleValidator.setValidators(Arrays.asList(
            getTitleNotEmptyValidator(),
            notEmptyMarkupValidator
    ));
    return cmArticleValidator;
  }

  @Bean
  ContentTypeValidator cmPersonValidator() {
    ContentTypeValidator cmPersonValidator = new ContentTypeValidator();
    cmPersonValidator.setContentType("CMPerson");
    cmPersonValidator.setValidatingSubtypes(true);

    NotEmptyValidator notEmptyValidatorFirstName = new NotEmptyValidator();
    notEmptyValidatorFirstName.setProperty("firstName");
    NotEmptyValidator notEmptyValidatorLastName = new NotEmptyValidator();
    notEmptyValidatorLastName.setProperty("lastName");
    EmailValidator emailValidator = new EmailValidator();
    emailValidator.setProperty("eMail");
    cmPersonValidator.setValidators(Arrays.asList(
            notEmptyValidatorFirstName,
            notEmptyValidatorLastName,
            emailValidator
    ));
    return cmPersonValidator;
  }

  @Bean
  MasterVersionUpdatedValidator masterVersionUpdatedValidator(CapConnection connection,
                                                              SitesService sitesService) {
    MasterVersionUpdatedValidator masterVersionUpdatedValidator = new MasterVersionUpdatedValidator();
    masterVersionUpdatedValidator.setConnection(connection);
    masterVersionUpdatedValidator.setSitesService(sitesService);
    masterVersionUpdatedValidator.setContentType("CMLocalized");
    masterVersionUpdatedValidator.setValidatingSubtypes(true);
    return masterVersionUpdatedValidator;
  }

  @Bean
  ContentLocaleMatchesSiteLocaleValidator contentLocaleMatchesSiteLocaleValidator(
          CapConnection connection,
          SitesService sitesService,
          @Value("${contentLocaleMatchesSiteLocaleValidator.severity:WARN}") Severity severity) {

    ContentLocaleMatchesSiteLocaleValidator validator
            = new ContentLocaleMatchesSiteLocaleValidator(sitesService);
    validator.setConnection(connection);
    validator.setContentType("CMLocalized");
    validator.setSeverity(severity);
    validator.setValidatingSubtypes(true);
    return validator;
  }

  @Bean
  SameMasterLinkValidator sameMasterLinkValidator(
          CapConnection connection,
          SitesService sitesService,
          @Value("${sameMasterLinkValidator.severity:WARN}") Severity severity) {

    SameMasterLinkValidator validator
            = new SameMasterLinkValidator(sitesService);
    validator.setConnection(connection);
    validator.setContentType("CMLocalized");
    validator.setSeverity(severity);
    validator.setValidatingSubtypes(true);
    return validator;
  }

  @Bean
  DuplicateDerivedInSiteValidator duplicateDerivedInSiteValidator(
          CapConnection connection,
          SitesService sitesService,
          @Value("${duplicateDerivedInSiteValidator.severity:WARN}") Severity severity) {

    DuplicateDerivedInSiteValidator validator
            = new DuplicateDerivedInSiteValidator(sitesService);
    validator.setConnection(connection);
    validator.setContentType("CMLocalized");
    validator.setSeverity(severity);
    validator.setValidatingSubtypes(true);
    return validator;
  }

  @Bean
  CrossSiteLinkValidator crossSiteLinkValidator(CapConnection connection,
                                                SitesService sitesService,
                                                @Value("WARN") Severity defaultSeverity,
                                                @Value("WARN") Severity severityCrossLocale,
                                                @Value("WARN") Severity severityCrossSite,
                                                @Value("WARN") Severity severityCrossSiteLocale) {
    CrossSiteLinkValidator crossSiteLinkValidator = new CrossSiteLinkValidator();
    crossSiteLinkValidator.setConnection(connection);
    crossSiteLinkValidator.setSitesService(sitesService);
    crossSiteLinkValidator.setContentType("CMLocalized");
    crossSiteLinkValidator.setValidatingSubtypes(true);
    crossSiteLinkValidator.setExcludedProperties(Collections.singletonList("placement"));
    crossSiteLinkValidator.setDefaultSeverity(defaultSeverity);
    crossSiteLinkValidator.setSeverityCrossLocale(severityCrossLocale);
    crossSiteLinkValidator.setSeverityCrossSite(severityCrossSite);
    crossSiteLinkValidator.setSeverityCrossSiteLocale(severityCrossSiteLocale);
    return crossSiteLinkValidator;
  }

  @Bean
  ConfigurableDeadLinkValidator configurableDeadLinkValidator(CapConnection connection) {
    ConfigurableDeadLinkValidator configurableDeadLinkValidator = new ConfigurableDeadLinkValidator();
    configurableDeadLinkValidator.setConnection(connection);
    configurableDeadLinkValidator.setExcludedProperties(Collections.singletonList("placement"));
    return configurableDeadLinkValidator;
  }

  @Bean
  PlacementsValidator placementsValidator(CapConnection connection,
                                          SitesService sitesService,
                                          ContentBackedPageGridService contentBackedPageGridService,
                                          @Value("WARN") Severity severityCrossLocale,
                                          @Value("WARN") Severity severityCrossSite,
                                          @Value("WARN") Severity severityCrossSiteLocale,
                                          @Value("ERROR") Severity severityDeadLink) {
    PlacementsValidator placementsValidator = new PlacementsValidator();
    placementsValidator.setConnection(connection);
    placementsValidator.setSitesService(sitesService);
    placementsValidator.setPageGridService(contentBackedPageGridService);
    placementsValidator.setContentType("CMChannel");
    placementsValidator.setValidatingSubtypes(true);
    placementsValidator.setSeverityCrossLocale(severityCrossLocale);
    placementsValidator.setSeverityCrossSite(severityCrossSite);
    placementsValidator.setSeverityCrossSiteLocale(severityCrossSiteLocale);
    placementsValidator.setSeverityDeadLink(severityDeadLink);
    return placementsValidator;
  }

  @Bean
  ContentTypeValidator cmAudioValidator() {
    ContentTypeValidator cmAudioValidator = new ContentTypeValidator();
    cmAudioValidator.setContentType("CMAudio");
    cmAudioValidator.setValidatingSubtypes(true);

    cmAudioValidator.setValidators(Collections.singletonList(
            getTitleNotEmptyValidator()
    ));
    return cmAudioValidator;
  }

  @SuppressWarnings("ProhibitedExceptionDeclared")
  @Bean
  ContentTypeValidator cmDownloadValidator() throws Exception {
    ContentTypeValidator cmDownloadValidator = new ContentTypeValidator();
    cmDownloadValidator.setContentType("CMDownload");
    cmDownloadValidator.setValidatingSubtypes(true);

    NotEmptyValidator notEmptyValidator = new NotEmptyValidator();
    notEmptyValidator.setProperty("data");
    RegExpValidator regExpValidator = new RegExpValidator();
    regExpValidator.setCode("FilenameValidator");
    regExpValidator.setProperty("filename");
    regExpValidator.setRegExp("^[^\\\\/:*?\"<>|]*$");
    regExpValidator.afterPropertiesSet();

    cmDownloadValidator.setValidators(Arrays.asList(
            notEmptyValidator,
            getTitleNotEmptyValidator(),
            regExpValidator
    ));
    return cmDownloadValidator;
  }

  @Bean
  ContentTypeValidator cmExternalLinkValidator() {
    ContentTypeValidator cmExternalLinkValidator = new ContentTypeValidator();
    cmExternalLinkValidator.setContentType("CMExternalLink");
    cmExternalLinkValidator.setValidatingSubtypes(true);

    NotEmptyValidator notEmptyValidator = new NotEmptyValidator();
    notEmptyValidator.setProperty("url");
    UrlValidator urlValidator = new UrlValidator();
    urlValidator.setProperty("url");
    cmExternalLinkValidator.setValidators(Arrays.asList(
            notEmptyValidator,
            urlValidator
    ));
    return cmExternalLinkValidator;
  }

  @Bean
  ContentTypeValidator cmGalleryValidator() {
    ContentTypeValidator cmGalleryValidator = new ContentTypeValidator();
    cmGalleryValidator.setContentType("CMGallery");
    cmGalleryValidator.setValidatingSubtypes(true);

    cmGalleryValidator.setValidators(Collections.singletonList(
            getTitleNotEmptyValidator()
    ));
    return cmGalleryValidator;
  }

  @Bean
  ContentTypeValidator cmVideoValidator() {
    ContentTypeValidator cmVideoValidator = new ContentTypeValidator();
    cmVideoValidator.setContentType("CMVideo");
    cmVideoValidator.setValidatingSubtypes(true);
    cmVideoValidator.setValidators(Collections.singletonList(
            getTitleNotEmptyValidator()
    ));
    return cmVideoValidator;
  }

  @Bean
  AtLeastOneNotEmptyValidator atLeastOneNotEmptyValidator(CapConnection connection) {
    AtLeastOneNotEmptyValidator atLeastOneNotEmptyValidator = new AtLeastOneNotEmptyValidator();
    atLeastOneNotEmptyValidator.setConnection(connection);
    atLeastOneNotEmptyValidator.setContentType("CMVideo");
    atLeastOneNotEmptyValidator.setValidatingSubtypes(true);
    atLeastOneNotEmptyValidator.setShowIssueForProperty("data");
    atLeastOneNotEmptyValidator.setExactlyOneMustBeSet(true);
    atLeastOneNotEmptyValidator.setProperties(Arrays.asList("data", "dataUrl"));
    return atLeastOneNotEmptyValidator;
  }

  @Bean
  SiteNameValidator cmSiteValidator(SiteModel siteModel) {
    SiteNameValidator cmSiteValidator = new SiteNameValidator();
    cmSiteValidator.setContentType("CMSite");
    cmSiteValidator.setValidatingSubtypes(true);
    cmSiteValidator.setSiteModel(siteModel);
    return cmSiteValidator;
  }

  @Bean
  ContentTypeValidator cmSpinnerValidator() {
    ContentTypeValidator cmSpinnerValidator = new ContentTypeValidator();
    cmSpinnerValidator.setContentType("CMSpinner");
    cmSpinnerValidator.setValidatingSubtypes(true);

    ListMinLengthValidator listMinLengthValidator = new ListMinLengthValidator();
    listMinLengthValidator.setProperty("sequence");
    listMinLengthValidator.setMinLength(2);
    cmSpinnerValidator.setValidators(Collections.singletonList(
            listMinLengthValidator
    ));
    return cmSpinnerValidator;
  }

  @Bean
  TimelineValidator cmTimelineValidator(CapConnection connection) {
    TimelineValidator cmTimelineValidator = new TimelineValidator();
    cmTimelineValidator.setConnection(connection);
    cmTimelineValidator.setContentType("CMVideo");
    cmTimelineValidator.setAllowSameStartTime(true);
    cmTimelineValidator.setValidatingSubtypes(true);
    return cmTimelineValidator;
  }

  @Bean
  ImageCropSizeValidator imageCropSizeValidator(CapConnection connection,
                                                TransformImageService transformImageService,
                                                ImageDimensionsExtractor imageDimensionsExtractor) {
    ImageCropSizeValidator imageCropSizeValidator = new ImageCropSizeValidator();
    imageCropSizeValidator.setConnection(connection);
    imageCropSizeValidator.setContentType("CMPicture");
    imageCropSizeValidator.setTransformImageService(transformImageService);
    imageCropSizeValidator.setStructProperty("localSettings");
    imageCropSizeValidator.setDataProperty("data");
    imageCropSizeValidator.setTransformsStructProperty("transforms");
    imageCropSizeValidator.setImageDimensionExtractor(imageDimensionsExtractor);
    imageCropSizeValidator.setFocusAreaProperty("focusArea");
    return imageCropSizeValidator;
  }

  private NotEmptyValidator getTitleNotEmptyValidator() {
    NotEmptyValidator notEmptyValidator = new NotEmptyValidator();
    notEmptyValidator.setProperty("title");
    return notEmptyValidator;
  }
}

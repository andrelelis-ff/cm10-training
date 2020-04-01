package com.coremedia.blueprint.assets.studio.validation;

import com.coremedia.blueprint.assets.studio.AssetManagementConfiguration;
import com.coremedia.blueprint.base.rest.config.ConfigurationService;
import com.coremedia.blueprint.base.rest.config.StructConfiguration;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.cms.assets.AssetConstants;
import com.coremedia.rest.validation.impl.Issue;
import com.coremedia.rest.validation.impl.IssuesImpl;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AssetMetadataValidatorTest.LocalConfig.class)
public class AssetMetadataValidatorTest {
  public static final String METADATA_PROPERTY = "metadata";
  private static final ImmutableList<String> DEFAULT_CHANNELS =
          ImmutableList.<String>builder().add("channel1", "channel2", "channel3").build();
  private static final ImmutableList<String> DEFAULT_REGIONS =
          ImmutableList.<String>builder().add("region1", "region2", "region3").build();

  @Inject
  private CapConnection connection;

  private AssetMetadataValidator testling;
  private IssuesImpl<Object> issues;

  @Before
  public void setUp() throws Exception {
    issues = new IssuesImpl<>(new Object(), Collections.singletonList(METADATA_PROPERTY));
    testling = createValidator(DEFAULT_CHANNELS, DEFAULT_REGIONS);
  }

  @Test
  public void testNoIssuesForNullValue() throws Exception {
    testling.validate(null, issues);

    assertEmptyIssues(issues);
  }

  @Test
  public void testNotOfTypeStructIssue() throws Exception {
    testling.validate(new Object(), issues);

    assertIssuesContainNotStructIssue(issues);
  }

  @Test
  public void testNoIssuesForNullChannelsOrRegions() throws Exception {
    Object entity = connection.getStructService().emptyStruct();

    testling = createValidator(null, DEFAULT_REGIONS);
    testling.validate(entity, issues);
    assertEmptyIssues(issues);

    testling = createValidator(DEFAULT_CHANNELS, Collections.<String>emptyList());
    testling.validate(entity, issues);
    assertEmptyIssues(issues);

    testling = createValidator(null, null);
    testling.validate(entity, issues);
    assertEmptyIssues(issues);

    testling = createValidator(Collections.<String>emptyList(), Collections.<String>emptyList());
    testling.validate(entity, issues);
    assertEmptyIssues(issues);
  }

  @Test
  public void testNoIssueForSubgroupOfChannelsOrRegions() throws Exception {
    Object entity = connection.getStructService().createStructBuilder()
            .declareStrings(AssetConstants.METADATA_CHANNELS_PROPERTY_NAME, 10,
                    ImmutableList.<String>builder().add("channel1", "channel3").build())
            .declareStrings(AssetConstants.METADATA_REGIONS_PROPERTY_NAME, 10,
                    ImmutableList.<String>builder().add("region2").build())
            .build();

    testling.validate(entity, issues);

    assertEmptyIssues(issues);
  }

  @Test
  public void testIssueForNotConfiguredChannel() throws Exception {
    Object entity = connection.getStructService().createStructBuilder()
            .declareStrings(AssetConstants.METADATA_CHANNELS_PROPERTY_NAME, 10,
                    ImmutableList.<String>builder().add("channel4").build())
            .build();

    testling.validate(entity, issues);

    assertIssuesContainChannelsOrRegions(issues, Collections.singletonList("channel4"), Collections.<String>emptyList());
  }

  @Test
  public void testIssueForNotConfiguredChannelOrRegion() throws Exception {
    Object entity = connection.getStructService().createStructBuilder()
            .declareStrings(AssetConstants.METADATA_CHANNELS_PROPERTY_NAME, 10,
                    ImmutableList.<String>builder().add("channel1", "channel4", "channel5").build())
            .declareStrings(AssetConstants.METADATA_REGIONS_PROPERTY_NAME, 10,
                    ImmutableList.<String>builder().add("region4").build())
            .build();

    testling.validate(entity, issues);

    assertIssuesContainChannelsOrRegions(issues, Arrays.asList("channel4", "channel5"), Collections.singletonList("region4"));
  }

  private void assertEmptyIssues(IssuesImpl<Object> issues) {
    Map<String, Set<Issue<Object>>> issuesByProperty = issues.getByProperty();

    assertThat(issues.getGlobal(), Matchers.empty());

    Set<Issue<Object>> metadataIssues = issuesByProperty.get(METADATA_PROPERTY);
    assertThat(issuesByProperty.values(), Matchers.hasSize(1));
    assertThat(metadataIssues, Matchers.empty());
  }

  private void assertIssuesContainNotStructIssue(IssuesImpl<Object> issues) {
    Map<String, Set<Issue<Object>>> issuesByProperty = issues.getByProperty();

    assertThat(issues.getGlobal(), Matchers.empty());

    Set<Issue<Object>> metadataIssues = issuesByProperty.get(METADATA_PROPERTY);
    assertThat(issuesByProperty.values(), Matchers.hasSize(1));
    assertThat(metadataIssues.iterator().next().getCode(),
            Matchers.is(AssetMetadataValidator.ISSUE_CODE_METADATA_PROPERTY_NOT_OF_TYPE_STRUCT));
  }

  private void assertIssuesContainChannelsOrRegions(IssuesImpl<Object> issues, List<String> channels, List<String> regions) {
    Map<String, Set<Issue<Object>>> issuesByProperty = issues.getByProperty();

    assertThat(issues.getGlobal(), Matchers.empty());

    Set<Issue<Object>> metadataIssues = issuesByProperty.get(METADATA_PROPERTY);
    assertThat(metadataIssues, Matchers.empty());

    assertThat(issuesByProperty.values(), Matchers.hasSize(channels.size() + regions.size() + 1));
    
    for (String channel : channels) {
      assertThat(issuesByProperty.get("metadata.channels." + channel).iterator().next().getCode(),
              Matchers.is(AssetMetadataValidator.ISSUE_CODE_UNKNOWN_CHANNEL));
    }
    for (String region : regions) {
      assertThat(issuesByProperty.get("metadata.regions." + region).iterator().next().getCode(),
              Matchers.is(AssetMetadataValidator.ISSUE_CODE_UNKNOWN_REGION));
    }
  }

  private static AssetMetadataValidator createValidator(List<String> channels, List<String> regions) {
    AssetMetadataValidator validator = new AssetMetadataValidator();
    validator.setMetadataProperty(METADATA_PROPERTY);

    AssetManagementConfiguration assetManagementConfiguration = new AssetManagementConfiguration();
    assetManagementConfiguration.setSettingsDocument("SettingsDocument");

    StructConfiguration structConfiguration = new StructConfiguration();
    Map<String, Object> globalStructs = new HashMap<>();
    if (channels != null) {
      globalStructs.put(AssetConstants.METADATA_CHANNELS_PROPERTY_NAME, channels);
    }
    if (regions != null) {
      globalStructs.put(AssetConstants.METADATA_REGIONS_PROPERTY_NAME, regions);
    }
    structConfiguration.setGlobalStructs(globalStructs);

    ConfigurationService configurationService = mock(ConfigurationService.class);
    when(configurationService.getStructMaps(null, "SettingsDocument", "settings")).thenReturn(structConfiguration);

    validator.setAssetManagementConfiguration(assetManagementConfiguration);
    validator.setConfigurationService(configurationService);

    return validator;
  }


  @Configuration
  @Import(XmlRepoConfiguration.class)
  public static class LocalConfig {
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/blueprint/assets/studio/validation/AssetMetadataValidatorTest-content.xml");
    }
  }
}

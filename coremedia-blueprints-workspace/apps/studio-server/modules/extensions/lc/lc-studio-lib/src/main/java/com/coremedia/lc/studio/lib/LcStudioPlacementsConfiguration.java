package com.coremedia.lc.studio.lib;

import com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords;
import com.coremedia.blueprint.base.pagegrid.rest.PlacementsStructFormatAdapter;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.rest.cap.CapRestServiceConfiguration;
import com.coremedia.rest.cap.content.convert.StructAdapter;
import com.coremedia.rest.cap.content.convert.StructListToMapAdapter;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@Import({CapRestServiceConfiguration.class})
@ImportResource(value = {
        "classpath:/com/coremedia/cap/common/uapi-services.xml",
        "classpath:/com/coremedia/blueprint/base/pagegrid/bpbase-pagegrid-configuration.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
public class LcStudioPlacementsConfiguration {

  @Bean
  static PlacementsStructFormatAdapter pdpPlacementsStructFormatAdapterChannel(CapConnection connection,
                                                                               StructAdapter placementListToMapAdapter) {
    PlacementsStructFormatAdapter adapter = new PlacementsStructFormatAdapter();
    adapter.setConnection(connection);
    adapter.setContentType("CMChannel");
    adapter.setProperty("pdpPagegrid");

    adapter.setPlacementListToMapAdapter(placementListToMapAdapter);
    return adapter;
  }

  @Bean
  static PlacementsStructFormatAdapter pdpPlacementsStructFormatAdapterProduct(CapConnection connection,
                                                                               StructAdapter placementListToMapAdapter) {
    PlacementsStructFormatAdapter adapter = new PlacementsStructFormatAdapter();
    adapter.setConnection(connection);
    adapter.setContentType("CMExternalProduct");
    adapter.setProperty("pdpPagegrid");

    adapter.setPlacementListToMapAdapter(placementListToMapAdapter);
    return adapter;
  }

  @Bean
  static StructListToMapAdapter placementListToMapAdapter() {
    StructListToMapAdapter structAdapter = new StructListToMapAdapter();
    structAdapter.setStructListPropertyName(PageGridContentKeywords.PLACEMENTS_STRUCT_LIST_PROPERTY_NAME);
    structAdapter.setStructMapPropertyPath(PageGridContentKeywords.PLACEMENTS_STRUCT_PROPERTY_PATH);
    structAdapter.setLinkPropertyName(PageGridContentKeywords.SECTION_PROPERTY_NAME);
    return structAdapter;
  }
}

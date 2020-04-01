package com.coremedia.lc.studio.lib.rest;

import com.coremedia.elastic.core.api.models.UnresolvableReferenceException;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.rest.linking.Linker;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.HashMap;

@Named
public class ProductInSiteJsonSerializer extends JsonSerializer<ProductInSite> {
  private static final Logger LOG = LoggerFactory.getLogger(ProductInSiteJsonSerializer.class);

  @Inject
  private Linker linker;

  @Override
  public void serialize(ProductInSite value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    HashMap<String, Object> serializedObject = new HashMap<>();
    try {
      Product product = value.getProduct();
      serializedObject.put("$Ref", linker.link(product).toString());
    } catch (UnresolvableReferenceException e) {
      LOG.warn("Could not resolve target reference: {}", e.toString());
    }

    jgen.writeObject(serializedObject);
  }

  @Override
  public Class<ProductInSite> handledType() {
    return ProductInSite.class;
  }
}

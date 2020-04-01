package com.coremedia.blueprint.cae.action.search;

import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.content.Content;
import com.coremedia.id.IdProvider;
import com.coremedia.objectserver.beans.ContentBeanIdScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Function to return {@link CMTaxonomy#getValue()} for a given numeric content ID that identifies a
 * {@link CMTaxonomy} bean which is both {@link ValidationService#validate(Object) valid} and
 * {@link Content#isInProduction() in production}.
 *
 * <p>This function returns null otherwise.
 *
 * @since 1810
 */
public class TaxonomyFacetFieldLabelFunction implements Function<String, String> {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IdProvider idProvider;
  private final ValidationService<? super CMTaxonomy> validationService;

  public TaxonomyFacetFieldLabelFunction(IdProvider idProvider,
                                         ValidationService<? super CMTaxonomy> validationService) {
    this.idProvider = requireNonNull(idProvider);
    this.validationService = requireNonNull(validationService);
  }

  @Override
  public String apply(String s) {
    Object object;
    try {
      object = idProvider.parseId(ContentBeanIdScheme.PREFIX + s);
    } catch (IllegalArgumentException  e) {
      LOG.debug("Not a content bean id: {}", s, e);
      return null;
    }

    if (!(object instanceof CMTaxonomy)) {
      return null;
    }

    CMTaxonomy taxonomy = (CMTaxonomy) object;
    if (taxonomy.getContent() == null || !taxonomy.getContent().isInProduction()) {
      return null;
    }

    return validationService.validate(taxonomy)
           ? taxonomy.getValue()
           : null;

  }
}

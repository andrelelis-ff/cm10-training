package com.coremedia.livecontext.ecommerce.ibm.pricing;

import com.coremedia.livecontext.ecommerce.ibm.catalog.WcPartNumber;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static java.util.Arrays.asList;

public class WcPriceParam {
  WcQuery query;

  public WcPriceParam(String externalProductId, String currency, @NonNull List<String> contractIds) {
    this.query = new WcQuery(
            asList(new WcPartNumber(externalProductId)),
            currency == null ? null : asList(currency),
            contractIds.isEmpty() ? null : contractIds);
  }

  public WcQuery getQuery() {
    return query;
  }

  public void setQuery(WcQuery query) {
    this.query = query;
  }
}
